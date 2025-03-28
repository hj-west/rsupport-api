package com.rsupport.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private static final String UPLOAD_DIR = "uploads/";

    @Override
    public String upload(MultipartFile file) {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR)); // 디렉토리 생성
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, file.getBytes());
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    @Override
    public void delete(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("파일 경로가 비어 있습니다.");
        }

        File file = new File(filePath);
        if (file.exists()) {
            if (!file.delete()) {
                throw new RuntimeException("파일 삭제 실패: " + filePath);
            }
        }
    }

}
