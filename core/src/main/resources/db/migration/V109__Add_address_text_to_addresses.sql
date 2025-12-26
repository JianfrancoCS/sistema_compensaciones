-- =============================================
-- V109: AGREGAR CAMPO DE DIRECCIÓN TEXTUAL A ADDRESSES
-- =============================================
-- Agrega campo para almacenar la dirección completa en texto
-- necesario para generar boletas de pago con información de empresa
-- NOTA: El campo address_text ya se crea en V108.5, esta migración
--       solo existe para mantener compatibilidad con versiones anteriores
--       que puedan haber creado la tabla sin este campo

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tb_addresses') AND name = 'address_text')
BEGIN
    ALTER TABLE app.tb_addresses
    ADD address_text NVARCHAR(500) NULL; -- Dirección completa en texto (calle, número, etc.)
END
GO

