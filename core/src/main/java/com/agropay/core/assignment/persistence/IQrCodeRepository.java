package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.QrCodeEntity;
import com.agropay.core.assignment.model.qrroll.QrCodeStatsDTO;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IQrCodeRepository extends ISoftRepository<QrCodeEntity, Long>,
        JpaSpecificationExecutor<QrCodeEntity> {

    @Query("SELECT qc FROM QrCodeEntity qc " +
           "WHERE qc.id = :qrCodeId " +
           "AND qc.deletedAt IS NULL")
    Optional<QrCodeEntity> findByQrCodeId(@Param("qrCodeId") Long qrCodeId);

    @Query("SELECT COUNT(qc) FROM QrCodeEntity qc " +
           "WHERE qc.qrRoll.id = :rollId " +
           "AND qc.deletedAt IS NULL")
    long countByRollId(@Param("rollId") Integer rollId);

    @Query("SELECT COUNT(qc) FROM QrCodeEntity qc " +
           "WHERE qc.qrRoll.id = :rollId " +
           "AND qc.isPrinted = false " +
           "AND qc.deletedAt IS NULL")
    long countUnprintedByRollId(@Param("rollId") Integer rollId);

    @Query("SELECT qc FROM QrCodeEntity qc " +
           "WHERE qc.qrRoll.id = :rollId " +
           "AND qc.deletedAt IS NULL " +
           "ORDER BY qc.createdAt DESC")
    List<QrCodeEntity> findByRollId(@Param("rollId") Integer rollId);

    Optional<QrCodeEntity> findByPublicId(UUID publicId);

    @Query("SELECT qc FROM QrCodeEntity qc " +
           "WHERE qc.qrRoll.id = :rollId " +
           "AND qc.isPrinted = false " +
           "AND qc.createdAt >= :startOfDay AND qc.createdAt < :endOfDay " +
           "AND qc.deletedAt IS NULL")
    List<QrCodeEntity> findUnprintedByRollIdAndDate(
            @Param("rollId") Integer rollId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT new com.agropay.core.assignment.model.qrroll.QrCodeStatsDTO(" +
           "   SUM(CASE WHEN qc.isPrinted = true THEN 1 ELSE 0 END), " +
           "   SUM(CASE WHEN qc.isUsed = true THEN 1 ELSE 0 END)) " +
           "FROM QrCodeEntity qc " +
           "WHERE qc.qrRoll.id = :rollId " +
           "AND qc.createdAt >= :startOfDay AND qc.createdAt < :endOfDay " +
           "AND qc.deletedAt IS NULL")
    QrCodeStatsDTO getPrintStatsByRollIdAndDate(
            @Param("rollId") Integer rollId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
