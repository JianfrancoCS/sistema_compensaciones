-- =============================================
-- Actualizar nombres de categorías de conceptos a español
-- =============================================

UPDATE app.tbl_concept_categories
SET name = 'Ingresos',
    updated_at = GETUTCDATE(),
    updated_by = 'SYSTEM'
WHERE code = 'INCOME' AND deleted_at IS NULL;
GO

UPDATE app.tbl_concept_categories
SET name = 'Descuentos',
    updated_at = GETUTCDATE(),
    updated_by = 'SYSTEM'
WHERE code = 'DEDUCTION' AND deleted_at IS NULL;
GO

UPDATE app.tbl_concept_categories
SET name = 'Jubilación',
    updated_at = GETUTCDATE(),
    updated_by = 'SYSTEM'
WHERE code = 'RETIREMENT' AND deleted_at IS NULL;
GO

UPDATE app.tbl_concept_categories
SET name = 'Aportes del Empleado',
    updated_at = GETUTCDATE(),
    updated_by = 'SYSTEM'
WHERE code = 'EMPLOYEE_CONTRIBUTION' AND deleted_at IS NULL;
GO

UPDATE app.tbl_concept_categories
SET name = 'Aportes del Empleador',
    updated_at = GETUTCDATE(),
    updated_by = 'SYSTEM'
WHERE code = 'EMPLOYER_CONTRIBUTION' AND deleted_at IS NULL;
GO

