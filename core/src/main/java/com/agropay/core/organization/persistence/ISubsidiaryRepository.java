package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.organization.model.subsidiary.SubsidiaryDetailsDTO;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ISubsidiaryRepository extends
        ISoftRepository<SubsidiaryEntity, Short>,
        IFindByPublicIdRepository<SubsidiaryEntity>,
        JpaSpecificationExecutor<SubsidiaryEntity> {

    Optional<SubsidiaryEntity> findByNameIgnoreCase(String name);

    @Query("""
        SELECT new com.agropay.core.organization.model.subsidiary.SubsidiaryDetailsDTO(
            s.publicId,
            s.name,
            s.district.publicId,
            s.createdAt,
            s.updatedAt,
            COUNT(e.personDocumentNumber)
        )
        FROM SubsidiaryEntity s
        LEFT JOIN EmployeeEntity e ON e.subsidiary.id = s.id AND e.deletedAt IS NULL
        WHERE s.publicId = :publicId AND s.deletedAt IS NULL
        GROUP BY s.publicId, s.name, s.district.publicId, s.createdAt, s.updatedAt
    """)
    Optional<SubsidiaryDetailsDTO> findDetailsByPublicId(@Param("publicId") UUID publicId);

}
