package com.agropay.core.organization.mapper;

import com.agropay.core.organization.domain.DistrictEntity;
import com.agropay.core.organization.domain.DepartmentEntity;
import com.agropay.core.organization.domain.ProvinceEntity;
import com.agropay.core.organization.model.location.DepartmentResponse;
import com.agropay.core.organization.model.location.DistrictDetailResponseDTO;
import com.agropay.core.organization.model.location.DistrictResponse;
import com.agropay.core.organization.model.location.ProvinceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ILocationMapper {

    public DepartmentResponse toResponse(DepartmentEntity department);
    public List<DepartmentResponse> toDeparmetResponseList(List<DepartmentEntity> departments);


    public ProvinceResponse toResponse(ProvinceEntity province);
    public List<ProvinceResponse> toProvinceResponseList(List<ProvinceEntity> provinces);

    public DistrictResponse toResponse(DistrictEntity district);
    public List<DistrictResponse> toDistrictResponseList(List<DistrictEntity> districts);

    @Mapping(source = "publicId", target = "publicId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "ubigeoReniec", target = "ubigeoReniec")
    @Mapping(source = "ubigeoInei", target = "ubigeoInei")
    @Mapping(source = "province.publicId", target = "provincePublicId")
    @Mapping(source = "province.department.publicId", target = "departmentPublicId")
    public DistrictDetailResponseDTO toDistrictDetailResponse(DistrictEntity district);
}
