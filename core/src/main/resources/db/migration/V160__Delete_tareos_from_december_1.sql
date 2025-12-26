-- =============================================
-- V160: Eliminar tareos del 1 de Diciembre 2025
-- =============================================
-- Esta migración hace soft delete de todos los tareos creados el 1 de diciembre de 2025
-- para dejar diciembre libre de tareos creados por migraciones
-- =============================================

BEGIN TRANSACTION;

-- Hacer soft delete de todos los tareos del 1 de diciembre de 2025
UPDATE app.tbl_tareos
SET deleted_at = GETUTCDATE(),
    deleted_by = 'SYSTEM',
    updated_at = GETUTCDATE(),
    updated_by = 'SYSTEM'
WHERE CAST(created_at AS DATE) = '2025-12-01'
    AND deleted_at IS NULL;

DECLARE @tareos_eliminados INT = @@ROWCOUNT;
PRINT 'Tareos eliminados (soft delete) del 1 de diciembre 2025: ' + CAST(@tareos_eliminados AS VARCHAR);

-- También hacer soft delete de los tareo_employees asociados a esos tareos
UPDATE app.tbl_tareo_employees
SET deleted_at = GETUTCDATE(),
    deleted_by = 'SYSTEM',
    updated_at = GETUTCDATE(),
    updated_by = 'SYSTEM'
WHERE tareo_id IN (
    SELECT id FROM app.tbl_tareos 
    WHERE CAST(created_at AS DATE) = '2025-12-01'
        AND deleted_at IS NOT NULL
)
AND deleted_at IS NULL;

DECLARE @tareo_employees_eliminados INT = @@ROWCOUNT;
PRINT 'Tareo employees eliminados (soft delete): ' + CAST(@tareo_employees_eliminados AS VARCHAR);

COMMIT TRANSACTION;

PRINT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';
PRINT 'Migración V160 completada exitosamente';
PRINT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';

