package com.agropay.core.images.application.usecase;

import com.agropay.core.images.constant.Bucket;
import com.agropay.core.images.model.SignatureUrlCommand;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface IFileStorageUseCase {
    SignatureUrlCommand getSignature(Bucket bucket);

    Map<String, Object> uploadFile(MultipartFile file, Bucket bucket);
}