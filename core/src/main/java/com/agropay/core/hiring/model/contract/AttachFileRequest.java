package com.agropay.core.hiring.model.contract;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttachFileRequest {
    @NotEmpty(message = "La lista de URLs no puede estar vacía")
    private List<String> imagesUri; // Las imágenes van en orden: [0]=orden 1, [1]=orden 2, etc.
}
