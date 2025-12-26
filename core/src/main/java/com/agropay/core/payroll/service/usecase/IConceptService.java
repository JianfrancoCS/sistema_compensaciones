package com.agropay.core.payroll.service.usecase;

import com.agropay.core.payroll.model.concept.*;
import com.agropay.core.shared.utils.PagedResult;

import java.util.List;
import java.util.UUID;

public interface IConceptService {

    /**
     * Obtiene todos los conceptos activos como opciones de select.
     * Usado para dropdowns en asignación de AFP/ONP a empleados.
     *
     * @return Lista de conceptos en formato select option
     */
    List<ConceptSelectOptionDTO> getSelectOptions();

    /**
     * Crea un nuevo concepto de planilla.
     *
     * @param request Datos del concepto a crear
     * @return Respuesta con los detalles del concepto creado
     */
    CommandConceptResponse create(CreateConceptRequest request);

    /**
     * Actualiza un concepto existente.
     *
     * @param publicId ID público del concepto a actualizar
     * @param request Datos actualizados del concepto
     * @return Respuesta con los detalles del concepto actualizado
     */
    CommandConceptResponse update(UUID publicId, UpdateConceptRequest request);

    /**
     * Elimina un concepto (soft delete).
     * Solo se puede eliminar si no está en uso en ninguna configuración de planilla.
     *
     * @param publicId ID público del concepto a eliminar
     */
    void delete(UUID publicId);

    /**
     * Obtiene los detalles de un concepto por su ID público.
     *
     * @param publicId ID público del concepto
     * @return Detalles del concepto
     */
    ConceptDetailsDTO getByPublicId(UUID publicId);

    /**
     * Obtiene una lista paginada de conceptos.
     *
     * @param name Filtro opcional por nombre
     * @param categoryPublicId Filtro opcional por categoría (publicId)
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el cual ordenar
     * @param sortDirection Dirección de ordenamiento (ASC/DESC)
     * @return Lista paginada de conceptos
     */
    PagedResult<ConceptListDTO> findAllPaged(String name, UUID categoryPublicId, int page, int size, String sortBy, String sortDirection);

    /**
     * Obtiene todas las categorías de conceptos disponibles.
     *
     * @return Lista de categorías con publicId y name
     */
    List<ConceptCategoryOptionDTO> getCategories();

    /**
     * Obtiene conceptos filtrados por código de categoría.
     * Usado para obtener conceptos de jubilación (RETIREMENT) o seguro (EMPLOYEE_CONTRIBUTION).
     *
     * @param categoryCode Código de la categoría (ej: "RETIREMENT", "EMPLOYEE_CONTRIBUTION")
     * @return Lista de conceptos en formato select option
     */
    List<ConceptSelectOptionDTO> getSelectOptionsByCategoryCode(String categoryCode);
}
