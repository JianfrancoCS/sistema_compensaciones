package com.agropay.core.organization.application.services;

import com.agropay.core.organization.application.usecase.IDepartmentUseCase;
import com.agropay.core.organization.domain.DepartmentEntity;
import com.agropay.core.organization.persistence.IDepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements IDepartmentUseCase {

    private final IDepartmentRepository departmentRepository;

    @Override
    public List<DepartmentEntity> getAll() {
        List<DepartmentEntity> all = departmentRepository.findAll();
        return all;
    }
}
