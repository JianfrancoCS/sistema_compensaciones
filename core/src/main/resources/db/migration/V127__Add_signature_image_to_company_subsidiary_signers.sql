-- =============================================
-- V127: AGREGAR CAMPO DE IMAGEN DE FIRMA A TBL_COMPANY_SUBSIDIARY_SIGNERS
-- =============================================
-- Agrega el campo signature_image_url para almacenar la URL de la imagen de firma del responsable

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_company_subsidiary_signers' AND schema_id = SCHEMA_ID('app'))
BEGIN
    -- Agregar signature_image_url si no existe
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_company_subsidiary_signers') AND name = 'signature_image_url')
    BEGIN
        ALTER TABLE app.tbl_company_subsidiary_signers
        ADD signature_image_url NVARCHAR(500) NULL;
    END
END
GO

