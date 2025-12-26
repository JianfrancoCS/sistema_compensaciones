package com.agropay.core.shared.generic.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.LocalDateTime;

@NoRepositoryBean
public interface ISoftRepository<T, ID> extends JpaRepository<T, ID> {
    /**
     * Soft delete que obtiene automáticamente el username del SecurityContext.
     */
    void softDelete(ID id);
    
    void softDelete(ID id, String deletedBy);
    void softDelete(ID id, LocalDateTime deletedAt, String deletedBy);
    
    /**
     * Soft delete all que obtiene automáticamente el username del SecurityContext.
     */
    void softDeleteAll(Iterable<T> entities);
    
    void softDeleteAll(Iterable<T> entities, String deletedBy);
    void softDeleteAll(Iterable<T> entities, LocalDateTime deletedAt, String deletedBy);
}
