/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.integrations.profiling;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.backoffice.campaign.model.BaseResponseFireBase;
import com.finacial.wealth.backoffice.campaign.model.SlideObject;
import com.finacial.wealth.backoffice.campaign.model.UploadBase64Request;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BackofficeStorageService {

    private final ProfilingStorageClient storageClient;

    public BackofficeStorageService(ProfilingStorageClient storageClient) {
        this.storageClient = storageClient;
    }

    public BaseResponseFireBase uploadSlide(MultipartFile file) {
        return storageClient.uploadSlide(file);
    }

    /*public BaseResponseFireBase uploadPictureBase64(String base64Image) {
        return storageClient.uploadPicture(base64Image);
    }*/
    public BaseResponseFireBase listSlides() {
        return storageClient.listSlides();
    }

    public BaseResponseFireBase deleteSlide(SlideObject slide) {
        return storageClient.deleteSlide(slide);
    }

    public BaseResponseFireBase deleteAllSlides() {
        return storageClient.deleteAllSlides();
    }

    public BaseResponseFireBase uploadPictureBase64(String base64Image) {
        UploadBase64Request req = new UploadBase64Request();
        req.setBase64Image(base64Image);
        return storageClient.uploadPictureJson(req);
    }

    public BaseResponseFireBase uploadPictureFile(MultipartFile file) {
        return storageClient.uploadPictureFile(file);
    }
}
