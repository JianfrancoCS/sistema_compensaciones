package com.agropay.core.images.web;

import com.agropay.core.images.application.usecase.IFileStorageUseCase;
import com.agropay.core.images.application.usecase.IImageUseCase;
import com.agropay.core.images.constant.Bucket;
import com.agropay.core.images.model.SignatureUrlCommand;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestión de Imágenes", description = "API para la gestión de URLs firmadas y eliminación de imágenes")
public class UploadController {

    private final IFileStorageUseCase fileStorageService;
    private final IImageUseCase imageUseCase;

    @Operation(summary = "Obtener URL de subida firmada",
               description = "Genera una URL firmada para subir archivos directamente al almacenamiento en la nube")
    @GetMapping("/upload/signature")
    public ResponseEntity<ApiResult<SignatureUrlCommand>> getUploadSignature(@RequestParam Bucket bucket) {
        log.info("Solicitud REST para obtener URL de subida firmada para bucket: {}", bucket);

        SignatureUrlCommand result = fileStorageService.getSignature(bucket);

        return ResponseEntity.ok(ApiResult.success(result, "URL de subida generada exitosamente"));
    }

    @Operation(summary = "Eliminar una imagen",
               description = "Elimina permanentemente una imagen del sistema mediante soft delete")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResult<Void>> deleteImage(@PathVariable UUID publicId) {
        log.info("Solicitud REST para eliminar imagen con publicId: {}", publicId);

        imageUseCase.softDeleteImage(publicId);

        return ResponseEntity.ok(ApiResult.success(null, "Imagen eliminada exitosamente"));
    }
}