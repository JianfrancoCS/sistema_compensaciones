-- =============================================
-- Agregar Seguro de Vida Ley y actualizar valores según Perú 2025
-- =============================================

-- Obtener IDs de categorías
DECLARE @employerContributionId SMALLINT;
SELECT @employerContributionId = id FROM app.tbl_concept_categories WHERE code = 'EMPLOYER_CONTRIBUTION';

-- =============================================
-- 1. Agregar Seguro de Vida Ley (0.51% del empleador)
-- =============================================
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'SEGURO_VIDA_LEY')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'SEGURO_VIDA_LEY',
        'Seguro de Vida Ley',
        'Seguro de Vida Ley - Aporte del empleador (0.51% según normativa peruana 2025)',
        @employerContributionId,
        0.51, -- Porcentaje fijo por ley
        201, -- Prioridad después de EsSalud
        GETUTCDATE(),
        'SYSTEM'
    );
    PRINT 'Concepto SEGURO_VIDA_LEY agregado correctamente';
END
ELSE
BEGIN
    PRINT 'Concepto SEGURO_VIDA_LEY ya existe, actualizando valor';
    UPDATE app.tbl_concepts
    SET value = 0.51,
        description = 'Seguro de Vida Ley - Aporte del empleador (0.51% según normativa peruana 2025)',
        updated_at = GETUTCDATE(),
        updated_by = 'SYSTEM'
    WHERE code = 'SEGURO_VIDA_LEY';
END

-- =============================================
-- 2. Actualizar EsSalud a 6% (según normativa peruana 2025)
-- =============================================
UPDATE app.tbl_concepts
SET value = 6.00,
    description = 'Seguro Social de Salud - Aporte del empleador (6% según normativa peruana 2025)',
    updated_at = GETUTCDATE(),
    updated_by = 'SYSTEM'
WHERE code = 'ESSALUD';

PRINT 'Valor de EsSalud actualizado a 6%';

-- =============================================
-- Verificación final
-- =============================================
DECLARE @totalConceptos INT;
SELECT @totalConceptos = COUNT(*) FROM app.tbl_concepts WHERE deleted_at IS NULL;

IF @totalConceptos < 12
BEGIN
    RAISERROR('Advertencia: Se esperaban al menos 12 conceptos pero se encontraron %d', 10, 1, @totalConceptos);
END
ELSE
BEGIN
    PRINT 'Migración exitosa: Conceptos actualizados correctamente';
    PRINT 'Seguro de Vida Ley agregado (0.51%)';
    PRINT 'EsSalud actualizado a 6%';
END
GO

