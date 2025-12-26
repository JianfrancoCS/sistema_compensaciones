-- =============================================
-- V156: FORZAR AGREGAR COLUMNAS DE SOFT DELETE A TBL_TAREO_MOTIVES
-- =============================================
-- La migración V155 se ejecutó pero las columnas no se agregaron.
-- Esta migración las agrega de forma más robusta, manejando errores.

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_tareo_motives' AND schema_id = SCHEMA_ID('app'))
BEGIN
    BEGIN TRY
        -- Agregar updated_at si no existe
        IF NOT EXISTS (SELECT 1 FROM sys.columns 
                       WHERE object_id = OBJECT_ID('app.tbl_tareo_motives') 
                       AND name = 'updated_at')
        BEGIN
            ALTER TABLE app.tbl_tareo_motives
            ADD updated_at DATETIME2 NULL;
            PRINT 'Column updated_at added to tbl_tareo_motives';
        END
        ELSE
        BEGIN
            PRINT 'Column updated_at already exists in tbl_tareo_motives';
        END
    END TRY
    BEGIN CATCH
        PRINT 'Error adding updated_at: ' + ERROR_MESSAGE();
    END CATCH

    BEGIN TRY
        -- Agregar updated_by si no existe
        IF NOT EXISTS (SELECT 1 FROM sys.columns 
                       WHERE object_id = OBJECT_ID('app.tbl_tareo_motives') 
                       AND name = 'updated_by')
        BEGIN
            ALTER TABLE app.tbl_tareo_motives
            ADD updated_by NVARCHAR(100) NULL;
            PRINT 'Column updated_by added to tbl_tareo_motives';
        END
        ELSE
        BEGIN
            PRINT 'Column updated_by already exists in tbl_tareo_motives';
        END
    END TRY
    BEGIN CATCH
        PRINT 'Error adding updated_by: ' + ERROR_MESSAGE();
    END CATCH

    BEGIN TRY
        -- Agregar deleted_at si no existe
        IF NOT EXISTS (SELECT 1 FROM sys.columns 
                       WHERE object_id = OBJECT_ID('app.tbl_tareo_motives') 
                       AND name = 'deleted_at')
        BEGIN
            ALTER TABLE app.tbl_tareo_motives
            ADD deleted_at DATETIME2 NULL;
            PRINT 'Column deleted_at added to tbl_tareo_motives';
        END
        ELSE
        BEGIN
            PRINT 'Column deleted_at already exists in tbl_tareo_motives';
        END
    END TRY
    BEGIN CATCH
        PRINT 'Error adding deleted_at: ' + ERROR_MESSAGE();
    END CATCH

    BEGIN TRY
        -- Agregar deleted_by si no existe
        IF NOT EXISTS (SELECT 1 FROM sys.columns 
                       WHERE object_id = OBJECT_ID('app.tbl_tareo_motives') 
                       AND name = 'deleted_by')
        BEGIN
            ALTER TABLE app.tbl_tareo_motives
            ADD deleted_by NVARCHAR(100) NULL;
            PRINT 'Column deleted_by added to tbl_tareo_motives';
        END
        ELSE
        BEGIN
            PRINT 'Column deleted_by already exists in tbl_tareo_motives';
        END
    END TRY
    BEGIN CATCH
        PRINT 'Error adding deleted_by: ' + ERROR_MESSAGE();
    END CATCH
END
ELSE
BEGIN
    PRINT 'Table app.tbl_tareo_motives does not exist';
END
GO

