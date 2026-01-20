/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.storage.firebase;

/**
 *
 * @author olufemioshin
 */
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseStorageService {

    private final Storage storage;

    @Value("${fcm.project.bucket.plural}")
    private String bucketName;

    @Value("${firebase.signed-url-minutes:60}")
    private long signedUrlMinutes;

    private static final String SLIDES_DIR = "slides/";
    private static final String PICTURES_DIR = "pictures/";

    public FirebaseStorageService(Storage storage) {
        this.storage = storage;
    }

    // --- NEW: check object exists ---
    public boolean exists(String objectName) {
        if (objectName == null || objectName.trim().isEmpty()) {
            return false;
        }
        Blob b = storage.get(BlobId.of(bucketName, objectName.trim()));
        return b != null && !b.isDirectory();
    }

    // --- NEW: sign url on demand ---
    public String signUrl(String objectName) {
        if (objectName == null || objectName.trim().isEmpty()) {
            return null;
        }
        try {
            URL url = storage.signUrl(
                    BlobInfo.newBuilder(BlobId.of(bucketName, objectName.trim())).build(),
                    signedUrlMinutes,
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.withV4Signature()
            );
            return url.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // --- NEW: delete by objectName ---
    public boolean deleteObject(String objectName) {
        if (objectName == null || objectName.trim().isEmpty()) {
            return false;
        }
        return storage.delete(BlobId.of(bucketName, objectName.trim()));
    }

    public BaseResponseFireBase uploadSlide(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return BaseResponseFireBase.fail(400, "File is required");
            }

            String original = safeName(file.getOriginalFilename());
            String objectName = SLIDES_DIR + System.currentTimeMillis() + "_" + original;

            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName))
                    .setContentType(guessContentType(file.getContentType(), original))
                    .build();

            storage.create(blobInfo, file.getBytes());

            FileItem item = buildFileItem(storage.get(BlobId.of(bucketName, objectName)));
            return BaseResponseFireBase.ok("Uploaded", item);

        } catch (Exception e) {
            return BaseResponseFireBase.fail(500, "Upload failed: " + e.getMessage());
        }
    }

    public BaseResponseFireBase uploadBase64Pix(String base64Image) {
        try {
            if (base64Image == null || base64Image.trim().isEmpty()) {
                return BaseResponseFireBase.fail(400, "base64Image is required");
            }

            // supports both:
            // 1) raw base64
            // 2) data:image/png;base64,AAA...
            String contentType = "image/png";
            String payload = base64Image.trim();

            int comma = payload.indexOf(',');
            if (payload.startsWith("data:") && comma > 0) {
                String meta = payload.substring(5, comma); // e.g. image/png;base64
                String[] parts = meta.split(";");
                if (parts.length > 0 && parts[0].contains("/")) {
                    contentType = parts[0];
                }
                payload = payload.substring(comma + 1);
            }

            byte[] bytes = Base64.getDecoder().decode(payload.getBytes(StandardCharsets.UTF_8));
            String ext = contentTypeToExt(contentType);
            String objectName = PICTURES_DIR + System.currentTimeMillis() + "_" + UUID.randomUUID() + ext;

            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName))
                    .setContentType(contentType)
                    .build();

            storage.create(blobInfo, bytes);

            FileItem item = buildFileItem(storage.get(BlobId.of(bucketName, objectName)));
            return BaseResponseFireBase.ok("Uploaded", item);

        } catch (Exception e) {
            return BaseResponseFireBase.fail(500, "Upload failed: " + e.getMessage());
        }
    }

    public BaseResponseFireBase listSlides() {
        try {
            List<FileItem> items = new ArrayList<FileItem>();

            Page<Blob> blobs = storage.list(
                    bucketName,
                    Storage.BlobListOption.prefix(SLIDES_DIR),
                    Storage.BlobListOption.currentDirectory()
            );

            for (Blob b : blobs.iterateAll()) {
                if (b == null || b.isDirectory()) {
                    continue;
                }
                items.add(buildFileItem(b));
            }

            // newest first
            Collections.sort(items, new Comparator<FileItem>() {
                @Override
                public int compare(FileItem a, FileItem b) {
                    Long x = a.getUpdatedAtMs() == null ? 0L : a.getUpdatedAtMs();
                    Long y = b.getUpdatedAtMs() == null ? 0L : b.getUpdatedAtMs();
                    return y.compareTo(x);
                }
            });

            return BaseResponseFireBase.ok("OK", items);

        } catch (Exception e) {
            return BaseResponseFireBase.fail(500, "List failed: " + e.getMessage());
        }
    }

    public BaseResponseFireBase deleteSlide(String fileNameOrObjectName) {
        try {
            if (fileNameOrObjectName == null || fileNameOrObjectName.trim().isEmpty()) {
                return BaseResponseFireBase.fail(400, "fileName is required");
            }

            String objectName = normalizeSlideObjectName(fileNameOrObjectName);
            boolean deleted = storage.delete(BlobId.of(bucketName, objectName));

            if (!deleted) {
                return BaseResponseFireBase.fail(404, "Not found: " + objectName);
            }
            return BaseResponseFireBase.ok("Deleted", objectName);

        } catch (Exception e) {
            return BaseResponseFireBase.fail(500, "Delete failed: " + e.getMessage());
        }
    }

    public BaseResponseFireBase deleteAllSlides() {
        try {
            int count = 0;

            Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(SLIDES_DIR));
            for (Blob b : blobs.iterateAll()) {
                if (b == null || b.isDirectory()) {
                    continue;
                }
                if (storage.delete(b.getBlobId())) {
                    count++;
                }
            }

            return BaseResponseFireBase.ok("Deleted all slides", count);

        } catch (Exception e) {
            return BaseResponseFireBase.fail(500, "Delete all failed: " + e.getMessage());
        }
    }

    public ByteArrayResource downloadFile(String fileNameOrObjectName) throws Exception {
        String objectName = normalizeSlideObjectName(fileNameOrObjectName);

        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        if (blob == null) {
            throw new NoSuchElementException("File not found: " + objectName);
        }
        byte[] bytes = blob.getContent();
        return new ByteArrayResource(bytes);
    }

    // ---------------- helpers ----------------
    private FileItem buildFileItem(Blob b) {
        FileItem i = new FileItem();
        i.setName(b.getName());
        i.setFileName(extractFileName(b.getName()));
        i.setSize(b.getSize());
        i.setContentType(b.getContentType());
        i.setUpdatedAtMs(b.getUpdateTime());

        // optional: signed URL (works even if bucket is private)
        try {
            URL url = storage.signUrl(
                    BlobInfo.newBuilder(BlobId.of(bucketName, b.getName())).build(),
                    signedUrlMinutes,
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.withV4Signature()
            );
            i.setSignedUrl(url.toString());
        } catch (Exception ignore) {
            // if signing fails, we still return metadata
        }
        return i;
    }

    private String normalizeSlideObjectName(String fileNameOrObjectName) {
        String s = fileNameOrObjectName.trim();
        if (s.startsWith(SLIDES_DIR)) {
            return s;
        }
        return SLIDES_DIR + s;
    }

    private String extractFileName(String objectName) {
        int idx = objectName.lastIndexOf('/');
        return (idx >= 0 ? objectName.substring(idx + 1) : objectName);
    }

    private String safeName(String name) {
        if (name == null) {
            return "file";
        }
        // very simple sanitization
        return name.replace("\\", "_").replace("/", "_").replace("..", "_").trim();
    }

    private String guessContentType(String provided, String filename) {
        if (provided != null && !provided.trim().isEmpty()) {
            return provided;
        }
        String f = (filename == null ? "" : filename.toLowerCase());
        if (f.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }
        if (f.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (f.endsWith(".pdf")) {
            return "application/pdf";
        }
        return "application/octet-stream";
    }

    private String contentTypeToExt(String contentType) {
        if (contentType == null) {
            return ".png";
        }
        String c = contentType.toLowerCase();
        if (c.contains("jpeg") || c.contains("jpg")) {
            return ".jpg";
        }
        if (c.contains("png")) {
            return ".png";
        }
        if (c.contains("webp")) {
            return ".webp";
        }
        return ".bin";
    }
}
