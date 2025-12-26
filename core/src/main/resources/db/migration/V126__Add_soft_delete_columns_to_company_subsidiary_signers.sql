-- =============================================
-- V126: AGREGAR COLUMNAS DE SOFT DELETE A TBL_COMPANY_SUBSIDIARY_SIGNERS
-- =============================================
-- La entidad CompanySubsidiarySignerEntity extiende AbstractEntity que incluye
-- updated_at, updated_by, deleted_at y deleted_by, pero la migración V113 no las creó.
-- Esta migración agrega las columnas faltantes para soportar soft delete.

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_company_subsidiary_signers' AND schema_id = SCHEMA_ID('app'))
BEGIN
    -- Agregar updated_at si no existe
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_company_subsidiary_signers') AND name = 'updated_at')
    BEGIN
        ALTER TABLE app.tbl_company_subsidiary_signers
        ADD updated_at DATETIME2 NULL;
    END

    -- Agregar updated_by si no existe
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_company_subsidiary_signers') AND name = 'updated_by')
    BEGIN
        ALTER TABLE app.tbl_company_subsidiary_signers
        ADD updated_by NVARCHAR(100) NULL;
    END

    -- Agregar deleted_at si no existe
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_company_subsidiary_signers') AND name = 'deleted_at')
    BEGIN
        ALTER TABLE app.tbl_company_subsidiary_signers
        ADD deleted_at DATETIME2 NULL;
    END

    -- Agregar deleted_by si no existe
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_company_subsidiary_signers') AND name = 'deleted_by')
    BEGIN
        ALTER TABLE app.tbl_company_subsidiary_signers
        ADD deleted_by NVARCHAR(100) NULL;
    END
END
GO

