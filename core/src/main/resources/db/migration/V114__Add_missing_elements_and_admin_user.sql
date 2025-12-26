-- =============================================
-- V114: AGREGAR ELEMENTOS FALTANTES Y USUARIO ADMIN
-- =============================================
-- Agrega los elementos faltantes del frontend y crea el usuario admin
-- con acceso a todos los elementos a través del perfil Administrador

-- =============================================
-- AGREGAR ELEMENTOS FALTANTES
-- =============================================

-- Organizaciones: Foreign Persons
DECLARE @container_org_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'organizations');
IF @container_org_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'foreign-persons')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('foreign-persons', 'Personas Extranjeras', '/system/foreign-persons', 'pi pi-globe', @container_org_id, 4, 1);
    END
END
GO

-- Asistencia: Attendance Entry y Exit
DECLARE @container_attendance_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'attendance');
IF @container_attendance_id IS NOT NULL
BEGIN
    -- Actualizar el elemento 'attendance' genérico a 'attendance-entry'
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'attendance' AND route = '/system/attendance')
    BEGIN
        UPDATE app.tbl_elements
        SET name = 'attendance-entry',
            display_name = 'Registro de Entrada',
            route = '/system/attendance/entry'
        WHERE name = 'attendance' AND route = '/system/attendance';
    END
    
    -- Agregar attendance-entry si no existe
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'attendance-entry')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('attendance-entry', 'Registro de Entrada', '/system/attendance/entry', 'pi pi-sign-in', @container_attendance_id, 0, 1);
    END
    
    -- Agregar attendance-exit
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'attendance-exit')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('attendance-exit', 'Registro de Salida', '/system/attendance/exit', 'pi pi-sign-out', @container_attendance_id, 1, 1);
    END
END
GO

-- Operaciones: Labor Units
DECLARE @container_ops_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'operations');
IF @container_ops_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'labor-units')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('labor-units', 'Unidades de Trabajo', '/system/labor-units', 'pi pi-th-large', @container_ops_id, 3, 1);
    END
END
GO

-- Configuración: Corregir rutas de settings
-- Seguridad: Mover containers, elements, profiles a security y corregir rutas
DECLARE @container_security_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'security');
IF @container_security_id IS NOT NULL
BEGIN
    -- Actualizar ruta de users
    UPDATE app.tbl_elements
    SET route = '/system/settings/users'
    WHERE name = 'users' AND route = '/system/users';
    
    -- Mover containers de settings a security si existe en settings
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'containers' AND container_id != @container_security_id)
    BEGIN
        UPDATE app.tbl_elements
        SET container_id = @container_security_id,
            route = '/system/settings/containers',
            order_index = 2
        WHERE name = 'containers';
    END
    ELSE IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'containers')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('containers', 'Contenedores', '/system/settings/containers', 'pi pi-folder', @container_security_id, 2, 1);
    END
    
    -- Mover elements de settings a security si existe en settings
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'elements' AND container_id != @container_security_id)
    BEGIN
        UPDATE app.tbl_elements
        SET container_id = @container_security_id,
            route = '/system/settings/elements',
            order_index = 3
        WHERE name = 'elements';
    END
    ELSE IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'elements')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('elements', 'Elementos', '/system/settings/elements', 'pi pi-list', @container_security_id, 3, 1);
    END
    
    -- Mover profiles de settings a security o cambiar roles por profiles
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'roles')
    BEGIN
        UPDATE app.tbl_elements
        SET name = 'profiles',
            display_name = 'Perfiles',
            route = '/system/settings/profiles',
            icon = 'pi pi-id-card',
            container_id = @container_security_id,
            order_index = 4
        WHERE name = 'roles';
    END
    ELSE IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'profiles' AND container_id != @container_security_id)
    BEGIN
        UPDATE app.tbl_elements
        SET container_id = @container_security_id,
            route = '/system/settings/profiles',
            order_index = 4
        WHERE name = 'profiles';
    END
    ELSE IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'profiles')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('profiles', 'Perfiles', '/system/settings/profiles', 'pi pi-id-card', @container_security_id, 4, 1);
    END
END
GO

-- =============================================
-- CREAR USUARIO ADMIN
-- =============================================
-- Usuario admin sin empleado asociado, con perfil Administrador
IF NOT EXISTS (SELECT 1 FROM app.tbl_users WHERE username = 'admin')
BEGIN
    DECLARE @admin_profile_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Administrador');
    
    -- Password: 'admin123' hasheado con bcrypt (10 rounds)
    -- Hash generado: $2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG
    INSERT INTO app.tbl_users (employee_id, profile_id, username, password_hash, is_active)
    VALUES (
        NULL, -- Sin empleado asociado
        @admin_profile_id, -- Perfil Administrador
        'admin',
        '$2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG', -- admin123
        1
    );
END
GO

-- =============================================
-- ASIGNAR NUEVOS ELEMENTOS AL PERFIL ADMINISTRADOR
-- =============================================
-- Asegurar que todos los elementos (incluyendo los nuevos) estén asignados al perfil Administrador
DECLARE @admin_profile_id_final SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Administrador');

IF @admin_profile_id_final IS NOT NULL
BEGIN
    -- Insertar todos los elementos activos que no estén ya asignados
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @admin_profile_id_final, e.id
    FROM app.tbl_elements e
    WHERE e.deleted_at IS NULL
      AND e.is_active = 1
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @admin_profile_id_final
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );
END
GO

