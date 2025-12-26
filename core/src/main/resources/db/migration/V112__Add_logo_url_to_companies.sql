-- =============================================
-- V112: AGREGAR CAMPO LOGO_URL A COMPANIES
-- =============================================
-- Agrega campo para almacenar la URL del logo de la empresa
-- necesario para generar boletas de pago con el logo corporativo

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_companies') AND name = 'logo_url')
BEGIN
    ALTER TABLE app.tbl_companies
    ADD logo_url NVARCHAR(500) NULL; -- URL del logo de la empresa
END
GO

-- Actualizar la empresa existente con el logo
UPDATE app.tbl_companies
SET logo_url = 'https://res.cloudinary.com/dcg6envhf/image/upload/v1763394460/logo_inkasberries_grx8jx.webp'
WHERE ruc = '20520866630' AND logo_url IS NULL;
GO

