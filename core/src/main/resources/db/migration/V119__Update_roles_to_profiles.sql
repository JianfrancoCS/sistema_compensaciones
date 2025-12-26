-- =============================================
-- V119: ACTUALIZAR ROLES A PERFILES
-- =============================================
-- Cambia el elemento 'roles' por 'profiles' en la base de datos
-- para reflejar que ahora usamos perfiles en lugar de roles

DECLARE @container_security_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'security');

IF @container_security_id IS NOT NULL
BEGIN
    -- Si existe el elemento 'roles' y NO existe 'profiles', actualizar 'roles' a 'profiles'
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'roles' AND deleted_at IS NULL)
       AND NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'profiles' AND deleted_at IS NULL)
    BEGIN
        -- Actualizar el elemento 'roles' a 'profiles'
        UPDATE app.tbl_elements
        SET name = 'profiles',
            display_name = 'Perfiles',
            route = '/system/settings/profiles',
            icon = 'pi pi-id-card'
        WHERE name = 'roles' AND deleted_at IS NULL;
        
        PRINT 'Elemento ''roles'' actualizado a ''profiles''';
    END
    -- Si existe 'roles' y también existe 'profiles', eliminar 'roles' (soft delete)
    ELSE IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'roles' AND deleted_at IS NULL)
         AND EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'profiles' AND deleted_at IS NULL)
    BEGIN
        -- Soft delete del elemento 'roles' ya que 'profiles' ya existe
        UPDATE app.tbl_elements
        SET deleted_at = GETUTCDATE(),
            deleted_by = 'SYSTEM'
        WHERE name = 'roles' AND deleted_at IS NULL;
        
        PRINT 'Elemento ''roles'' eliminado (soft delete) porque ''profiles'' ya existe';
    END
    -- Si no existe 'roles' ni 'profiles', crear 'profiles'
    ELSE IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'profiles' AND deleted_at IS NULL)
         AND NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'roles' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('profiles', 'Perfiles', '/system/settings/profiles', 'pi pi-id-card', @container_security_id, 1, 1);
        
        PRINT 'Elemento ''profiles'' creado en el contenedor de seguridad';
    END
END
GO

PRINT 'Migración V119 completada: Elemento ''roles'' actualizado/eliminado, ''profiles'' verificado';
GO

