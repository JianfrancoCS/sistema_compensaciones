package com.agropay.core.images.application.usecase;

import com.agropay.core.images.domain.ImageEntity;

import java.util.List;
import java.util.UUID;

public interface IImageUseCase {
    void attachImage(IImageable imageable, String fileUrl);
    void createImage(IImageable imageable, String fileUrl);
    void createImageWithOrder(IImageable imageable, String fileUrl, Integer orderValue);
    void softDeleteImage(UUID publicId);
    List<ImageEntity> getImagesByImageable(IImageable imageable);
}
