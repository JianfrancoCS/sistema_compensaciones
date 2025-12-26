-- =============================================
-- V120: MOVER CONTAINERS, ELEMENTS Y PROFILES A SECURITY
-- =============================================
-- Mueve los elementos containers, elements y profiles del contenedor 'settings' 
-- al contenedor 'security' donde deben estar junto con users

DECLARE @container_security_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'security');
DECLARE @container_settings_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'settings');

IF @container_security_id IS NOT NULL
BEGIN
    -- Mover containers a security
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'containers' AND container_id = @container_settings_id AND deleted_at IS NULL)
    BEGIN
        UPDATE app.tbl_elements
        SET container_id = @container_security_id,
            order_index = 2
        WHERE name = 'containers' AND container_id = @container_settings_id AND deleted_at IS NULL;
        
        PRINT 'Elemento ''containers'' movido de settings a security';
    END
    ELSE IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'containers' AND container_id = @container_security_id AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('containers', 'Contenedores', '/system/settings/containers', 'pi pi-folder', @container_security_id, 2, 1);
        
        PRINT 'Elemento ''containers'' creado en security';
    END
    
    -- Mover elements a security
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'elements' AND container_id = @container_settings_id AND deleted_at IS NULL)
    BEGIN
        UPDATE app.tbl_elements
        SET container_id = @container_security_id,
            order_index = 3
        WHERE name = 'elements' AND container_id = @container_settings_id AND deleted_at IS NULL;
        
        PRINT 'Elemento ''elements'' movido de settings a security';
    END
    ELSE IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'elements' AND container_id = @container_security_id AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('elements', 'Elementos', '/system/settings/elements', 'pi pi-list', @container_security_id, 3, 1);
        
        PRINT 'Elemento ''elements'' creado en security';
    END
    
    -- Mover profiles a security (o actualizar si viene de roles)
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'profiles' AND container_id = @container_settings_id AND deleted_at IS NULL)
    BEGIN
        UPDATE app.tbl_elements
        SET container_id = @container_security_id,
            order_index = 4
        WHERE name = 'profiles' AND container_id = @container_settings_id AND deleted_at IS NULL;
        
        PRINT 'Elemento ''profiles'' movido de settings a security';
    END
    ELSE IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'profiles' AND container_id = @container_security_id AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('profiles', 'Perfiles', '/system/settings/profiles', 'pi pi-id-card', @container_security_id, 4, 1);
        
        PRINT 'Elemento ''profiles'' creado en security';
    END
END
GO

PRINT 'Migración V120 completada: containers, elements y profiles movidos a security';
GO

