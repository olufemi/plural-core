/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.campaign.model.BaseResponseFireBase;
import com.finacial.wealth.backoffice.campaign.model.SlideObject;
import com.finacial.wealth.backoffice.campaign.model.UploadBase64Request;
import com.finacial.wealth.backoffice.integrations.profiling.BackofficeStorageService;
import static java.lang.Math.log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/backoffice/storage")
public class BackofficeStorageController {

    private static final Logger log = LoggerFactory.getLogger(BackofficeStorageController.class);

    private final BackofficeStorageService service;

    public BackofficeStorageController(BackofficeStorageService service) {
        this.service = service;
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/uploadPictureFile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = {"application/json"})
    public ResponseEntity<BaseResponseFireBase> uploadPictureFile(
            @RequestPart("file") MultipartFile file) {
        log.info("HIT -/uploadPictureFile | File Name : {}", file.getOriginalFilename());

        BaseResponseFireBase resp = service.uploadPictureFile(file);

        if (resp == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseFireBase.fail(500, "No response from storage service"));
        }

        if (resp.getStatusCode() != 200) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST) // or map dynamically
                    .body(resp);
        }

        return ResponseEntity.status(HttpStatus.OK).body(service.uploadPictureFile(file));
    }

    @PostMapping(value = "/uploadSlide", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS')")
    public ResponseEntity<BaseResponseFireBase> uploadSlide(@RequestPart("file") MultipartFile file) {

        BaseResponseFireBase resp = service.uploadSlide(file);

        if (resp == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseFireBase.fail(500, "No response from storage service"));
        }

        if (resp.getStatusCode() != 200) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST) // or map dynamically
                    .body(resp);
        }

        return ResponseEntity.status(HttpStatus.OK).body(service.uploadSlide(file));
    }

    @PostMapping(value = "/uploadPicture", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS')")
    public ResponseEntity<BaseResponseFireBase> uploadPicture(@RequestBody UploadBase64Request rq) {
        BaseResponseFireBase resp = service.uploadPictureBase64(rq.getBase64Image());

        if (resp == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseFireBase.fail(500, "No response from storage service"));
        }

        if (resp.getStatusCode() != 200) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST) // or map dynamically
                    .body(resp);
        }
        return ResponseEntity.status(HttpStatus.OK).body(service.uploadPictureBase64(rq.getBase64Image()));

    }

    @GetMapping("/slides")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS')")
    public ResponseEntity<BaseResponseFireBase> listSlides() {

        BaseResponseFireBase resp = service.listSlides();

        if (resp == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseFireBase.fail(500, "No response from storage service"));
        }

        if (resp.getStatusCode() != 200) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST) // or map dynamically
                    .body(resp);
        }
        return ResponseEntity.status(HttpStatus.OK).body(service.listSlides());

    }

    @DeleteMapping("/slides")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<BaseResponseFireBase> deleteSlide(@RequestBody SlideObject slide) {

        BaseResponseFireBase resp = service.deleteSlide(slide);

        if (resp == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseFireBase.fail(500, "No response from storage service"));
        }

        if (resp.getStatusCode() != 200) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST) // or map dynamically
                    .body(resp);
        }
        return ResponseEntity.status(HttpStatus.OK).body(service.deleteSlide(slide));

    }

    @DeleteMapping("/slides/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<BaseResponseFireBase> deleteAllSlides() {
        BaseResponseFireBase resp = service.deleteAllSlides();

        if (resp == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseFireBase.fail(500, "No response from storage service"));
        }

        if (resp.getStatusCode() != 200) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST) // or map dynamically
                    .body(resp);
        }
        return ResponseEntity.status(HttpStatus.OK).body(service.deleteAllSlides());

    }

//    @PostMapping(value = "/uploadPictureFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS')")
//    public BaseResponseFireBase uploadPictureFile(@RequestPart("file") MultipartFile file) {
//        return service.uploadPictureFile(file);
//    }
}
