package com.agropay.core.images.application.usecase;

import com.agropay.core.images.model.CreateImageCommand;
import com.agropay.core.images.model.ImageDTO;

import java.util.Set;

public interface IImageableUseCase {
    Set<ImageDTO> getImages(final Long ownerId, final String owner);
    ImageDTO createImage(final CreateImageCommand command, final String owner);
    void deleteImage(final Long id, final String owner);
}
