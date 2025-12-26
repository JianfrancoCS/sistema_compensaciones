-- =============================================
-- V131: ACTUALIZAR period_number PARA PERÍODOS EXISTENTES
-- =============================================
-- Actualiza el campo period_number para todos los períodos existentes
-- basándose en el orden de period_end dentro de cada mes
-- Esto es necesario porque V117 creó períodos sin period_number

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_payroll_periods') AND name = 'period_number')
BEGIN
    -- Actualizar period_number para cada período basándose en el orden de period_end
    UPDATE p1
    SET period_number = (
        SELECT COUNT(*) + 1
        FROM app.tbl_payroll_periods p2
        WHERE p2.year = p1.year
          AND p2.month = p1.month
          AND p2.period_end < p1.period_end
          AND p2.deleted_at IS NULL
    )
    FROM app.tbl_payroll_periods p1
    WHERE p1.deleted_at IS NULL
      AND p1.period_number = 1; -- Solo actualizar los que tienen el valor por defecto
    
    PRINT 'Campo period_number actualizado para períodos existentes.';
END
ELSE
BEGIN
    PRINT 'Columna period_number no existe. V130 debe ejecutarse primero.';
END
GO

