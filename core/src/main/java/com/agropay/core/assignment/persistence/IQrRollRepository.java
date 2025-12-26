package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.QrRollEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IQrRollRepository extends ISoftRepository<QrRollEntity, Integer>,
        IFindByPublicIdRepository<QrRollEntity> {

    Optional<QrRollEntity> findById(Integer id);

    @Query("SELECT r FROM QrRollEntity r WHERE r.deletedAt IS NULL AND EXISTS (SELECT 1 FROM QrCodeEntity qc WHERE qc.qrRoll.id = r.id AND qc.isPrinted = false)")
    Page<QrRollEntity> findRollsWithUnprintedCodes(Pageable pageable);

    @Query("SELECT r FROM QrRollEntity r " +
           "WHERE r.deletedAt IS NULL " +
           "AND r.maxQrCodesPerDay IS NOT NULL " +
           "AND NOT EXISTS (" +
           "    SELECT 1 FROM QrRollEmployeeEntity rre " +
           "    WHERE rre.qrRoll.id = r.id " +
           "    AND rre.assignedDate = :date " +
           "    AND rre.deletedAt IS NULL" +
           ")")
    List<QrRollEntity> findUnassignedRollsForDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT r.id) FROM QrRollEntity r " +
           "INNER JOIN QrRollEmployeeEntity rre ON rre.qrRoll.id = r.id " +
           "WHERE r.deletedAt IS NULL " +
           "AND rre.assignedDate = :date " +
           "AND rre.deletedAt IS NULL")
    long countAssignedRollsForDate(@Param("date") LocalDate date);

    @Query("SELECT r FROM QrRollEntity r " +
           "WHERE r.deletedAt IS NULL " +
           "AND r.maxQrCodesPerDay IS NOT NULL " +
           "AND NOT EXISTS (" +
           "    SELECT 1 FROM QrRollEmployeeEntity rre " +
           "    WHERE rre.qrRoll.id = r.id " +
           "    AND rre.deletedAt IS NULL" +
           ")")
    List<QrRollEntity> findAvailableRolls();

    @Query("SELECT DISTINCT qc.qrRoll FROM QrCodeEntity qc " +
           "WHERE qc.isPrinted = true " +
           "AND qc.createdAt >= :startOfDay AND qc.createdAt < :endOfDay " +
           "AND qc.deletedAt IS NULL " +
           "AND (:rollPublicId IS NULL OR qc.qrRoll.publicId = :rollPublicId)")
    Page<QrRollEntity> findRollsWithPrintsByDate(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("rollPublicId") UUID rollPublicId,
            Pageable pageable
    );
}
