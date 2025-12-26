package com.agropay.core.organization.web;

import com.agropay.core.organization.application.usecase.IDepartmentUseCase;
import com.agropay.core.organization.application.usecase.IDistrictUseCase;
import com.agropay.core.organization.application.usecase.IProvinceUseCase;
import com.agropay.core.organization.model.location.DepartmentResponse;
import com.agropay.core.organization.model.location.DistrictDetailResponseDTO;
import com.agropay.core.organization.model.location.DistrictResponse;
import com.agropay.core.organization.model.location.ProvinceResponse;
import com.agropay.core.organization.mapper.ILocationMapper;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Gestión de Ubicaciones", description = "Operaciones para consultar la información geográfica de Perú (Departamentos, Provincias y Distritos).")
public class LocationController {
    private final IDepartmentUseCase departmentUseCase;
    private final IProvinceUseCase provinceUseCase;
    private final IDistrictUseCase districtUseCase;

    private final ILocationMapper locationMapper;

    @GetMapping("/departments")
    @Operation(summary = "Obtener todos los departamentos", description = "Devuelve una lista completa de todos los departamentos del Perú.")
    public ResponseEntity<ApiResult<List<DepartmentResponse>>> getDepartments() {
        List<DepartmentResponse> collect = locationMapper.toDeparmetResponseList(departmentUseCase.getAll());
        ApiResult<List<DepartmentResponse>> result = ApiResult.success(collect, "Departamentos encontrados exitosamente");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/departments/{departmentId}/provinces")
    @Operation(summary = "Obtener provincias por departamento", description = "Devuelve una lista de todas las provincias que pertenecen a un departamento específico, identificado por su ID público.")
    public ResponseEntity<ApiResult<List<ProvinceResponse>>> getProvincesByDepartmentId(@PathVariable("departmentId") UUID departmentId) {
        List<ProvinceResponse> collect = locationMapper.toProvinceResponseList(provinceUseCase.getAllByIdentifier(departmentId));
        ApiResult<List<ProvinceResponse>> result = ApiResult.success(collect, "Provincias encontradas exitosamente");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/provinces/{provinceId}/districts")
    @Operation(summary = "Obtener distritos por provincia", description = "Devuelve una lista de todos los distritos que pertenecen a una provincia específica, identificada por su ID público.")
    public ResponseEntity<ApiResult<List<DistrictResponse>>> getDistrictsByProvinceId(@PathVariable("provinceId") UUID provinceId) {
        List<DistrictResponse> collect = locationMapper.toDistrictResponseList(districtUseCase.getAllByIdentifier(provinceId));
        ApiResult<List<DistrictResponse>> result = ApiResult.success(collect, "Distritos encontrados exitosamente");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/districts/{districtId}")
    @Operation(summary = "Obtener detalle de un distrito", description = "Devuelve la información detallada de un distrito específico, identificado por su ID público.")
    public ResponseEntity<ApiResult<DistrictDetailResponseDTO>> getDistrictById(@PathVariable("districtId") UUID districtId) {
        DistrictDetailResponseDTO response = districtUseCase.getDetailByPublicId(districtId);
        ApiResult<DistrictDetailResponseDTO> result = ApiResult.success(response, "Distrito encontrado exitosamente");
        return ResponseEntity.ok(result);
    }
}
