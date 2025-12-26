package com.agropay.core.hiring.model.addendum;

import jakarta.validation.constraints.NotBlank;

public record GenerateUploadUrlRequest(
    @NotBlank(message = "El nombre del archivo no puede estar vacío")
    String fileName
) {}