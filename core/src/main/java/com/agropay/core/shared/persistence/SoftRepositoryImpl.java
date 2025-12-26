package com.agropay.core.shared.persistence;

import com.agropay.core.shared.generic.persistence.ISoftRepository;
import com.agropay.core.shared.utils.SecurityContextUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.time.LocalDateTime;


public class SoftRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID>
        implements ISoftRepository<T, ID> {

    private final EntityManager entityManager;
    private final JpaEntityInformation<T, ID> entityInformation;

    public SoftRepositoryImpl(JpaEntityInformation<T, ID> entityInformation,
                                    EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
    }

    @Override
    @Transactional
    public void softDelete(ID id) {
        softDelete(id, SecurityContextUtils.getCurrentUsername());
    }

    @Override
    @Transactional
    public void softDelete(ID id, String deletedBy) {
        softDelete(id, LocalDateTime.now(), deletedBy);
    }

    @Override
    @Transactional
    public void softDelete(ID id, LocalDateTime deletedAt, String deletedBy) {
        String entityName = entityInformation.getEntityName();
        String idFieldName = entityInformation.getIdAttribute().getName();

        String jpql = "UPDATE " + entityName + " e SET e.deletedAt = :deletedAt, e.deletedBy = :deletedBy " +
                "WHERE e." + idFieldName + " = :id AND e.deletedAt IS NULL";

        Query query = entityManager.createQuery(jpql);
        query.setParameter("id", id);
        query.setParameter("deletedAt", deletedAt);
        query.setParameter("deletedBy", deletedBy);

        int updated = query.executeUpdate();
        if (updated == 0) {
            throw new EntityNotFoundException("Entity not found or already deleted with id: " + id);
        }
    }

    @Override
    @Transactional
    public void softDeleteAll(Iterable<T> entities) {
        softDeleteAll(entities, SecurityContextUtils.getCurrentUsername());
    }

    @Override
    @Transactional
    public void softDeleteAll(Iterable<T> entities, String deletedBy) {
        LocalDateTime now = LocalDateTime.now();
        for (T entity : entities) {
            softDelete(entityInformation.getId(entity), now, deletedBy);
        }
    }

    @Override
    @Transactional
    public void softDeleteAll(Iterable<T> entities, LocalDateTime deletedAt, String deletedBy) {
        for (T entity : entities) {
            softDelete(entityInformation.getId(entity), deletedAt, deletedBy);
        }
    }
}
