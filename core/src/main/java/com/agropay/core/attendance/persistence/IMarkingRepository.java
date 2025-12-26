package com.agropay.core.attendance.persistence;

import com.agropay.core.attendance.domain.MarkingDetailEntity;
import com.agropay.core.attendance.domain.MarkingEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IMarkingRepository extends ISoftRepository<MarkingEntity, Long>, JpaSpecificationExecutor<MarkingEntity> {

    Optional<MarkingEntity> findByPublicId(UUID publicId);

    // Dashboard query - Note: This query uses MarkingDetailEntity which has the entry type information
    // For now, we'll query MarkingDetailEntity directly
    @Query("""
        SELECT CAST(md.markedAt AS date) as date,
               SUM(CASE WHEN md.isEntry = true THEN 1 ELSE 0 END) as entries,
               SUM(CASE WHEN md.isEntry = false THEN 1 ELSE 0 END) as exits
        FROM MarkingDetailEntity md
        JOIN md.marking m
        WHERE md.deletedAt IS NULL
        AND m.deletedAt IS NULL
        AND (:subsidiaryId IS NULL OR m.subsidiary.id = :subsidiaryId)
        AND (:dateFrom IS NULL OR CAST(md.markedAt AS date) >= :dateFrom)
        AND (:dateTo IS NULL OR CAST(md.markedAt AS date) <= :dateTo)
        GROUP BY CAST(md.markedAt AS date)
        ORDER BY date ASC
    """)
    List<Object[]> getAttendanceTrend(
            @Param("subsidiaryId") Short subsidiaryId,
            @Param("dateFrom") java.time.LocalDate dateFrom,
            @Param("dateTo") java.time.LocalDate dateTo
    );
}