package com.agropay.core.files.application.usecase;

import com.agropay.core.files.domain.InternalFileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para almacenar y recuperar archivos internos en SQL Server
 */
public interface IInternalFileStorageUseCase {

    /**
     * Guarda un archivo asociado a una entidad
     */
    InternalFileEntity saveFile(IFileable fileable, MultipartFile file, String category, String description);

    /**
     * Guarda un archivo desde bytes
     */
    InternalFileEntity saveFile(IFileable fileable, byte[] fileContent, String fileName, String fileType, String category, String description);

    /**
     * Obtiene un archivo por su publicId
     */
    InternalFileEntity getFile(UUID publicId);

    /**
     * Obtiene todos los archivos de una entidad
     */
    List<InternalFileEntity> getFilesByFileable(IFileable fileable);

    /**
     * Obtiene archivos de una entidad por categoría
     */
    List<InternalFileEntity> getFilesByFileableAndCategory(IFileable fileable, String category);

    /**
     * Elimina (soft delete) todos los archivos de una entidad
     */
    void deleteFilesByFileable(IFileable fileable, String deletedBy);

    /**
     * Elimina (soft delete) un archivo específico
     */
    void deleteFile(UUID publicId, String deletedBy);

    /**
     * Guarda múltiples archivos sin eliminar los anteriores.
     * Útil cuando quieres agregar varias imágenes a una entidad sin reemplazar las existentes.
     */
    List<InternalFileEntity> saveMultipleFiles(IFileable fileable, List<MultipartFile> files, 
                                               String category, String description);

    /**
     * Elimina múltiples archivos por sus publicIds.
     * Útil cuando el usuario elimina varias imágenes antes de guardar.
     */
    void deleteFiles(List<UUID> publicIds, String deletedBy);

    /**
     * Sincroniza la lista de archivos activos para una entidad.
     * 
     * Lógica:
     * - Si activePublicIds es null → no hace nada
     * - Si activePublicIds es vacío [] → elimina todos los archivos de la entidad
     * - Si activePublicIds tiene menos archivos que los actuales → elimina los que faltan
     * 
     * @param fileable La entidad propietaria de los archivos
     * @param activePublicIds Lista de publicIds que deben quedar activos (null = no hacer nada, [] = eliminar todos)
     * @param deletedBy Usuario que realiza la eliminación
     */
    void synchronizeActiveFiles(IFileable fileable, List<UUID> activePublicIds, String deletedBy);
}

