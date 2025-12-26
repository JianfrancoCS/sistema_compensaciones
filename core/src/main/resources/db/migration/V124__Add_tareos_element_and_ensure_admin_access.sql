-- =============================================
-- V124: AGREGAR ELEMENTO TAREOS Y ASEGURAR ACCESO ADMIN
-- =============================================
-- Agrega el elemento de tareos al contenedor de operaciones
-- y asegura que planillas, períodos y tareos estén asignados al perfil admin

-- Obtener contenedor de operaciones
DECLARE @container_operations_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'operations' AND deleted_at IS NULL);

IF @container_operations_id IS NOT NULL
BEGIN
    -- Agregar elemento de tareos si no existe
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'tareos' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active, is_web, is_mobile, is_desktop)
        VALUES ('tareos', 'Tareos', '/system/tareos', 'pi pi-list-check', @container_operations_id, 5, 1, 1, 0, 0);
        
        PRINT 'Elemento ''tareos'' creado en operations';
    END
    ELSE
    BEGIN
        -- Actualizar si ya existe pero no está en el contenedor correcto
        UPDATE app.tbl_elements
        SET container_id = @container_operations_id,
            order_index = 5,
            is_web = 1,
            is_mobile = 0,
            is_desktop = 0
        WHERE name = 'tareos' AND deleted_at IS NULL;
        
        PRINT 'Elemento ''tareos'' actualizado en operations';
    END
END
GO

-- Asegurar que planillas, períodos y tareos estén asignados al perfil Administrador
DECLARE @admin_profile_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Administrador' AND deleted_at IS NULL);

IF @admin_profile_id IS NOT NULL
BEGIN
    -- Asignar planillas si no está asignado
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'payrolls' AND deleted_at IS NULL)
    BEGIN
        DECLARE @payrolls_element_id SMALLINT = (SELECT id FROM app.tbl_elements WHERE name = 'payrolls' AND deleted_at IS NULL);
        
        IF NOT EXISTS (SELECT 1 FROM app.tbl_profiles_elements 
                       WHERE profile_id = @admin_profile_id 
                       AND element_id = @payrolls_element_id 
                       AND deleted_at IS NULL)
        BEGIN
            INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
            VALUES (@admin_profile_id, @payrolls_element_id);
            
            PRINT 'Elemento ''payrolls'' asignado al perfil Administrador';
        END
    END
    
    -- Asignar períodos si no está asignado
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'periods' AND deleted_at IS NULL)
    BEGIN
        DECLARE @periods_element_id SMALLINT = (SELECT id FROM app.tbl_elements WHERE name = 'periods' AND deleted_at IS NULL);
        
        IF NOT EXISTS (SELECT 1 FROM app.tbl_profiles_elements 
                       WHERE profile_id = @admin_profile_id 
                       AND element_id = @periods_element_id 
                       AND deleted_at IS NULL)
        BEGIN
            INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
            VALUES (@admin_profile_id, @periods_element_id);
            
            PRINT 'Elemento ''periods'' asignado al perfil Administrador';
        END
    END
    
    -- Asignar tareos si no está asignado
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'tareos' AND deleted_at IS NULL)
    BEGIN
        DECLARE @tareos_element_id SMALLINT = (SELECT id FROM app.tbl_elements WHERE name = 'tareos' AND deleted_at IS NULL);
        
        IF NOT EXISTS (SELECT 1 FROM app.tbl_profiles_elements 
                       WHERE profile_id = @admin_profile_id 
                       AND element_id = @tareos_element_id 
                       AND deleted_at IS NULL)
        BEGIN
            INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
            VALUES (@admin_profile_id, @tareos_element_id);
            
            PRINT 'Elemento ''tareos'' asignado al perfil Administrador';
        END
    END
END
GO

PRINT 'Migración V124 completada: elemento tareos agregado y acceso admin asegurado';
GO

