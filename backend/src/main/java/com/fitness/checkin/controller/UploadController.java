package com.fitness.checkin.controller;

import com.fitness.checkin.common.BusinessException;
import com.fitness.checkin.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url-prefix}")
    private String urlPrefix;

    /** 图片上传,返回可访问 URL */
    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件为空");
        }
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains(".."))
                ? "" : (original != null && original.contains(".")
                ? original.substring(original.lastIndexOf(".")) : ".jpg");
        // 按日期分目录
        String datePath = LocalDate.now().toString();
        File dir = new File(uploadDir, datePath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("创建上传目录失败");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        File dest = new File(dir, filename);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败");
        }
        String url = urlPrefix + "/" + datePath + "/" + filename;
        return Result.success(Map.of("url", url));
    }
}
