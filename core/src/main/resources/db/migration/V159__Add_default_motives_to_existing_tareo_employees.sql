-- =============================================
-- V159: Agregar campos de soft delete y motivos por defecto a tareos existentes
-- =============================================
-- Esta migración:
-- 1. Agrega los campos de soft delete (deleted_at, deleted_by, updated_at, updated_by) a tbl_tareo_employee_motives
-- 2. Asigna motivos por defecto a los tareos existentes que no tienen motivos asignados
--    Se usa "Capacitación inicial" (remunerado) como motivo de entrada por defecto
--    y se determina el motivo de salida según si el empleado trabajó 8 horas o menos.
-- =============================================

BEGIN TRANSACTION;

-- Agregar campos de soft delete a tbl_tareo_employee_motives si no existen
IF NOT EXISTS (
    SELECT 1 
    FROM sys.columns 
    WHERE object_id = OBJECT_ID('app.tbl_tareo_employee_motives') 
    AND name = 'updated_at'
)
BEGIN
    ALTER TABLE app.tbl_tareo_employee_motives
    ADD updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL;
    
    PRINT 'Campos de soft delete agregados a tbl_tareo_employee_motives';
END
ELSE
BEGIN
    PRINT 'Campos de soft delete ya existen en tbl_tareo_employee_motives';
END

-- Obtener IDs de motivos
DECLARE @motive_capacitacion_id SMALLINT;
DECLARE @motive_permiso_con_goce_id SMALLINT;
DECLARE @motive_salida_tramite_id SMALLINT;

-- Obtener ID de "Capacitación inicial" (remunerado, is_paid = 1)
SELECT @motive_capacitacion_id = id 
FROM app.tbl_tareo_motives 
WHERE name = 'Capacitación inicial' AND deleted_at IS NULL;

-- Obtener ID de "Permiso con goce" (remunerado, is_paid = 1)
SELECT @motive_permiso_con_goce_id = id 
FROM app.tbl_tareo_motives 
WHERE name = 'Permiso con goce' AND deleted_at IS NULL;

-- Obtener ID de "Salida por trámite personal" (no remunerado, is_paid = 0)
SELECT @motive_salida_tramite_id = id 
FROM app.tbl_tareo_motives 
WHERE name = 'Salida por trámite personal' AND deleted_at IS NULL;

-- Verificar que los motivos existen
IF @motive_capacitacion_id IS NULL
BEGIN
    PRINT 'ERROR: Motivo "Capacitación inicial" no encontrado';
    ROLLBACK TRANSACTION;
    RETURN;
END

IF @motive_permiso_con_goce_id IS NULL
BEGIN
    PRINT 'ERROR: Motivo "Permiso con goce" no encontrado';
    ROLLBACK TRANSACTION;
    RETURN;
END

IF @motive_salida_tramite_id IS NULL
BEGIN
    PRINT 'ERROR: Motivo "Salida por trámite personal" no encontrado';
    ROLLBACK TRANSACTION;
    RETURN;
END

PRINT 'Motivos encontrados:';
PRINT '  - Capacitación inicial: ' + CAST(@motive_capacitacion_id AS VARCHAR);
PRINT '  - Permiso con goce: ' + CAST(@motive_permiso_con_goce_id AS VARCHAR);
PRINT '  - Salida por trámite personal: ' + CAST(@motive_salida_tramite_id AS VARCHAR);

-- Insertar motivos de entrada para tareos sin motivo de entrada
-- Usar "Capacitación inicial" (remunerado) como motivo por defecto
INSERT INTO app.tbl_tareo_employee_motives (tareo_employee_id, motive_id, applied_at, created_at, created_by)
SELECT 
    te.id,
    @motive_capacitacion_id,
    te.start_time,
    GETUTCDATE(),
    'SYSTEM'
FROM app.tbl_tareo_employees te
WHERE te.deleted_at IS NULL
    AND te.start_time IS NOT NULL
    -- Solo agregar si no tiene ningún motivo de entrada (aplicado a la hora de entrada)
    AND NOT EXISTS (
        SELECT 1 
        FROM app.tbl_tareo_employee_motives tem
        WHERE tem.tareo_employee_id = te.id
            AND tem.applied_at = te.start_time
            AND (tem.deleted_at IS NULL)
    );

DECLARE @entrada_count INT = @@ROWCOUNT;
PRINT 'Motivos de entrada agregados: ' + CAST(@entrada_count AS VARCHAR);

-- Insertar motivos de salida para tareos sin motivo de salida
-- Si trabajó 8 horas o más, usar "Permiso con goce" (remunerado)
-- Si trabajó menos de 8 horas, usar "Salida por trámite personal" (no remunerado)
INSERT INTO app.tbl_tareo_employee_motives (tareo_employee_id, motive_id, applied_at, created_at, created_by)
SELECT 
    te.id,
    CASE 
        WHEN te.actual_hours >= 8.0 THEN @motive_permiso_con_goce_id
        ELSE @motive_salida_tramite_id
    END,
    te.end_time,
    GETUTCDATE(),
    'SYSTEM'
FROM app.tbl_tareo_employees te
WHERE te.deleted_at IS NULL
    AND te.end_time IS NOT NULL
    AND te.actual_hours IS NOT NULL
    -- Solo agregar si no tiene ningún motivo de salida (aplicado a la hora de salida)
    AND NOT EXISTS (
        SELECT 1 
        FROM app.tbl_tareo_employee_motives tem
        WHERE tem.tareo_employee_id = te.id
            AND tem.applied_at = te.end_time
            AND (tem.deleted_at IS NULL)
    );

DECLARE @salida_count INT = @@ROWCOUNT;
PRINT 'Motivos de salida agregados: ' + CAST(@salida_count AS VARCHAR);

-- Recalcular paidHours para los tareos actualizados
-- Si ambos motivos son remunerados: paidHours = 8.0
-- Si alguno no es remunerado: paidHours = actualHours
UPDATE te
SET paid_hours = CASE
    WHEN entrada_motive.is_paid = 1 AND salida_motive.is_paid = 1 THEN CAST(8.0 AS DECIMAL(5,2))
    ELSE te.actual_hours
END,
updated_at = GETUTCDATE(),
updated_by = 'SYSTEM'
FROM app.tbl_tareo_employees te
INNER JOIN (
    -- Motivo de entrada: el que se aplica a la hora de entrada (start_time)
    SELECT 
        tem.tareo_employee_id,
        tm.is_paid
    FROM app.tbl_tareo_employee_motives tem
    INNER JOIN app.tbl_tareo_motives tm ON tm.id = tem.motive_id
    INNER JOIN app.tbl_tareo_employees te2 ON te2.id = tem.tareo_employee_id
    WHERE te2.deleted_at IS NULL
        AND tem.deleted_at IS NULL
        AND tem.applied_at = te2.start_time
) entrada_motive ON entrada_motive.tareo_employee_id = te.id
INNER JOIN (
    -- Motivo de salida: el que se aplica a la hora de salida (end_time)
    SELECT 
        tem.tareo_employee_id,
        tm.is_paid
    FROM app.tbl_tareo_employee_motives tem
    INNER JOIN app.tbl_tareo_motives tm ON tm.id = tem.motive_id
    INNER JOIN app.tbl_tareo_employees te2 ON te2.id = tem.tareo_employee_id
    WHERE te2.deleted_at IS NULL
        AND tem.deleted_at IS NULL
        AND tem.applied_at = te2.end_time
) salida_motive ON salida_motive.tareo_employee_id = te.id
WHERE te.deleted_at IS NULL
    AND te.actual_hours IS NOT NULL
    AND te.start_time IS NOT NULL
    AND te.end_time IS NOT NULL
    AND (
        te.paid_hours IS NULL 
        OR te.paid_hours != CASE
            WHEN entrada_motive.is_paid = 1 AND salida_motive.is_paid = 1 THEN CAST(8.0 AS DECIMAL(5,2))
            ELSE te.actual_hours
        END
    );

DECLARE @paid_hours_updated INT = @@ROWCOUNT;
PRINT 'paidHours recalculados: ' + CAST(@paid_hours_updated AS VARCHAR);

COMMIT TRANSACTION;

PRINT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';
PRINT 'Migración V159 completada exitosamente';
PRINT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';

