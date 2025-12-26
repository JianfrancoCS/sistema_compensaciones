package com.agropay.core.images.application.service;

import com.agropay.core.images.application.usecase.IFileStorageUseCase;
import com.agropay.core.images.constant.Bucket;
import com.agropay.core.images.model.SignatureUrlCommand;
import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
public class FileStorageServiceImpl implements IFileStorageUseCase {

    private final Cloudinary cloudinary;

    public FileStorageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public SignatureUrlCommand getSignature(Bucket bucket) {
        long timestamp = System.currentTimeMillis() / 1000L;

        Map<String, Object> paramsToSign = new TreeMap<>();
        paramsToSign.put("timestamp", timestamp);

        if (bucket != null) {
            paramsToSign.put("folder", bucket.getFolderName());
        }

        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinary.config.apiSecret);

        String uploadUrl = String.format(
                "https://api.cloudinary.com/v1_1/%s/image/upload",
                cloudinary.config.cloudName
        );

        return SignatureUrlCommand.builder()
                .uploadUrl(uploadUrl)
                .apiKey(cloudinary.config.apiKey)
                .timestamp(timestamp)
                .signature(signature)
                .folder(bucket.getFolderName())
                .build();
    }

    @Override
    public Map<String, Object> uploadFile(MultipartFile file, Bucket bucket) {
        try {
            log.info("Uploading file: {} to folder: {}", file.getOriginalFilename(), bucket.getFolderName());

            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("folder", bucket.getFolderName());
            uploadOptions.put("use_filename", true);
            uploadOptions.put("unique_filename", true);
            uploadOptions.put("overwrite", true);
            uploadOptions.put("resource_type", "auto");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);

            log.info("Upload successful. Public ID: {}", uploadResult.get("public_id"));

            return uploadResult;

        } catch (Exception e) {
            log.error("Error uploading to Cloudinary", e);
            throw new RuntimeException("Error uploading file: " + e.getMessage(), e);
        }
    }
}