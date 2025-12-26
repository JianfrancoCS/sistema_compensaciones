-- =============================================
-- V128: AGREGAR ELEMENTO DE RESPONSABLES DE FIRMA
-- =============================================
-- Agrega el elemento para gestionar responsables de firma de boletas de pago
-- Se agrega al contenedor de "Configuración" ya que es una configuración del sistema

DECLARE @container_settings_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'settings');
IF @container_settings_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'subsidiary-signers')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('subsidiary-signers', 'Responsables de Firma', '/system/subsidiary-signers', 'pi pi-pencil', @container_settings_id, 0, 1);
    END
END
GO

-- =============================================
-- ASIGNAR ELEMENTO AL PERFIL ADMINISTRADOR
-- =============================================
DECLARE @admin_profile_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Administrador');
DECLARE @signer_element_id SMALLINT = (SELECT id FROM app.tbl_elements WHERE name = 'subsidiary-signers');

IF @admin_profile_id IS NOT NULL AND @signer_element_id IS NOT NULL
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM app.tbl_profiles_elements 
        WHERE profile_id = @admin_profile_id 
        AND element_id = @signer_element_id 
        AND deleted_at IS NULL
    )
    BEGIN
        INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
        VALUES (@admin_profile_id, @signer_element_id);
    END
END
GO

