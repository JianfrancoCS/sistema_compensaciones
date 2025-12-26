package com.agropay.core.hiring.model.contract;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateUploadUrlRequest {

    @NotBlank(message = "El nombre del archivo no puede estar vacío")
    private String fileName;
}
