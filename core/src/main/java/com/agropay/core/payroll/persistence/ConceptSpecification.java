package com.agropay.core.payroll.persistence;

import com.agropay.core.payroll.domain.ConceptEntity;
import com.agropay.core.payroll.domain.ConceptCategoryEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConceptSpecification {

    public static Specification<ConceptEntity> filterBy(
            String name,
            UUID categoryPublicId,
            Sort sort
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Crear join con category (LEFT JOIN para no excluir registros)
            Join<ConceptEntity, ConceptCategoryEntity> categoryJoin = root.join("category", JoinType.LEFT);

            // Filtro por nombre (busca en name y code)
            if (StringUtils.hasText(name)) {
                String searchPattern = "%" + name.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), searchPattern),
                    cb.like(cb.lower(root.get("code")), searchPattern)
                ));
            }

            // Filtro por categoría
            if (categoryPublicId != null) {
                predicates.add(cb.equal(categoryJoin.get("publicId"), categoryPublicId));
            }

            // Aplicar ordenamiento personalizado si se proporciona
            if (sort != null && sort.isSorted()) {
                List<Order> orders = new ArrayList<>();
                for (Sort.Order sortOrder : sort) {
                    Order order = buildOrder(root, categoryJoin, sortOrder, cb);
                    if (order != null) {
                        orders.add(order);
                    }
                }
                if (!orders.isEmpty()) {
                    query.orderBy(orders);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Construye una Order para un campo, manejando campos de tablas relacionadas
     */
    private static Order buildOrder(
            Root<ConceptEntity> root,
            Join<ConceptEntity, ConceptCategoryEntity> categoryJoin,
            Sort.Order sortOrder,
            jakarta.persistence.criteria.CriteriaBuilder cb) {
        
        String property = sortOrder.getProperty();
        Sort.Direction direction = sortOrder.getDirection();

        jakarta.persistence.criteria.Expression<?> expression;
        
        switch (property) {
            case "categoryName":
                expression = categoryJoin.get("name");
                break;
            case "name":
            case "code":
            case "description":
            case "value":
            case "calculationPriority":
            case "createdAt":
            case "updatedAt":
            case "publicId":
                expression = root.get(property);
                break;
            default:
                // Por defecto, intentar buscar en la entidad raíz
                try {
                    expression = root.get(property);
                } catch (IllegalArgumentException e) {
                    // Si el campo no existe, retornar null (se ignorará)
                    return null;
                }
        }

        return direction.isAscending() ? cb.asc(expression) : cb.desc(expression);
    }
}

