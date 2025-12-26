-- =============================================
-- V155: AGREGAR COLUMNAS DE SOFT DELETE A TBL_TAREO_MOTIVES
-- =============================================
-- La entidad TareoMotiveEntity extiende AbstractEntity que incluye
-- updated_at, updated_by, deleted_at y deleted_by, pero parece que la tabla
-- en la base de datos no tiene estas columnas. Esta migración las agrega
-- condicionalmente si no existen.

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_tareo_motives' AND schema_id = SCHEMA_ID('app'))
BEGIN
    -- Agregar updated_at si no existe
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_tareo_motives') AND name = 'updated_at')
    BEGIN
        ALTER TABLE app.tbl_tareo_motives
        ADD updated_at DATETIME2 NULL DEFAULT GETUTCDATE();
        PRINT 'Column updated_at added to tbl_tareo_motives';
    END

    -- Agregar updated_by si no existe
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_tareo_motives') AND name = 'updated_by')
    BEGIN
        ALTER TABLE app.tbl_tareo_motives
        ADD updated_by NVARCHAR(100) NULL;
        PRINT 'Column updated_by added to tbl_tareo_motives';
    END

    -- Agregar deleted_at si no existe
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_tareo_motives') AND name = 'deleted_at')
    BEGIN
        ALTER TABLE app.tbl_tareo_motives
        ADD deleted_at DATETIME2 NULL;
        PRINT 'Column deleted_at added to tbl_tareo_motives';
    END

    -- Agregar deleted_by si no existe
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_tareo_motives') AND name = 'deleted_by')
    BEGIN
        ALTER TABLE app.tbl_tareo_motives
        ADD deleted_by NVARCHAR(100) NULL;
        PRINT 'Column deleted_by added to tbl_tareo_motives';
    END
END
GO

