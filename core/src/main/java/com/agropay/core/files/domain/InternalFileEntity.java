package com.agropay.core.files.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidad polimórfica para almacenar archivos internos en SQL Server.
 * Permite asociar archivos (PDFs, imágenes, etc.) a diferentes entidades del sistema.
 */
@Entity
@Table(name = "tbl_internal_files", schema = "app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InternalFileEntity extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    // Relación polimórfica (cambiado a String para compatibilidad con IImageable)
    @Column(name = "fileable_id", nullable = false, length = 255)
    private String fileableId;

    @Column(name = "fileable_type", nullable = false, length = 100)
    private String fileableType;

    // Información del archivo
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType; // MIME type

    @Column(name = "file_size", nullable = false)
    private Long fileSize; // Tamaño en bytes

    @Lob
    @Column(name = "file_content", nullable = false)
    private byte[] fileContent;

    // Metadatos adicionales
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "category", length = 50)
    private String category; // Ej: 'CONTRACT', 'PAYSLIP', 'SIGNATURE', etc.
}

