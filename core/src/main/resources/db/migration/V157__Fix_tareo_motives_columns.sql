-- =============================================
-- V157: CORREGIR COLUMNAS DE TBL_TAREO_MOTIVES
-- =============================================
-- Script directo para agregar las columnas que faltan.
-- Ejecuta los ALTER TABLE directamente sin condiciones.

USE agropay;
GO

-- Verificar y agregar updated_at
IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('app.tbl_tareo_motives') 
    AND name = 'updated_at'
)
BEGIN
    ALTER TABLE app.tbl_tareo_motives
    ADD updated_at DATETIME2 NULL;
    PRINT 'Column updated_at added';
END
ELSE
BEGIN
    PRINT 'Column updated_at already exists';
END
GO

-- Verificar y agregar updated_by
IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('app.tbl_tareo_motives') 
    AND name = 'updated_by'
)
BEGIN
    ALTER TABLE app.tbl_tareo_motives
    ADD updated_by NVARCHAR(100) NULL;
    PRINT 'Column updated_by added';
END
ELSE
BEGIN
    PRINT 'Column updated_by already exists';
END
GO

-- Verificar y agregar deleted_at
IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('app.tbl_tareo_motives') 
    AND name = 'deleted_at'
)
BEGIN
    ALTER TABLE app.tbl_tareo_motives
    ADD deleted_at DATETIME2 NULL;
    PRINT 'Column deleted_at added';
END
ELSE
BEGIN
    PRINT 'Column deleted_at already exists';
END
GO

-- Verificar y agregar deleted_by
IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('app.tbl_tareo_motives') 
    AND name = 'deleted_by'
)
BEGIN
    ALTER TABLE app.tbl_tareo_motives
    ADD deleted_by NVARCHAR(100) NULL;
    PRINT 'Column deleted_by added';
END
ELSE
BEGIN
    PRINT 'Column deleted_by already exists';
END
GO

