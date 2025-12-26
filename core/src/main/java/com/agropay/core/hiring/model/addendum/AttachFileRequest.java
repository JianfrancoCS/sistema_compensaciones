package com.agropay.core.hiring.model.addendum;

import jakarta.validation.constraints.NotBlank;

public record AttachFileRequest(
    @NotBlank(message = "La URL del archivo no puede estar vacía")
    String fileUrl
) {}