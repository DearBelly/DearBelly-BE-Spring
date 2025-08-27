package com.hanium.mom4u.external.s3.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.multipart.MultipartFile;

public class FileUploadEvent extends ApplicationEvent {

    private MultipartFile file;

    public FileUploadEvent(Object source, MultipartFile file) {
        super(source);
        this.file = file;
    }
    public MultipartFile getFile() {
        return file;
    }
}
