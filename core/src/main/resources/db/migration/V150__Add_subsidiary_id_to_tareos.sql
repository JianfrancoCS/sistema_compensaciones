-- =============================================
-- V150: AGREGAR SUBSIDIARY_ID A TAREOS
-- =============================================
-- Agrega una relación directa entre tareos y subsidiarias
-- Esto permite que los tareos administrativos (sin lote) tengan una subsidiaria directa
-- en lugar de depender del supervisor

-- 1. Agregar columna subsidiary_id (nullable inicialmente para poblar datos)
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_tareos') AND name = 'subsidiary_id')
BEGIN
    ALTER TABLE app.tbl_tareos
    ADD subsidiary_id SMALLINT NULL;
    
    PRINT 'Columna subsidiary_id agregada a tbl_tareos';
END
GO

-- 2. Poblar datos existentes:
--    - Si el tareo tiene lote, usar la subsidiaria del lote
--    - Si el tareo no tiene lote, usar la subsidiaria del supervisor
UPDATE t
SET subsidiary_id = CASE
    WHEN t.lote_id IS NOT NULL THEN l.subsidiary_id
    ELSE e.subsidiary_id
END
FROM app.tbl_tareos t
LEFT JOIN app.tbl_lotes l ON t.lote_id = l.id AND l.deleted_at IS NULL
LEFT JOIN app.tbl_employees e ON t.supervisor_employee_document_number = e.person_document_number AND e.deleted_at IS NULL
WHERE t.deleted_at IS NULL
  AND t.subsidiary_id IS NULL;

PRINT 'Datos de subsidiary_id poblados para tareos existentes';
GO

-- 3. Verificar que todos los tareos activos tengan subsidiary_id
DECLARE @tareos_sin_subsidiaria INT = (
    SELECT COUNT(*)
    FROM app.tbl_tareos
    WHERE deleted_at IS NULL
      AND subsidiary_id IS NULL
);

IF @tareos_sin_subsidiaria > 0
BEGIN
    PRINT 'ADVERTENCIA: Existen ' + CAST(@tareos_sin_subsidiaria AS NVARCHAR) + ' tareos activos sin subsidiary_id';
    PRINT 'Estos tareos pueden tener problemas al obtener la subsidiaria';
END
ELSE
BEGIN
    PRINT 'Todos los tareos activos tienen subsidiary_id asignado';
END
GO

-- 4. Hacer la columna NOT NULL (después de poblar datos)
-- Primero verificamos que no haya NULLs
IF NOT EXISTS (
    SELECT 1 
    FROM app.tbl_tareos 
    WHERE deleted_at IS NULL 
      AND subsidiary_id IS NULL
)
BEGIN
    ALTER TABLE app.tbl_tareos
    ALTER COLUMN subsidiary_id SMALLINT NOT NULL;
    
    PRINT 'Columna subsidiary_id establecida como NOT NULL';
END
ELSE
BEGIN
    PRINT 'ADVERTENCIA: No se puede establecer subsidiary_id como NOT NULL porque existen tareos activos sin subsidiaria';
    PRINT 'Por favor, revisa los tareos sin subsidiaria antes de continuar';
END
GO

-- 5. Agregar foreign key constraint
IF NOT EXISTS (
    SELECT 1 
    FROM sys.foreign_keys 
    WHERE name = 'FK_tareos_subsidiary'
      AND parent_object_id = OBJECT_ID('app.tbl_tareos')
)
BEGIN
    ALTER TABLE app.tbl_tareos
    ADD CONSTRAINT FK_tareos_subsidiary
        FOREIGN KEY (subsidiary_id)
        REFERENCES app.tbl_subsidiaries(id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;
    
    PRINT 'Foreign key FK_tareos_subsidiary agregado';
END
GO

-- 6. Agregar índice para mejorar rendimiento de consultas
IF NOT EXISTS (
    SELECT 1 
    FROM sys.indexes 
    WHERE name = 'IX_tareos_subsidiary_active'
      AND object_id = OBJECT_ID('app.tbl_tareos')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_tareos_subsidiary_active
    ON app.tbl_tareos(subsidiary_id, deleted_at);
    
    PRINT 'Índice IX_tareos_subsidiary_active creado';
END
GO

PRINT 'Migración V150 completada exitosamente';
GO

