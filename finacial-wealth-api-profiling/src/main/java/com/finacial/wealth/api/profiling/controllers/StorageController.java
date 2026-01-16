/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.controllers;

/**
 *
 * @author olufemioshin
 */
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
@RequestMapping("/walletmgt/storage")
public class StorageController {

     private final FirebaseStorageService storageService;

    public StorageController(FirebaseStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/getSlide/{fileName}")
    public ResponseEntity<Object> downloadFile(@PathVariable String fileName, HttpServletRequest request) throws Exception {
        log.info("HIT -/download | File Name : {}", fileName);
        ByteArrayResource res = storageService.downloadFile(fileName);
        long len = res.contentLength();

        return ResponseEntity.ok()
                .contentLength(len)
                .header("Content-type", "application/octet-stream")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(res);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/uploadSlide",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = {"application/json"})
    public ResponseEntity<BaseResponseFireBase> uploadSlide(@RequestParam(value = "file", required = true) MultipartFile multipartFile) {
        log.info("HIT -/upload | File Name : {}", multipartFile.getOriginalFilename());
        return ResponseEntity.status(HttpStatus.OK).body(storageService.uploadSlide(multipartFile));
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/uploadPicture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = {"application/json"})
    public ResponseEntity<BaseResponseFireBase> uploadPicture(@RequestParam(value = "base64Image", required = true) String base64Image) {
        log.info("HIT -/upload | Base64 String len: {}", (base64Image == null ? 0 : base64Image.length()));
        return ResponseEntity.status(HttpStatus.OK).body(storageService.uploadBase64Pix(base64Image));
    }

    @GetMapping("/getSlides")
    public ResponseEntity<BaseResponseFireBase> listSlides() {
        log.info("HIT -/list files from Firebase Storage slides Directory ... ");
        return ResponseEntity.status(HttpStatus.OK).body(storageService.listSlides());
    }

    @DeleteMapping("/delete-slide")
    public ResponseEntity<BaseResponseFireBase> deleteSlide(@RequestBody SlideObject slide) {
        log.info("HIT -/delete single slide from Firebase Storage slides Directory ... ");
        String name = (slide == null ? null : slide.getFileName());
        return ResponseEntity.status(HttpStatus.OK).body(storageService.deleteSlide(name));
    }

    @DeleteMapping("/deleteAllSlides")
    public ResponseEntity<BaseResponseFireBase> deleteAllSlide() {
        log.info("HIT -/delete ALL slides from Firebase Storage slides Directory ... ");
        return ResponseEntity.status(HttpStatus.OK).body(storageService.deleteAllSlides());
    }
}
