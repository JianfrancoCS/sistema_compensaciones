-- =============================================
-- V116: CREAR PERFIL BASE "COLABORADOR" Y ELEMENTOS
-- =============================================
-- Perfil base que todos los usuarios tendrán, permite:
-- - Ver/editar perfil personal
-- - Consultar boletas de pago (con filtros por fecha y período)

-- =============================================
-- CREAR PERFIL COLABORADOR
-- =============================================
IF NOT EXISTS (SELECT 1 FROM app.tbl_profiles WHERE name = 'Colaborador')
BEGIN
    INSERT INTO app.tbl_profiles (name, description, is_active)
    VALUES ('Colaborador', 'Perfil base para todos los colaboradores. Permite ver perfil personal y consultar boletas de pago', 1);
END
GO

-- =============================================
-- CREAR ELEMENTOS PARA COLABORADOR
-- =============================================

-- Elemento: Mi Perfil (sin contenedor, directo)
IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'my-profile')
BEGIN
    INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
    VALUES ('my-profile', 'Mi Perfil', '/system/my-profile', 'pi pi-user', NULL, 0, 1);
END
GO

-- Elemento: Boletas (sin contenedor, directo)
IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'payslips')
BEGIN
    INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
    VALUES ('payslips', 'Mis Boletas', '/system/payslips', 'pi pi-file-pdf', NULL, 1, 1);
END
GO

-- =============================================
-- ASIGNAR ELEMENTOS AL PERFIL COLABORADOR
-- =============================================
DECLARE @colaborador_profile_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Colaborador' AND deleted_at IS NULL);

IF @colaborador_profile_id IS NOT NULL
BEGIN
    -- Asignar elemento: Mi Perfil
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @colaborador_profile_id, e.id
    FROM app.tbl_elements e
    WHERE e.name = 'my-profile'
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @colaborador_profile_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );

    -- Asignar elemento: Boletas
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @colaborador_profile_id, e.id
    FROM app.tbl_elements e
    WHERE e.name = 'payslips'
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @colaborador_profile_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );
END
GO

-- =============================================
-- ASIGNAR PERFIL COLABORADOR A TODOS LOS USUARIOS EXISTENTES
-- =============================================
-- Todos los usuarios existentes tendrán el perfil Colaborador como perfil adicional
DECLARE @colaborador_profile_id_users SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Colaborador' AND deleted_at IS NULL);

IF @colaborador_profile_id_users IS NOT NULL
BEGIN
    -- Asignar perfil Colaborador a todos los usuarios que no lo tengan ya
    INSERT INTO app.tbl_users_profiles (user_id, profile_id, is_active)
    SELECT u.id, @colaborador_profile_id_users, 1
    FROM app.tbl_users u
    WHERE u.deleted_at IS NULL
      AND u.is_active = 1
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_users_profiles up
          WHERE up.user_id = u.id
            AND up.profile_id = @colaborador_profile_id_users
            AND up.deleted_at IS NULL
      );
END
GO

