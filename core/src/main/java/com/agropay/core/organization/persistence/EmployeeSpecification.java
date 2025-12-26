package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.domain.PositionEntity;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.organization.domain.DocumentTypeEntity;
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

public class EmployeeSpecification {

    public static Specification<EmployeeEntity> filterBy(
            String documentNumber,
            String personName,
            UUID subsidiaryId,
            UUID positionId,
            UUID documentTypePublicId,
            Sort sort
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Crear joins necesarios (LEFT JOIN para no excluir registros)
            Join<EmployeeEntity, PersonEntity> personJoin = root.join("person", JoinType.LEFT);
            Join<EmployeeEntity, SubsidiaryEntity> subsidiaryJoin = root.join("subsidiary", JoinType.LEFT);
            Join<EmployeeEntity, PositionEntity> positionJoin = root.join("position", JoinType.LEFT);

            if (StringUtils.hasText(documentNumber)) {
                predicates.add(cb.like(cb.lower(root.get("personDocumentNumber")), "%" + documentNumber.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(personName)) {
                predicates.add(cb.like(cb.lower(personJoin.get("names")), "%" + personName.toLowerCase() + "%"));
            }

            if (subsidiaryId != null) {
                predicates.add(cb.equal(subsidiaryJoin.get("publicId"), subsidiaryId));
            }

            if (positionId != null) {
                predicates.add(cb.equal(positionJoin.get("publicId"), positionId));
            }

            if (documentTypePublicId != null) {
                Join<PersonEntity, DocumentTypeEntity> documentTypeJoin = personJoin.join("documentType", JoinType.LEFT);
                predicates.add(cb.equal(documentTypeJoin.get("publicId"), documentTypePublicId));
            }

            // Aplicar ordenamiento personalizado si se proporciona
            if (sort != null && sort.isSorted()) {
                List<Order> orders = new ArrayList<>();
                for (Sort.Order sortOrder : sort) {
                    Order order = buildOrder(root, personJoin, subsidiaryJoin, positionJoin, sortOrder, cb);
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
            Root<EmployeeEntity> root,
            Join<EmployeeEntity, PersonEntity> personJoin,
            Join<EmployeeEntity, SubsidiaryEntity> subsidiaryJoin,
            Join<EmployeeEntity, PositionEntity> positionJoin,
            Sort.Order sortOrder,
            jakarta.persistence.criteria.CriteriaBuilder cb) {
        
        String property = sortOrder.getProperty();
        Sort.Direction direction = sortOrder.getDirection();

        jakarta.persistence.criteria.Expression<?> expression;
        
        switch (property) {
            case "subsidiaryName":
                expression = subsidiaryJoin.get("name");
                break;
            case "positionName":
                expression = positionJoin.get("name");
                break;
            case "documentNumber":
            case "personDocumentNumber": 
                expression = root.get("personDocumentNumber");
                break;
            case "names":
            case "paternalLastname":
            case "maternalLastname":
                expression = personJoin.get(property);
                break;
            case "createdAt":
            case "updatedAt":
            case "publicId":
            case "code":
                expression = root.get(property.equals("publicId") ? "code" : property);
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
