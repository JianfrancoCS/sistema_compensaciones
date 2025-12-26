package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.HarvestRecordEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IHarvestRecordRepository extends ISoftRepository<HarvestRecordEntity, Long>,
        IFindByPublicIdRepository<HarvestRecordEntity> {

    @Query("SELECT CASE WHEN COUNT(hr) > 0 THEN true ELSE false END FROM HarvestRecordEntity hr " +
           "WHERE hr.qrCode.id = :qrCodeId " +
           "AND hr.deletedAt IS NULL")
    boolean existsByQrCodeId(@Param("qrCodeId") Long qrCodeId);

    Optional<HarvestRecordEntity> findByTemporalIdAndDeletedAtIsNull(String temporalId);

    /**
     * Cuenta los registros de cosecha asociados a un QR Roll
     */
    @Query("SELECT COUNT(hr) FROM HarvestRecordEntity hr " +
           "WHERE hr.qrCode.qrRoll.id = :qrRollId " +
           "AND hr.deletedAt IS NULL")
    Long countByQrRollId(@Param("qrRollId") Integer qrRollId);

    /**
     * Cuenta los registros de cosecha asociados a un QR Roll
     * Usado para calcular productividad al cerrar el tareo
     * (mismo método que countByQrRollId, pero con nombre más descriptivo)
     */
    @Query("SELECT COUNT(hr) FROM HarvestRecordEntity hr " +
           "WHERE hr.qrCode.qrRoll.id = :qrRollId " +
           "AND hr.deletedAt IS NULL")
    Long countByQrRollIdForProductivity(@Param("qrRollId") Integer qrRollId);
}