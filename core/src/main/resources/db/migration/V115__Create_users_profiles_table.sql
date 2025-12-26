-- =============================================
-- V115: TABLA INTERMEDIA USUARIOS X PERFILES (Relación N:N)
-- =============================================
-- Permite que un usuario tenga múltiples perfiles (perfiles temporales)
-- El campo profile_id en tbl_users sigue siendo el perfil principal/por defecto
-- Esta tabla permite agregar perfiles adicionales sin afectar el principal

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_users_profiles' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_users_profiles(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        user_id SMALLINT NOT NULL, -- FK a tbl_users
        profile_id SMALLINT NOT NULL, -- FK a tbl_profiles
        is_active BIT NOT NULL DEFAULT 1, -- Permite desactivar temporalmente sin eliminar
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        CONSTRAINT FK_users_profiles_user
        FOREIGN KEY (user_id) REFERENCES app.tbl_users(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION,

        CONSTRAINT FK_users_profiles_profile
        FOREIGN KEY (profile_id) REFERENCES app.tbl_profiles(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
    );

    -- Índices
    CREATE UNIQUE NONCLUSTERED INDEX UQ_users_profiles_public_id_active
    ON app.tbl_users_profiles(public_id) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_users_profiles_user
    ON app.tbl_users_profiles(user_id, deleted_at) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_users_profiles_profile
    ON app.tbl_users_profiles(profile_id, deleted_at) WHERE deleted_at IS NULL;

    -- Evitar duplicados: un usuario no puede tener el mismo perfil activo dos veces
    CREATE UNIQUE NONCLUSTERED INDEX UQ_users_profiles_user_profile_active
    ON app.tbl_users_profiles(user_id, profile_id)
    WHERE deleted_at IS NULL AND is_active = 1;

    CREATE NONCLUSTERED INDEX IX_users_profiles_deleted_at
    ON app.tbl_users_profiles(deleted_at);
END
GO

-- =============================================
-- ASIGNAR ELEMENTOS A PERFILES RRHH Y SUPERVISOR
-- =============================================
-- Asignar elementos específicos a cada perfil para demostrar la funcionalidad

DECLARE @rrhh_profile_id SMALLINT;
DECLARE @supervisor_profile_id SMALLINT;

SET @rrhh_profile_id = (SELECT id FROM app.tbl_profiles WHERE name = 'RRHH' AND deleted_at IS NULL);
SET @supervisor_profile_id = (SELECT id FROM app.tbl_profiles WHERE name = 'Supervisor' AND deleted_at IS NULL);

-- Asignar elementos al perfil RRHH (Recursos Humanos)
-- RRHH tiene acceso a: Dashboard, Organizaciones (employees, positions), Contratación (contracts, addendums), Asistencia (attendance, justifications)
IF @rrhh_profile_id IS NOT NULL
BEGIN
    -- Dashboard
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @rrhh_profile_id, e.id
    FROM app.tbl_elements e
    WHERE e.name = 'dashboard'
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @rrhh_profile_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );

    -- Organizaciones: employees, positions
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @rrhh_profile_id, e.id
    FROM app.tbl_elements e
    WHERE e.name IN ('employees', 'positions')
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @rrhh_profile_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );

    -- Contratación: contracts, addendums
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @rrhh_profile_id, e.id
    FROM app.tbl_elements e
    WHERE e.name IN ('contracts', 'addendums')
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @rrhh_profile_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );

    -- Asistencia: attendance-entry, attendance-exit, justifications
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @rrhh_profile_id, e.id
    FROM app.tbl_elements e
    WHERE e.name IN ('attendance-entry', 'attendance-exit', 'justifications')
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @rrhh_profile_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );
END
GO

-- Asignar elementos al perfil Supervisor
-- Supervisor tiene acceso a: Dashboard, Operaciones (labors, labor-units, batches), Asistencia (attendance-entry, attendance-exit)
DECLARE @supervisor_profile_id2 SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Supervisor' AND deleted_at IS NULL);

IF @supervisor_profile_id2 IS NOT NULL
BEGIN
    -- Dashboard
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @supervisor_profile_id2, e.id
    FROM app.tbl_elements e
    WHERE e.name = 'dashboard'
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @supervisor_profile_id2
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );

    -- Operaciones: labors, labor-units, batches
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @supervisor_profile_id2, e.id
    FROM app.tbl_elements e
    WHERE e.name IN ('labors', 'labor-units', 'batches')
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @supervisor_profile_id2
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );

    -- Asistencia: attendance-entry, attendance-exit
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @supervisor_profile_id2, e.id
    FROM app.tbl_elements e
    WHERE e.name IN ('attendance-entry', 'attendance-exit')
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @supervisor_profile_id2
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );
END
GO

-- =============================================
-- CREAR USUARIOS DE EJEMPLO CON MÚLTIPLES PERFILES
-- =============================================

-- Usuario 1: jefe_rrhh
-- Perfil principal: RRHH
-- Perfil adicional: Supervisor (para demostrar múltiples perfiles)
-- Este usuario tendrá acceso a elementos de RRHH + elementos de Supervisor
IF NOT EXISTS (SELECT 1 FROM app.tbl_users WHERE username = 'jefe_rrhh')
BEGIN
    DECLARE @jefe_rrhh_user_id SMALLINT;
    DECLARE @rrhh_profile_id_user SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'RRHH' AND deleted_at IS NULL);
    DECLARE @supervisor_profile_id_user SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Supervisor' AND deleted_at IS NULL);
    
    -- Password: 'rrhh123' hasheado con bcrypt (10 rounds)
    -- Hash generado usando bcrypt: $2a$10$rKqXqXqXqXqXqXqXqXqXeKqXqXqXqXqXqXqXqXqXqXqXqXqXqXqXq
    -- Nota: Este hash es un placeholder. En producción, generar con: BCrypt.hashpw("rrhh123", BCrypt.gensalt(10))
    INSERT INTO app.tbl_users (employee_id, profile_id, username, password_hash, is_active)
    VALUES (
        NULL, -- Sin empleado asociado (puede ser admin de RRHH)
        @rrhh_profile_id_user, -- Perfil principal: RRHH
        'jefe_rrhh',
        '$2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG', -- rrhh123 (mismo hash que admin123 por ahora, cambiar en producción)
        1
    );
    
    SET @jefe_rrhh_user_id = SCOPE_IDENTITY();
    
    -- Asignar perfil adicional: Supervisor (perfil temporal)
    IF @supervisor_profile_id_user IS NOT NULL AND @jefe_rrhh_user_id IS NOT NULL
    BEGIN
        INSERT INTO app.tbl_users_profiles (user_id, profile_id, is_active)
        VALUES (@jefe_rrhh_user_id, @supervisor_profile_id_user, 1);
    END
END
GO

-- Usuario 2: supervisor_campo
-- Perfil principal: Supervisor
-- Perfil adicional: RRHH (para demostrar múltiples perfiles)
-- Este usuario tendrá acceso a elementos de Supervisor + elementos de RRHH
IF NOT EXISTS (SELECT 1 FROM app.tbl_users WHERE username = 'supervisor_campo')
BEGIN
    DECLARE @supervisor_campo_user_id SMALLINT;
    DECLARE @rrhh_profile_id_user2 SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'RRHH' AND deleted_at IS NULL);
    DECLARE @supervisor_profile_id_user2 SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Supervisor' AND deleted_at IS NULL);
    
    -- Password: 'super123' hasheado con bcrypt (10 rounds)
    -- Nota: Este hash es un placeholder. En producción, generar con: BCrypt.hashpw("super123", BCrypt.gensalt(10))
    INSERT INTO app.tbl_users (employee_id, profile_id, username, password_hash, is_active)
    VALUES (
        NULL, -- Sin empleado asociado
        @supervisor_profile_id_user2, -- Perfil principal: Supervisor
        'supervisor_campo',
        '$2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG', -- super123 (mismo hash que admin123 por ahora, cambiar en producción)
        1
    );
    
    SET @supervisor_campo_user_id = SCOPE_IDENTITY();
    
    -- Asignar perfil adicional: RRHH (perfil temporal)
    IF @rrhh_profile_id_user2 IS NOT NULL AND @supervisor_campo_user_id IS NOT NULL
    BEGIN
        INSERT INTO app.tbl_users_profiles (user_id, profile_id, is_active)
        VALUES (@supervisor_campo_user_id, @rrhh_profile_id_user2, 1);
    END
END
GO


