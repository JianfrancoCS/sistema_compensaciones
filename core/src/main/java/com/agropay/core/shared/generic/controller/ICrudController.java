package com.agropay.core.shared.generic.controller;

import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface ICrudController<Detail_dto,list_dto,create_dto,update_dto,response_dto>{
    @GetMapping()
    ResponseEntity<ApiResult<PagedResult<list_dto>>> getPagedList(
            @RequestParam String query,
            @RequestParam String sortDirection,
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam Integer pageSize
    );

    @GetMapping(value = "/{publicId}")
    ResponseEntity<ApiResult<Detail_dto>> findById(@PathVariable(name = "publicId") UUID publicId);

    @PostMapping
    ResponseEntity<ApiResult<response_dto>> create(@RequestBody @Valid create_dto request);

    @PutMapping("/{publicId}")
    ResponseEntity<ApiResult<response_dto>> update(
            @RequestBody @Valid update_dto request,
            @PathVariable("publicId") UUID publicId
    );

    @DeleteMapping("/{publicId}")
    ResponseEntity<ApiResult<Void>> delete(@PathVariable("publicId") UUID publicId);

}
