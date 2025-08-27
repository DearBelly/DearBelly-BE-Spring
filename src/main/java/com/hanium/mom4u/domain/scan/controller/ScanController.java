package com.hanium.mom4u.domain.scan.controller;

import com.hanium.mom4u.domain.scan.service.ScanService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/scan")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;

    @PostMapping
    public void scan(
            @RequestParam("file") MultipartFile file) {
        scanService.sendImageToS3(file);

    }
}
