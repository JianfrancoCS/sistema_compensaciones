-- =============================================
-- V101: TABLAS DE USUARIOS, PERFILES, CONTENEDORES Y ELEMENTOS
-- =============================================
-- Crea las tablas para el sistema de permisos basado en perfiles:
-- - users: Usuarios del sistema (vinculados a empleados)
-- - profiles: Perfiles de acceso (normalmente asociados a un cargo/position)
-- - containers: Contenedores (acordeones en el frontend)
-- - elements: Elementos (botones finales en el frontend)

-- =============================================
-- PERFILES (Profiles)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_profiles' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_profiles(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        name NVARCHAR(100) NOT NULL,
        description NVARCHAR(500) NULL,
        -- NOTA: La relación con positions se maneja a través de tbl_profile_x_positions (tabla histórica)
        -- NO usar position_id directo aquí
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL
    );

    -- Índices
    CREATE UNIQUE NONCLUSTERED INDEX UQ_profiles_public_id_active
    ON app.tbl_profiles(public_id) WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_profiles_name_active
    ON app.tbl_profiles(name) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_profiles_deleted_at
    ON app.tbl_profiles(deleted_at);
END
GO

-- =============================================
-- CONTENEDORES (Containers - Acordeones del frontend)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_containers' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_containers(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        name NVARCHAR(100) NOT NULL, -- Identificador único (ej: 'organizations', 'hiring')
        display_name NVARCHAR(150) NOT NULL, -- Nombre para mostrar (ej: 'Organizaciones')
        icon NVARCHAR(100) NULL, -- Icono de PrimeNG (ej: 'pi pi-sitemap')
        order_index INT NOT NULL DEFAULT 0, -- Orden de aparición en el sidebar
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL
    );

    -- Índices
    CREATE UNIQUE NONCLUSTERED INDEX UQ_containers_public_id_active
    ON app.tbl_containers(public_id) WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_containers_name_active
    ON app.tbl_containers(name) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_containers_order_active
    ON app.tbl_containers(order_index, deleted_at) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_containers_deleted_at
    ON app.tbl_containers(deleted_at);
END
GO

-- =============================================
-- ELEMENTOS (Elements - Botones finales del frontend)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_elements' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_elements(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        name NVARCHAR(100) NOT NULL, -- Identificador único (ej: 'subsidiaries', 'areas')
        display_name NVARCHAR(150) NOT NULL, -- Nombre para mostrar (ej: 'Fundos')
        route NVARCHAR(255) NULL, -- Ruta del frontend (ej: '/system/subsidiaries')
        icon NVARCHAR(100) NULL, -- Icono de PrimeNG (ej: 'pi pi-warehouse')
        container_id SMALLINT NULL, -- FK a tbl_containers (acordeón padre - para agrupación visual)
        order_index INT NOT NULL DEFAULT 0, -- Orden dentro del contenedor
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        CONSTRAINT FK_elements_container
        FOREIGN KEY (container_id) REFERENCES app.tbl_containers(id)
        ON DELETE SET NULL ON UPDATE CASCADE
    );

    -- Índices
    CREATE UNIQUE NONCLUSTERED INDEX UQ_elements_public_id_active
    ON app.tbl_elements(public_id) WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_elements_name_active
    ON app.tbl_elements(name) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_elements_container_active
    ON app.tbl_elements(container_id, order_index, deleted_at) WHERE container_id IS NOT NULL AND deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_elements_route_active
    ON app.tbl_elements(route, deleted_at) WHERE route IS NOT NULL AND deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_elements_deleted_at
    ON app.tbl_elements(deleted_at);
END
GO

-- =============================================
-- PERFILES X ELEMENTOS (Relación N:N - Qué elementos tiene cada perfil)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_profiles_elements' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_profiles_elements(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        profile_id SMALLINT NOT NULL,
        element_id SMALLINT NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        CONSTRAINT FK_profiles_elements_profile
        FOREIGN KEY (profile_id) REFERENCES app.tbl_profiles(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

        CONSTRAINT FK_profiles_elements_element
        FOREIGN KEY (element_id) REFERENCES app.tbl_elements(id)
        ON DELETE CASCADE ON UPDATE CASCADE
    );

    -- Índices
    CREATE UNIQUE NONCLUSTERED INDEX UQ_profiles_elements_public_id_active
    ON app.tbl_profiles_elements(public_id) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_profiles_elements_profile
    ON app.tbl_profiles_elements(profile_id, deleted_at);

    CREATE NONCLUSTERED INDEX IX_profiles_elements_element
    ON app.tbl_profiles_elements(element_id, deleted_at);

    CREATE UNIQUE NONCLUSTERED INDEX UQ_profiles_elements_activo
    ON app.tbl_profiles_elements(profile_id, element_id)
    WHERE deleted_at IS NULL;
END
GO

-- =============================================
-- USUARIOS (Users - Opcionalmente vinculados a empleados)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_users' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_users(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        employee_id NVARCHAR(15) NULL, -- FK a tbl_employees (person_document_number) - NULL permitido
        profile_id SMALLINT NULL, -- FK a tbl_profiles (perfil actual del usuario)
        username NVARCHAR(100) NOT NULL, -- Username único (normalmente el DNI)
        password_hash NVARCHAR(255) NOT NULL, -- Hash de la contraseña (bcrypt)
        is_active BIT NOT NULL DEFAULT 1,
        last_login_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        CONSTRAINT FK_users_employee
        FOREIGN KEY (employee_id) REFERENCES app.tbl_employees(person_document_number)
        ON DELETE SET NULL ON UPDATE CASCADE,

        CONSTRAINT FK_users_profile
        FOREIGN KEY (profile_id) REFERENCES app.tbl_profiles(id)
        ON DELETE SET NULL ON UPDATE CASCADE
    );

    -- Índices
    CREATE UNIQUE NONCLUSTERED INDEX UQ_users_public_id_active
    ON app.tbl_users(public_id) WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_users_username_active
    ON app.tbl_users(username) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_users_employee_active
    ON app.tbl_users(employee_id, deleted_at) WHERE employee_id IS NOT NULL AND deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_users_profile_active
    ON app.tbl_users(profile_id, deleted_at) WHERE profile_id IS NOT NULL AND deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_users_deleted_at
    ON app.tbl_users(deleted_at);
END
GO

