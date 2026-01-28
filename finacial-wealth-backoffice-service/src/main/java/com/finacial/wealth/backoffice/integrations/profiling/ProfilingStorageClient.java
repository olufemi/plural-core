/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.backoffice.integrations.profiling;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.backoffice.campaign.model.BaseResponseFireBase;
import com.finacial.wealth.backoffice.campaign.model.SlideObject;
import com.finacial.wealth.backoffice.campaign.model.UploadBase64Request;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "profiling-service",
        contextId = "profilingStorageClient",
        configuration = {com.finacial.wealth.backoffice.config.FeignConfig.class,
            com.finacial.wealth.backoffice.config.FeignMultipartConfig.class}
)
public interface ProfilingStorageClient {

    @PostMapping(
            value = "/campaign-upload-mgt/storage/uploadSlide",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    BaseResponseFireBase uploadSlide(@RequestPart("file") MultipartFile file);

    // Admin-friendly JSON upload (recommended)
    @PostMapping(
            value = "/campaign-upload-mgt/storage/uploadPicture",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    BaseResponseFireBase uploadPictureJson(@RequestBody UploadBase64Request rq);

    // Best admin UX: upload file directly
    @PostMapping(
            value = "/campaign-upload-mgt/storage/uploadPictureFile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    BaseResponseFireBase uploadPictureFile(@RequestPart("file") MultipartFile file);

    @GetMapping("/campaign-upload-mgt/storage/getSlides")
    BaseResponseFireBase listSlides();

    @DeleteMapping("/campaign-upload-mgt/storage/delete-slide")
    BaseResponseFireBase deleteSlide(@RequestBody SlideObject slide);

    @DeleteMapping("/campaign-upload-mgt/storage/deleteAllSlides")
    BaseResponseFireBase deleteAllSlides();
}
