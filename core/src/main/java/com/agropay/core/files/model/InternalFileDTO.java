package com.agropay.core.files.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalFileDTO {
    private UUID publicId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String category;
    private String description;
    private LocalDateTime createdAt;
    private String downloadUrl; // URL para descargar el archivo
}

