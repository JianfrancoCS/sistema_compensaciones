package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.DistrictEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IDistrictRepository extends
        ISoftRepository<DistrictEntity, Integer>,
        IFindByPublicIdRepository<DistrictEntity>
{
    @Query("SELECT d FROM DistrictEntity AS d WHERE d.province.publicId=:publicId")
    List<DistrictEntity> getAllByIdentifier(@Param(value = "publicId") UUID publicIdProvince);

    Optional<DistrictEntity> getByPublicId(@Param(value = "publicId") UUID publicId);

}
