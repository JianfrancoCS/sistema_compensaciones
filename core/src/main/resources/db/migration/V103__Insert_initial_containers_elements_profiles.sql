-- =============================================
-- V103: INSERTAR DATOS INICIALES DE CONTENEDORES, ELEMENTOS Y PERFILES
-- =============================================
-- Inserta los datos iniciales basados en la estructura del sidebar del frontend

-- =============================================
-- CONTENEDORES (Containers)
-- =============================================
-- Insertar contenedores basados en el sidebar actual
IF NOT EXISTS (SELECT 1 FROM app.tbl_containers WHERE name = 'dashboard')
BEGIN
    INSERT INTO app.tbl_containers (name, display_name, icon, order_index, is_active)
    VALUES ('dashboard', 'Dashboard', 'pi pi-chart-line', 0, 1);
END
GO

IF NOT EXISTS (SELECT 1 FROM app.tbl_containers WHERE name = 'organizations')
BEGIN
    INSERT INTO app.tbl_containers (name, display_name, icon, order_index, is_active)
    VALUES ('organizations', 'Organizaciones', 'pi pi-sitemap', 1, 1);
END
GO

IF NOT EXISTS (SELECT 1 FROM app.tbl_containers WHERE name = 'hiring')
BEGIN
    INSERT INTO app.tbl_containers (name, display_name, icon, order_index, is_active)
    VALUES ('hiring', 'Contratación', 'pi pi-file-edit', 2, 1);
END
GO

IF NOT EXISTS (SELECT 1 FROM app.tbl_containers WHERE name = 'operations')
BEGIN
    INSERT INTO app.tbl_containers (name, display_name, icon, order_index, is_active)
    VALUES ('operations', 'Operaciones', 'pi pi-cog', 3, 1);
END
GO

IF NOT EXISTS (SELECT 1 FROM app.tbl_containers WHERE name = 'attendance')
BEGIN
    INSERT INTO app.tbl_containers (name, display_name, icon, order_index, is_active)
    VALUES ('attendance', 'Asistencia', 'pi pi-clock', 4, 1);
END
GO

IF NOT EXISTS (SELECT 1 FROM app.tbl_containers WHERE name = 'payroll')
BEGIN
    INSERT INTO app.tbl_containers (name, display_name, icon, order_index, is_active)
    VALUES ('payroll', 'Planillas', 'pi pi-wallet', 5, 1);
END
GO

IF NOT EXISTS (SELECT 1 FROM app.tbl_containers WHERE name = 'settings')
BEGIN
    INSERT INTO app.tbl_containers (name, display_name, icon, order_index, is_active)
    VALUES ('settings', 'Configuración', 'pi pi-cog', 6, 1);
END
GO

IF NOT EXISTS (SELECT 1 FROM app.tbl_containers WHERE name = 'security')
BEGIN
    INSERT INTO app.tbl_containers (name, display_name, icon, order_index, is_active)
    VALUES ('security', 'Seguridad', 'pi pi-shield', 7, 1);
END
GO

-- =============================================
-- PERFILES BÁSICOS (Profiles) - DEBE IR PRIMERO
-- =============================================
-- Perfil Administrador (tiene acceso a todo)
IF NOT EXISTS (SELECT 1 FROM app.tbl_profiles WHERE name = 'Administrador')
BEGIN
    INSERT INTO app.tbl_profiles (name, description, is_active)
    VALUES ('Administrador', 'Perfil con acceso completo al sistema', 1);
END
GO

-- Perfil Supervisor (acceso limitado)
IF NOT EXISTS (SELECT 1 FROM app.tbl_profiles WHERE name = 'Supervisor')
BEGIN
    INSERT INTO app.tbl_profiles (name, description, is_active)
    VALUES ('Supervisor', 'Perfil para supervisores de campo', 1);
END
GO

-- Perfil RRHH (Recursos Humanos)
IF NOT EXISTS (SELECT 1 FROM app.tbl_profiles WHERE name = 'RRHH')
BEGIN
    INSERT INTO app.tbl_profiles (name, description, is_active)
    VALUES ('RRHH', 'Perfil para personal de Recursos Humanos', 1);
END
GO

-- =============================================
-- ELEMENTOS (Elements)
-- =============================================
-- Nota: Los elementos NO tienen profile_id directo.
-- La relación con perfiles se hace a través de tbl_profiles_elements (N:N)
-- El container_id es solo para agrupación visual en el frontend (acordeón)

-- Dashboard (sin contenedor padre, es directo)
IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'dashboard')
BEGIN
    INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
    VALUES ('dashboard', 'Dashboard', '/system/dashboard', 'pi pi-chart-line', NULL, 0, 1);
END
GO

-- Organizaciones
DECLARE @container_org_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'organizations');

IF @container_org_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'subsidiaries')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('subsidiaries', 'Fundos', '/system/subsidiaries', 'pi pi-warehouse', @container_org_id, 0, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'areas')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('areas', 'Áreas', '/system/areas', 'pi pi-objects-column', @container_org_id, 1, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'positions')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('positions', 'Cargos', '/system/positions', 'pi pi-briefcase', @container_org_id, 2, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'employees')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('employees', 'Empleados', '/system/employees', 'pi pi-users', @container_org_id, 3, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'organization-chart')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('organization-chart', 'Organigrama', '/system/organization-chart', 'pi pi-sitemap', @container_org_id, 4, 1);
    END
END
GO

-- Contratación
DECLARE @container_hiring_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'hiring');

IF @container_hiring_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'contracts')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('contracts', 'Contratos', '/system/contracts', 'pi pi-file-edit', @container_hiring_id, 0, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'contract-templates')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('contract-templates', 'Plantillas de Contrato', '/system/contract-templates', 'pi pi-copy', @container_hiring_id, 1, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'addendums')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('addendums', 'Adendas', '/system/addendums', 'pi pi-file-plus', @container_hiring_id, 2, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'addendum-templates')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('addendum-templates', 'Plantillas de Adenda', '/system/addendum-templates', 'pi pi-copy', @container_hiring_id, 3, 1);
    END
END
GO

-- Operaciones
DECLARE @container_ops_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'operations');

IF @container_ops_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'labors')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('labors', 'Labores', '/system/labors', 'pi pi-briefcase', @container_ops_id, 0, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'batches')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('batches', 'Lotes', '/system/batches', 'pi pi-th-large', @container_ops_id, 1, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'qr')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('qr', 'Códigos QR', '/system/qr', 'pi pi-qrcode', @container_ops_id, 2, 1);
    END
END
GO

-- Asistencia
DECLARE @container_attendance_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'attendance');

IF @container_attendance_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'attendance')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('attendance', 'Asistencia', '/system/attendance', 'pi pi-clock', @container_attendance_id, 0, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'justifications')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('justifications', 'Justificaciones', '/system/justifications', 'pi pi-file', @container_attendance_id, 1, 1);
    END
END
GO

-- Planillas
DECLARE @container_payroll_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'payroll');

IF @container_payroll_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'payrolls')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('payrolls', 'Planillas', '/system/payrolls', 'pi pi-file-excel', @container_payroll_id, 0, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'periods')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('periods', 'Períodos', '/system/periods', 'pi pi-calendar', @container_payroll_id, 1, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'calendar')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('calendar', 'Calendario', '/system/calendar', 'pi pi-calendar-plus', @container_payroll_id, 2, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'payroll-configurations')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('payroll-configurations', 'Configuración de Planilla', '/system/payroll-configurations/detail', 'pi pi-cog', @container_payroll_id, 3, 1);
    END
END
GO

-- Configuración
DECLARE @container_settings_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'settings');

IF @container_settings_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'company-settings')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('company-settings', 'Mi Empresa', '/system/company', 'pi pi-building', @container_settings_id, 0, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'variables')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('variables', 'Variables', '/system/variables', 'pi pi-code', @container_settings_id, 1, 1);
    END
END
GO

-- Seguridad
DECLARE @container_security_id SMALLINT = (SELECT id FROM app.tbl_containers WHERE name = 'security');

IF @container_security_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'users')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('users', 'Usuarios', '/system/settings/users', 'pi pi-users', @container_security_id, 0, 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'profiles')
    BEGIN
        INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
        VALUES ('profiles', 'Perfiles', '/system/settings/profiles', 'pi pi-id-card', @container_security_id, 1, 1);
    END
END
GO

-- =============================================
-- ASIGNAR ELEMENTOS A PERFILES (vía tbl_profiles_elements)
-- =============================================
-- Esta es la relación N:N que permite que un elemento pertenezca a múltiples perfiles
-- y que un perfil tenga múltiples elementos

-- Obtener IDs de perfiles (ya fueron insertados arriba)
DECLARE @admin_profile_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Administrador');
DECLARE @supervisor_profile_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Supervisor');
DECLARE @rrhh_profile_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'RRHH');

-- Asignar TODOS los elementos al perfil Administrador
IF @admin_profile_id IS NOT NULL
BEGIN
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @admin_profile_id, e.id
    FROM app.tbl_elements e
    WHERE e.deleted_at IS NULL
      AND e.is_active = 1
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @admin_profile_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );
END
GO

-- TODO: Asignar elementos específicos a otros perfiles (Supervisor, RRHH, etc.)
-- según los requerimientos del negocio

