package com.agropay.core.images.application.service;

import com.agropay.core.images.application.usecase.IImageableUseCase;
import com.agropay.core.images.domain.ImageEntity;
import com.agropay.core.images.model.CreateImageCommand;
import com.agropay.core.images.model.ImageDTO;
import com.agropay.core.images.persistence.ImageRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ImageService implements IImageableUseCase {

    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Set<ImageDTO> getImages(Long ownerId, String owner) {
        return imageRepository.findByImageableIdAndImageableType(String.valueOf(ownerId), owner)
                .stream()
                .map(image -> new ImageDTO(image.getId(), image.getUrl()))
                .collect(Collectors.toSet());
    }

    @Override
    public ImageDTO createImage(CreateImageCommand command, String owner) {
        ImageEntity imageEntity = new ImageEntity();
        // Convertir Long a String para compatibilidad con el nuevo tipo de imageableId
        imageEntity.setImageableId(String.valueOf(command.getImageableId()));
        imageEntity.setImageableType(command.getImageableType());
        imageEntity.setUrl(command.getUrl());

        imageEntity = imageRepository.save(imageEntity);

        return new ImageDTO(imageEntity.getId(), imageEntity.getUrl());
    }

    @Override
    public void deleteImage(Long id, String owner) {
        // In a real scenario, you would get the user from the security context:
        // String deletedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        String deletedBy = "system"; // Placeholder
        imageRepository.softDelete(id, deletedBy);
    }
}
