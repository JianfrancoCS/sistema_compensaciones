package com.agropay.core.files.web;

import com.agropay.core.files.application.usecase.IInternalFileStorageUseCase;
import com.agropay.core.files.domain.InternalFileEntity;
import com.agropay.core.files.model.InternalFileDTO;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.SecurityContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/internal-files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestión de Archivos Internos", description = "API para subir, descargar y gestionar archivos internos almacenados en SQL Server")
public class InternalFileController {

    private final IInternalFileStorageUseCase fileStorageService;

    @Operation(summary = "Subir archivo",
               description = "Sube un archivo asociado a una entidad del sistema. " +
                           "Si se proporciona activePublicIds, sincroniza la lista de archivos activos después de subir. " +
                           "Si category está presente y hay archivos existentes de esa categoría, los reemplaza.")
    @PostMapping("/upload")
    public ResponseEntity<ApiResult<InternalFileDTO>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileableId") String fileableId,
            @RequestParam("fileableType") String fileableType,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "activePublicIds", required = false) List<UUID> activePublicIds) {
        
        log.info("Solicitud REST para subir archivo: {} para entidad {} con ID: {}. Sincronizar activos: {}", 
                file.getOriginalFilename(), fileableType, fileableId, activePublicIds != null);

        try {
            // Crear objeto IFileable temporal
            IFileableWrapper fileable = new IFileableWrapper(fileableId, fileableType);
            
            // Subir el archivo (si hay categoría, reemplaza los anteriores de esa categoría)
            InternalFileEntity savedFile = fileStorageService.saveFile(fileable, file, category, description);
            
            // Si se proporciona activePublicIds, sincronizar la lista de archivos activos
            // Esto permite que el frontend envíe la lista completa de archivos que deben quedar activos
            if (activePublicIds != null) {
                String currentUser = SecurityContextUtils.getCurrentUsername();
                // Agregar el archivo recién subido a la lista de activos si no está
                if (!activePublicIds.contains(savedFile.getPublicId())) {
                    activePublicIds = new java.util.ArrayList<>(activePublicIds);
                    activePublicIds.add(savedFile.getPublicId());
                }
                fileStorageService.synchronizeActiveFiles(fileable, activePublicIds, currentUser);
                log.info("Archivos sincronizados después de subir. Archivos activos: {}", activePublicIds.size());
            }
            
            InternalFileDTO dto = mapToDTO(savedFile);
            
            return ResponseEntity.ok(ApiResult.success(dto, "Archivo subido exitosamente"));
            
        } catch (Exception e) {
            log.error("Error subiendo archivo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("Error subiendo archivo: " + e.getMessage()));
        }
    }

    @Operation(summary = "Descargar archivo",
               description = "Descarga un archivo por su publicId con caché HTTP y validación ETag (como Laravel Storage::response())")
    @GetMapping("/{publicId}/download")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable UUID publicId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        
        try {
            InternalFileEntity file = fileStorageService.getFile(publicId);
            
            // Generar ETag (similar a cómo Laravel genera ETags)
            String etag = "\"" + file.getPublicId() + "-" + 
                         (file.getUpdatedAt() != null ? file.getUpdatedAt().hashCode() : file.getCreatedAt().hashCode()) + "\"";
            
            // Validar ETag: si el cliente ya tiene la versión cacheada, responder 304 Not Modified
            // Esto es clave para evitar múltiples descargas (como hace Laravel)
            if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
                log.debug("Cliente tiene versión cacheada del archivo con publicId: {}", publicId);
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .header("ETag", etag)
                        .build();
            }
            
            log.info("Sirviendo archivo con publicId: {}", publicId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(file.getFileType()));
            
            // Para imágenes, usar "inline" en lugar de "attachment" para que se muestren directamente
            String contentDisposition = file.getFileType().startsWith("image/") 
                ? "inline" 
                : "attachment";
            headers.setContentDispositionFormData(contentDisposition, file.getFileName());
            headers.setContentLength(file.getFileSize());
            
            // Headers de caché HTTP (similar a Laravel Storage::response())
            // Cache-Control: public permite que proxies y CDNs cacheen
            // max-age=31536000 = 1 año (las imágenes no cambian frecuentemente)
            headers.setCacheControl("public, max-age=31536000, immutable");
            headers.setETag(etag);
            
            // Last-Modified para validación de caché
            if (file.getUpdatedAt() != null) {
                headers.setLastModified(java.time.Instant.ofEpochMilli(
                    java.sql.Timestamp.valueOf(file.getUpdatedAt()).getTime()));
            } else if (file.getCreatedAt() != null) {
                headers.setLastModified(java.time.Instant.ofEpochMilli(
                    java.sql.Timestamp.valueOf(file.getCreatedAt()).getTime()));
            }
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(file.getFileContent());
                    
        } catch (Exception e) {
            log.error("Error descargando archivo con publicId: {}", publicId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Obtener archivo (metadatos)",
               description = "Obtiene los metadatos de un archivo sin descargar el contenido")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResult<InternalFileDTO>> getFile(@PathVariable UUID publicId) {
        log.info("Solicitud REST para obtener archivo con publicId: {}", publicId);

        try {
            InternalFileEntity file = fileStorageService.getFile(publicId);
            InternalFileDTO dto = mapToDTO(file);
            
            return ResponseEntity.ok(ApiResult.success(dto, "Archivo obtenido exitosamente"));
            
        } catch (Exception e) {
            log.error("Error obteniendo archivo con publicId: {}", publicId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResult.failure("Archivo no encontrado"));
        }
    }

    @Operation(summary = "Listar archivos de una entidad",
               description = "Obtiene todos los archivos asociados a una entidad")
    @GetMapping("/fileable/{fileableType}/{fileableId}")
    public ResponseEntity<ApiResult<List<InternalFileDTO>>> getFilesByFileable(
            @PathVariable String fileableType,
            @PathVariable String fileableId,
            @RequestParam(value = "category", required = false) String category) {
        
        log.info("Solicitud REST para obtener archivos de entidad {} con ID: {}", fileableType, fileableId);

        try {
            IFileableWrapper fileable = new IFileableWrapper(fileableId, fileableType);
            
            List<InternalFileEntity> files;
            if (category != null) {
                files = fileStorageService.getFilesByFileableAndCategory(fileable, category);
            } else {
                files = fileStorageService.getFilesByFileable(fileable);
            }
            
            List<InternalFileDTO> dtos = files.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResult.success(dtos, "Archivos obtenidos exitosamente"));
            
        } catch (Exception e) {
            log.error("Error obteniendo archivos de entidad {} con ID: {}", fileableType, fileableId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("Error obteniendo archivos: " + e.getMessage()));
        }
    }

    @Operation(summary = "Subir múltiples archivos",
               description = "Sube múltiples archivos asociados a una entidad sin eliminar los anteriores. " +
                           "Si se proporciona activePublicIds, sincroniza la lista de archivos activos después de subir.")
    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResult<List<InternalFileDTO>>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("fileableId") String fileableId,
            @RequestParam("fileableType") String fileableType,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "activePublicIds", required = false) List<UUID> activePublicIds) {
        
        log.info("Solicitud REST para subir {} archivos para entidad {} con ID: {}. Sincronizar activos: {}", 
                files.size(), fileableType, fileableId, activePublicIds != null);

        try {
            IFileableWrapper fileable = new IFileableWrapper(fileableId, fileableType);
            
            // Subir múltiples archivos (no elimina los anteriores)
            List<InternalFileEntity> savedFiles = fileStorageService.saveMultipleFiles(
                    fileable, files, category, description);
            
            // Si se proporciona activePublicIds, sincronizar la lista de archivos activos
            if (activePublicIds != null) {
                String currentUser = SecurityContextUtils.getCurrentUsername();
                // Agregar los archivos recién subidos a la lista de activos
                List<UUID> updatedActiveIds = new java.util.ArrayList<>(activePublicIds);
                savedFiles.forEach(saved -> {
                    if (!updatedActiveIds.contains(saved.getPublicId())) {
                        updatedActiveIds.add(saved.getPublicId());
                    }
                });
                fileStorageService.synchronizeActiveFiles(fileable, updatedActiveIds, currentUser);
                log.info("Archivos sincronizados después de subir múltiples. Archivos activos: {}", updatedActiveIds.size());
            }
            
            List<InternalFileDTO> dtos = savedFiles.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResult.success(dtos, "Archivos subidos exitosamente"));
            
        } catch (Exception e) {
            log.error("Error subiendo múltiples archivos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("Error subiendo archivos: " + e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar archivo",
               description = "Elimina un archivo mediante soft delete")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResult<Void>> deleteFile(@PathVariable UUID publicId) {
        log.info("Solicitud REST para eliminar archivo con publicId: {}", publicId);

        try {
            String currentUser = SecurityContextUtils.getCurrentUsername();
            fileStorageService.deleteFile(publicId, currentUser);
            
            return ResponseEntity.ok(ApiResult.success(null, "Archivo eliminado exitosamente"));
            
        } catch (Exception e) {
            log.error("Error eliminando archivo con publicId: {}", publicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("Error eliminando archivo: " + e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar múltiples archivos",
               description = "Elimina múltiples archivos mediante soft delete por sus publicIds")
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResult<Void>> deleteFiles(@RequestBody List<UUID> publicIds) {
        log.info("Solicitud REST para eliminar {} archivos", publicIds.size());

        try {
            String currentUser = SecurityContextUtils.getCurrentUsername();
            fileStorageService.deleteFiles(publicIds, currentUser);
            
            return ResponseEntity.ok(ApiResult.success(null, "Archivos eliminados exitosamente"));
            
        } catch (Exception e) {
            log.error("Error eliminando múltiples archivos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("Error eliminando archivos: " + e.getMessage()));
        }
    }

    @Operation(summary = "Sincronizar archivos activos",
               description = "Sincroniza la lista de archivos activos para una entidad. " +
                             "Si activePublicIds es null, no hace nada. " +
                             "Si es lista vacía [], elimina todos. " +
                             "Si tiene menos archivos que los actuales, elimina los que faltan.")
    @PostMapping("/synchronize")
    public ResponseEntity<ApiResult<Void>> synchronizeActiveFiles(
            @RequestParam("fileableId") String fileableId,
            @RequestParam("fileableType") String fileableType,
            @RequestParam(value = "activePublicIds", required = false) List<UUID> activePublicIds) {
        
        log.info("Solicitud REST para sincronizar archivos activos para entidad {} con ID: {}", 
                fileableType, fileableId);

        try {
            IFileableWrapper fileable = new IFileableWrapper(fileableId, fileableType);
            String currentUser = SecurityContextUtils.getCurrentUsername();
            
            fileStorageService.synchronizeActiveFiles(fileable, activePublicIds, currentUser);
            
            return ResponseEntity.ok(ApiResult.success(null, "Archivos sincronizados exitosamente"));
            
        } catch (Exception e) {
            log.error("Error sincronizando archivos activos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("Error sincronizando archivos: " + e.getMessage()));
        }
    }

    private InternalFileDTO mapToDTO(InternalFileEntity file) {
        String downloadUrl = "/v1/internal-files/" + file.getPublicId() + "/download";
        
        return InternalFileDTO.builder()
                .publicId(file.getPublicId())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .category(file.getCategory())
                .description(file.getDescription())
                .createdAt(file.getCreatedAt())
                .downloadUrl(downloadUrl)
                .build();
    }

    // Wrapper temporal para IFileable
    private static class IFileableWrapper implements com.agropay.core.files.application.usecase.IFileable {
        private final String id;
        private final String simpleName;

        public IFileableWrapper(String id, String simpleName) {
            this.id = id;
            this.simpleName = simpleName;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getSimpleName() {
            return simpleName;
        }
    }
}

