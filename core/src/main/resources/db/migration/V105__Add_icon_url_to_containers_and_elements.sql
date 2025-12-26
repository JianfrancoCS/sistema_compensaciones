-- =============================================
-- V105: AGREGAR CAMPO icon_url A CONTENEDORES Y ELEMENTOS
-- =============================================
-- Agrega el campo icon_url para soportar iconos como imágenes (SVG, PNG, etc.)
-- desde Cloudinary, manteniendo compatibilidad con icon (clases de PrimeNG)

-- =============================================
-- CONTENEDORES (Containers)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_containers') AND name = 'icon_url')
BEGIN
    ALTER TABLE app.tbl_containers
    ADD icon_url NVARCHAR(500) NULL; -- URL de la imagen en Cloudinary
END
GO

-- =============================================
-- ELEMENTOS (Elements)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_elements') AND name = 'icon_url')
BEGIN
    ALTER TABLE app.tbl_elements
    ADD icon_url NVARCHAR(500) NULL; -- URL de la imagen en Cloudinary
END
GO

