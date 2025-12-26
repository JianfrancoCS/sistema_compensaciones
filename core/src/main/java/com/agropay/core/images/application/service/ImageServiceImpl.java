package com.agropay.core.images.application.service;

import com.agropay.core.images.application.usecase.IImageUseCase;
import com.agropay.core.images.application.usecase.IImageable;
import com.agropay.core.images.domain.ImageEntity;
import com.agropay.core.images.persistence.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements IImageUseCase {

    private final ImageRepository imageRepository;

    @Override
    @Transactional
    public void attachImage(IImageable imageable, String fileUrl) {
        log.info("Attaching file {} to imageable entity: {} with ID: {}", fileUrl, imageable.getSimpleName(), imageable.getId());

        // Soft-delete existing images for this imageable entity
        // El repositorio ya filtra solo imágenes activas (deletedAt IS NULL)
        List<ImageEntity> existingImages = imageRepository.findByImageableIdAndImageableType(
                imageable.getId(),
                imageable.getSimpleName()
        );
        if (!existingImages.isEmpty()) {
            // Hacer soft delete de las imágenes existentes
            // Si alguna ya fue eliminada entre la consulta y ahora, se manejará silenciosamente
            int deletedCount = 0;
            for (ImageEntity image : existingImages) {
                try {
                    imageRepository.softDelete(image.getId(), "system");
                    deletedCount++;
                    log.debug("Soft-deleted image with ID {} for imageable entity {} with ID {}", 
                            image.getId(), imageable.getSimpleName(), imageable.getId());
                } catch (Exception e) {
                    // Si la imagen ya fue eliminada o no existe, simplemente continuamos
                    // Esto puede pasar en casos de concurrencia, no es un error crítico
                    log.debug("Image with ID {} already deleted or not found, skipping: {}", image.getId(), e.getMessage());
                }
            }
            if (deletedCount > 0) {
                log.info("Soft-deleted {} existing images for imageable entity {} with ID {}", 
                        deletedCount, imageable.getSimpleName(), imageable.getId());
            }
        }

        // Create new ImageEntity
        ImageEntity newImage = new ImageEntity();
        newImage.setPublicId(UUID.randomUUID());
        newImage.setImageableId(imageable.getId());
        newImage.setImageableType(imageable.getSimpleName());
        newImage.setUrl(fileUrl);
        newImage.setOrderValue(1); // Default order for single image replacement
        imageRepository.save(newImage);
        log.info("Attached new file {} to imageable entity {} with ID {}", fileUrl, imageable.getSimpleName(), imageable.getId());
    }

    @Override
    @Transactional
    public void createImage(IImageable imageable, String fileUrl) {
        createImageWithOrder(imageable, fileUrl, 1);
    }

    @Transactional
    public void createImageWithOrder(IImageable imageable, String fileUrl, Integer orderValue) {
        log.info("Creating new image {} for imageable entity: {} with ID: {}, order: {}", fileUrl, imageable.getSimpleName(), imageable.getId(), orderValue);
        ImageEntity newImage = new ImageEntity();
        newImage.setPublicId(UUID.randomUUID());
        newImage.setImageableId(imageable.getId());
        newImage.setImageableType(imageable.getSimpleName());
        newImage.setUrl(fileUrl);
        newImage.setOrderValue(orderValue);
        imageRepository.save(newImage);
        log.info("Created new image {} for imageable entity {} with ID {}, order: {}", fileUrl, imageable.getSimpleName(), imageable.getId(), orderValue);
    }

    @Override
    @Transactional
    public void softDeleteImage(UUID publicId) {
        log.info("Attempting to soft delete image with publicId: {}", publicId);
        imageRepository.findByPublicId(publicId).ifPresent(image -> {
            imageRepository.softDelete(image.getId(), "system"); // Assuming 'system' as the deletedBy user
            log.info("Soft-deleted image with publicId: {}", publicId);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageEntity> getImagesByImageable(IImageable imageable) {
        log.info("Fetching images for imageable entity: {} with ID: {}", imageable.getSimpleName(), imageable.getId());
        return imageRepository.findByImageableIdAndImageableType(imageable.getId(), imageable.getSimpleName());
    }
}
