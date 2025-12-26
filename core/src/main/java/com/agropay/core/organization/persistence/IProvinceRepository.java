package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.ProvinceEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IProvinceRepository extends ISoftRepository<ProvinceEntity, Integer>  {
    @Query("SELECT p FROM ProvinceEntity AS p WHERE p.department.publicId=:publicId")
    List<ProvinceEntity> getAllByIdentifier(@Param(value = "publicId") UUID publicIdDepartment);

}
