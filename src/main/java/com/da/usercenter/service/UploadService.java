package com.da.usercenter.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UploadService {
    List<String> uploadImg(MultipartFile[] files, Long id) throws IOException;
}
