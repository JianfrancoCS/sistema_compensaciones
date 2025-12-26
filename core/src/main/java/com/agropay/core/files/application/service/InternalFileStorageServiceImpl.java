package com.agropay.core.files.application.service;

import com.agropay.core.files.application.usecase.IFileable;
import com.agropay.core.files.application.usecase.IInternalFileStorageUseCase;
import com.agropay.core.files.domain.InternalFileEntity;
import com.agropay.core.files.persistence.IInternalFileRepository;
import com.agropay.core.shared.utils.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalFileStorageServiceImpl implements IInternalFileStorageUseCase {

    private final IInternalFileRepository fileRepository;

    @Override
    @Transactional
    public InternalFileEntity saveFile(IFileable fileable, MultipartFile file, String category, String description) {
        try {
            log.info("Guardando archivo {} para entidad {} con ID: {}", 
                    file.getOriginalFilename(), fileable.getSimpleName(), fileable.getId());

            String currentUser = SecurityContextUtils.getCurrentUsername();

            // Soft-delete archivos existentes de la misma categoría (solo si hay categoría)
            // Esto permite reemplazo por categoría: si subes una nueva foto de la misma categoría, elimina la anterior
            if (category != null) {
                List<InternalFileEntity> existingFiles = fileRepository.findByFileableAndCategory(
                        fileable.getId(), fileable.getSimpleName(), category);
                if (!existingFiles.isEmpty()) {
                    // CORREGIDO: Solo eliminar archivos de la categoría específica, no todos
                    fileRepository.softDeleteByFileableAndCategory(
                            fileable.getId(), fileable.getSimpleName(), category, currentUser);
                    log.info("Eliminados {} archivos existentes de la categoría {} para entidad {} con ID {}", 
                            existingFiles.size(), category, fileable.getSimpleName(), fileable.getId());
                }
            }

            // Crear nueva entidad de archivo
            // Nota: createdBy y updatedBy se manejan automáticamente por JPA Auditing (@CreatedBy, @LastModifiedBy)
            InternalFileEntity newFile = new InternalFileEntity();
            newFile.setPublicId(UUID.randomUUID());
            newFile.setFileableId(fileable.getId());
            newFile.setFileableType(fileable.getSimpleName());
            newFile.setFileName(file.getOriginalFilename());
            newFile.setFileType(file.getContentType());
            newFile.setFileSize(file.getSize());
            newFile.setFileContent(file.getBytes());
            newFile.setCategory(category);
            newFile.setDescription(description);
            // createdBy se establece automáticamente por @CreatedBy en AbstractEntity

            InternalFileEntity saved = fileRepository.save(newFile);
            log.info("Archivo guardado exitosamente con publicId: {}", saved.getPublicId());

            return saved;

        } catch (Exception e) {
            log.error("Error guardando archivo para entidad {} con ID: {}", 
                    fileable.getSimpleName(), fileable.getId(), e);
            throw new RuntimeException("Error guardando archivo: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public InternalFileEntity saveFile(IFileable fileable, byte[] fileContent, String fileName, 
                                      String fileType, String category, String description) {
        try {
            log.info("Guardando archivo {} ({} bytes) para entidad {} con ID: {}", 
                    fileName, fileContent.length, fileable.getSimpleName(), fileable.getId());

            String currentUser = SecurityContextUtils.getCurrentUsername();

            // Soft-delete archivos existentes de la misma categoría (solo si hay categoría)
            // Esto permite reemplazo por categoría: si subes una nueva foto de la misma categoría, elimina la anterior
            if (category != null) {
                List<InternalFileEntity> existingFiles = fileRepository.findByFileableAndCategory(
                        fileable.getId(), fileable.getSimpleName(), category);
                if (!existingFiles.isEmpty()) {
                    // CORREGIDO: Solo eliminar archivos de la categoría específica, no todos
                    fileRepository.softDeleteByFileableAndCategory(
                            fileable.getId(), fileable.getSimpleName(), category, currentUser);
                    log.info("Eliminados {} archivos existentes de la categoría {} para entidad {} con ID {}", 
                            existingFiles.size(), category, fileable.getSimpleName(), fileable.getId());
                }
            }

            // Crear nueva entidad de archivo
            // Nota: createdBy y updatedBy se manejan automáticamente por JPA Auditing (@CreatedBy, @LastModifiedBy)
            InternalFileEntity newFile = new InternalFileEntity();
            newFile.setPublicId(UUID.randomUUID());
            newFile.setFileableId(fileable.getId());
            newFile.setFileableType(fileable.getSimpleName());
            newFile.setFileName(fileName);
            newFile.setFileType(fileType);
            newFile.setFileSize((long) fileContent.length);
            newFile.setFileContent(fileContent);
            newFile.setCategory(category);
            newFile.setDescription(description);
            // createdBy se establece automáticamente por @CreatedBy en AbstractEntity

            InternalFileEntity saved = fileRepository.save(newFile);
            log.info("Archivo guardado exitosamente con publicId: {}", saved.getPublicId());

            return saved;

        } catch (Exception e) {
            log.error("Error guardando archivo para entidad {} con ID: {}", 
                    fileable.getSimpleName(), fileable.getId(), e);
            throw new RuntimeException("Error guardando archivo: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InternalFileEntity getFile(UUID publicId) {
        log.info("Obteniendo archivo con publicId: {}", publicId);
        return fileRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado con publicId: " + publicId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternalFileEntity> getFilesByFileable(IFileable fileable) {
        log.info("Obteniendo archivos para entidad {} con ID: {}", 
                fileable.getSimpleName(), fileable.getId());
        return fileRepository.findByFileableIdAndFileableTypeAndDeletedAtIsNull(
                fileable.getId(), fileable.getSimpleName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternalFileEntity> getFilesByFileableAndCategory(IFileable fileable, String category) {
        log.info("Obteniendo archivos de categoría {} para entidad {} con ID: {}", 
                category, fileable.getSimpleName(), fileable.getId());
        return fileRepository.findByFileableAndCategory(
                fileable.getId(), fileable.getSimpleName(), category);
    }

    @Override
    @Transactional
    public void deleteFilesByFileable(IFileable fileable, String deletedBy) {
        log.info("Eliminando archivos para entidad {} con ID: {}", 
                fileable.getSimpleName(), fileable.getId());
        fileRepository.softDeleteByFileable(fileable.getId(), fileable.getSimpleName(), deletedBy);
    }

    @Override
    @Transactional
    public void deleteFile(UUID publicId, String deletedBy) {
        log.info("Eliminando archivo con publicId: {}", publicId);
        InternalFileEntity file = getFile(publicId);
        file.setDeletedAt(java.time.LocalDateTime.now());
        file.setDeletedBy(deletedBy);
        fileRepository.save(file);
    }

    /**
     * Guarda múltiples archivos sin eliminar los anteriores.
     * Útil cuando quieres agregar varias imágenes a una entidad sin reemplazar las existentes.
     */
    @Override
    @Transactional
    public List<InternalFileEntity> saveMultipleFiles(IFileable fileable, List<MultipartFile> files, 
                                                      String category, String description) {
        try {
            log.info("Guardando {} archivos para entidad {} con ID: {}", 
                    files.size(), fileable.getSimpleName(), fileable.getId());

            List<InternalFileEntity> savedFiles = new java.util.ArrayList<>();
            
            for (MultipartFile file : files) {
                InternalFileEntity newFile = new InternalFileEntity();
                newFile.setPublicId(UUID.randomUUID());
                newFile.setFileableId(fileable.getId());
                newFile.setFileableType(fileable.getSimpleName());
                newFile.setFileName(file.getOriginalFilename());
                newFile.setFileType(file.getContentType());
                newFile.setFileSize(file.getSize());
                newFile.setFileContent(file.getBytes());
                newFile.setCategory(category);
                newFile.setDescription(description);
                
                InternalFileEntity saved = fileRepository.save(newFile);
                savedFiles.add(saved);
                log.info("Archivo guardado exitosamente con publicId: {}", saved.getPublicId());
            }
            
            log.info("Guardados {} archivos exitosamente para entidad {} con ID: {}", 
                    savedFiles.size(), fileable.getSimpleName(), fileable.getId());
            
            return savedFiles;
            
        } catch (Exception e) {
            log.error("Error guardando múltiples archivos para entidad {} con ID: {}", 
                    fileable.getSimpleName(), fileable.getId(), e);
            throw new RuntimeException("Error guardando múltiples archivos: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina múltiples archivos por sus publicIds.
     * Útil cuando el usuario elimina varias imágenes antes de guardar.
     */
    @Override
    @Transactional
    public void deleteFiles(List<UUID> publicIds, String deletedBy) {
        log.info("Eliminando {} archivos", publicIds.size());
        
        if (publicIds.isEmpty()) {
            return;
        }
        
        fileRepository.softDeleteByPublicIds(publicIds, deletedBy);
        log.info("Eliminados {} archivos exitosamente", publicIds.size());
    }

    /**
     * Sincroniza la lista de archivos activos para una entidad.
     * 
     * Lógica:
     * - Si activePublicIds es null → no hace nada
     * - Si activePublicIds es vacío [] → elimina todos los archivos de la entidad
     * - Si activePublicIds tiene menos archivos que los actuales → elimina los que faltan
     */
    @Override
    @Transactional
    public void synchronizeActiveFiles(IFileable fileable, List<UUID> activePublicIds, String deletedBy) {
        log.info("Sincronizando archivos activos para entidad {} con ID: {}. Archivos activos: {}", 
                fileable.getSimpleName(), fileable.getId(), 
                activePublicIds == null ? "null (no hacer nada)" : activePublicIds.size());

        // Si es null, no hacer nada
        if (activePublicIds == null) {
            log.info("Lista de archivos activos es null, no se realiza ninguna acción");
            return;
        }

        // Obtener todos los archivos actuales de la entidad (sin deleted_at)
        List<InternalFileEntity> currentFiles = fileRepository.findByFileableIdAndFileableTypeAndDeletedAtIsNull(
                fileable.getId(), fileable.getSimpleName());

        // Si la lista está vacía, eliminar todos los archivos
        if (activePublicIds.isEmpty()) {
            if (!currentFiles.isEmpty()) {
                log.info("Lista vacía recibida, eliminando todos los {} archivos de la entidad", currentFiles.size());
                fileRepository.softDeleteByFileable(fileable.getId(), fileable.getSimpleName(), deletedBy);
            } else {
                log.info("Lista vacía recibida pero no hay archivos para eliminar");
            }
            return;
        }

        // Obtener los publicIds de los archivos actuales
        List<UUID> currentPublicIds = currentFiles.stream()
                .map(InternalFileEntity::getPublicId)
                .toList();

        // Encontrar los archivos que deben eliminarse (están en BD pero no en la lista activa)
        List<UUID> filesToDelete = currentPublicIds.stream()
                .filter(publicId -> !activePublicIds.contains(publicId))
                .toList();

        // Si hay archivos para eliminar, eliminarlos
        if (!filesToDelete.isEmpty()) {
            log.info("Eliminando {} archivos que no están en la lista activa. Archivos a eliminar: {}", 
                    filesToDelete.size(), filesToDelete);
            fileRepository.softDeleteByPublicIds(filesToDelete, deletedBy);
        } else {
            log.info("No hay archivos para eliminar, todos los archivos actuales están en la lista activa");
        }

        // Verificar si hay publicIds en la lista activa que no existen en BD
        List<UUID> missingPublicIds = activePublicIds.stream()
                .filter(publicId -> !currentPublicIds.contains(publicId))
                .toList();

        if (!missingPublicIds.isEmpty()) {
            log.warn("La lista activa contiene {} publicIds que no existen en BD: {}. " +
                    "Estos archivos deben crearse primero antes de sincronizar.", 
                    missingPublicIds.size(), missingPublicIds);
        }
    }
}

