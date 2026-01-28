/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.controllers;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.client.model.UploadBase64Request;
import com.finacial.wealth.api.profiling.storage.firebase.BaseResponseFireBase;
import com.finacial.wealth.api.profiling.storage.firebase.FirebaseStorageService;
import com.finacial.wealth.api.profiling.storage.firebase.SlideObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@RestController
@RequestMapping("/campaign-upload-mgt/storage")
public class StorageController {

    private final FirebaseStorageService storageService;

    public StorageController(FirebaseStorageService storageService) {
        this.storageService = storageService;
    }

    /* =========================================================
     * DOWNLOAD (BACKWARD + REST CORRECT)
     * ========================================================= */

    // Existing POST (keep for backward compatibility)
    @PostMapping("/getSlide/{fileName}")
    public ResponseEntity<Object> downloadFilePost(
            @PathVariable String fileName,
            HttpServletRequest request
    ) throws Exception {
        log.info("HIT -/download (POST) | File Name : {}", fileName);
        return buildDownloadResponse(fileName);
    }

    // New GET (REST correct)
    @GetMapping("/getSlide/{fileName}")
    public ResponseEntity<Object> downloadFileGet(@PathVariable String fileName) throws Exception {
        log.info("HIT -/download (GET) | File Name : {}", fileName);
        return buildDownloadResponse(fileName);
    }

    private ResponseEntity<Object> buildDownloadResponse(String fileName) throws Exception {
        ByteArrayResource res = storageService.downloadFile(fileName);
        long len = res.contentLength();

        return ResponseEntity.ok()
                .contentLength(len)
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(res);
    }

    /* =========================================================
     * UPLOAD SLIDE (PDF / PPT / VIDEO)
     * ========================================================= */

    @PostMapping(
            value = "/uploadSlide",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BaseResponseFireBase> uploadSlide(
            @RequestParam("file") MultipartFile multipartFile
    ) {
        log.info("HIT -/uploadSlide | File Name : {}", multipartFile.getOriginalFilename());
        return ResponseEntity.ok(storageService.uploadSlide(multipartFile));
    }

    /* =========================================================
     * UPLOAD PICTURE – LEGACY MULTIPART (DO NOT REMOVE)
     * ========================================================= */

    @PostMapping(
            value = "/uploadPicture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BaseResponseFireBase> uploadPictureMultipart(
            @RequestParam("base64Image") String base64Image
    ) {
        log.info("HIT -/uploadPicture (multipart) | Base64 len: {}",
                base64Image == null ? 0 : base64Image.length());

        return ResponseEntity.ok(storageService.uploadBase64Pix(base64Image));
    }

    /* =========================================================
     * UPLOAD PICTURE – JSON (ADMIN / BACKOFFICE FRIENDLY)
     * ========================================================= */

    @PostMapping(
            value = "/uploadPicture",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BaseResponseFireBase> uploadPictureJson(
            @RequestBody UploadBase64Request rq
    ) {
        String base64Image = (rq == null ? null : rq.getBase64Image());
        log.info("HIT -/uploadPicture (JSON) | Base64 len: {}",
                base64Image == null ? 0 : base64Image.length());

        return ResponseEntity.ok(storageService.uploadBase64Pix(base64Image));
    }

    /* =========================================================
     * OPTIONAL: UPLOAD IMAGE FILE (BEST FOR ADMIN UX)
     * ========================================================= */

    @PostMapping(
            value = "/uploadPictureFile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BaseResponseFireBase> uploadPictureFile(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("HIT -/uploadPictureFile | File Name : {}", file.getOriginalFilename());
        return ResponseEntity.ok(storageService.uploadPictureFile(file));
    }

    /* =========================================================
     * LIST / DELETE
     * ========================================================= */

    @GetMapping("/getSlides")
    public ResponseEntity<BaseResponseFireBase> listSlides() {
        log.info("HIT -/getSlides");
        return ResponseEntity.ok(storageService.listSlides());
    }

    @DeleteMapping("/delete-slide")
    public ResponseEntity<BaseResponseFireBase> deleteSlide(@RequestBody SlideObject slide) {
        String name = (slide == null ? null : slide.getFileName());
        log.info("HIT -/delete-slide | Object : {}", name);
        return ResponseEntity.ok(storageService.deleteSlide(name));
    }

    @DeleteMapping("/deleteAllSlides")
    public ResponseEntity<BaseResponseFireBase> deleteAllSlides() {
        log.info("HIT -/deleteAllSlides");
        return ResponseEntity.ok(storageService.deleteAllSlides());
    }
}


