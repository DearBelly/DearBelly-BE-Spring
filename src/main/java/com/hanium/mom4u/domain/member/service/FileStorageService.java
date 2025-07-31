package com.hanium.mom4u.domain.member.service;

import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Transactional
public class FileStorageService {
    private static final String UPLOAD_DIR="uploads";

    public String save(MultipartFile file){;
        if(file==null || file.isEmpty()){
            throw GeneralException.of(StatusCode.FILE_EMPTY);
        }try {
            // 업로드 폴더가 없으면 생성
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 파일명 중복 방지 (타임스탬프 등)
            String originalFilename = file.getOriginalFilename();
            String filename = System.currentTimeMillis() + "_" + originalFilename;
            Path filePath = Paths.get(UPLOAD_DIR, filename);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath);

            // 반환: 웹에서 접근 가능한 경로
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw GeneralException.of(StatusCode.FILE_SAVE_FAILED);
        }

    }
    }

