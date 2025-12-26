package com.agropay.core.organization.model.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DistrictDetailResponseDTO {
    private UUID publicId;
    private String name;
    private String ubigeoReniec;
    private String ubigeoInei;
    private UUID provincePublicId;
    private UUID departmentPublicId;
}
