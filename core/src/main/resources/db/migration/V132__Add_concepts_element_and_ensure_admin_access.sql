-- =============================================
-- Agregar elemento de Conceptos al contenedor de Configuración
-- y asegurar que esté asignado al perfil Administrador
-- =============================================

DECLARE @container_settings_id SMALLINT;
SELECT @container_settings_id = id FROM app.tbl_containers WHERE name = 'settings' AND deleted_at IS NULL;

IF @container_settings_id IS NOT NULL
BEGIN
    -- Insertar el elemento de Conceptos si no existe
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'concepts' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('concepts', 'Conceptos', '/system/settings/concepts', 'pi pi-list', @container_settings_id, 2, 1);
    END
    ELSE
    BEGIN
        -- Actualizar el elemento si ya existe pero no está en el contenedor correcto
        UPDATE app.tbl_elements
        SET container_id = @container_settings_id,
            route = '/system/settings/concepts',
            icon = 'pi pi-list',
            order_index = 2,
            is_active = 1
        WHERE name = 'concepts' AND deleted_at IS NULL;
    END
END
GO

-- =============================================
-- Asignar el elemento de Conceptos al perfil Administrador
-- =============================================
DECLARE @admin_profile_id SMALLINT;
DECLARE @concepts_element_id SMALLINT;

SELECT @admin_profile_id = id FROM app.tbl_profiles WHERE name = 'Administrador' AND deleted_at IS NULL;
SELECT @concepts_element_id = id FROM app.tbl_elements WHERE name = 'concepts' AND deleted_at IS NULL;

IF @admin_profile_id IS NOT NULL AND @concepts_element_id IS NOT NULL
BEGIN
    -- Asignar el elemento al perfil Administrador si no está ya asignado
    IF NOT EXISTS (
        SELECT 1 FROM app.tbl_profiles_elements
        WHERE profile_id = @admin_profile_id
          AND element_id = @concepts_element_id
          AND deleted_at IS NULL
    )
    BEGIN
        INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
        VALUES (@admin_profile_id, @concepts_element_id);
    END
END
GO

