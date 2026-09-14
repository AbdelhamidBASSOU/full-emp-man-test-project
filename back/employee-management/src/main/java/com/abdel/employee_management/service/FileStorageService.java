package com.abdel.employee_management.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {

    String uploadFile(MultipartFile file, String objectKey);

    InputStream getFile(String objectKey);

    void deleteFile(String objectKey);
}