-- =============================================
-- V102: TABLA HISTÓRICA PROFILE_X_POSITIONS
-- =============================================
-- Crea la tabla histórica que relaciona perfiles con posiciones (cargos).
-- El último registro activo (is_active = 1) es el perfil actual que usa el usuario.
-- Esta tabla permite mantener historial de cambios de perfiles por posición.

-- =============================================
-- PROFILE_X_POSITIONS (Tabla histórica)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_profile_x_positions' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_profile_x_positions(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        profile_id SMALLINT NOT NULL,
        position_id SMALLINT NOT NULL,
        is_active BIT NOT NULL DEFAULT 1, -- El último registro con is_active=1 es el actual
        start_date DATETIME2 NOT NULL DEFAULT GETUTCDATE(), -- Fecha de inicio de vigencia
        end_date DATETIME2 NULL, -- Fecha de fin de vigencia (NULL si está activo)
        notes NVARCHAR(500) NULL, -- Notas sobre el cambio
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        CONSTRAINT FK_profile_x_positions_profile
        FOREIGN KEY (profile_id) REFERENCES app.tbl_profiles(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

        CONSTRAINT FK_profile_x_positions_position
        FOREIGN KEY (position_id) REFERENCES app.tbl_positions(id)
        ON DELETE CASCADE ON UPDATE CASCADE
    );

    -- Índices
    CREATE UNIQUE NONCLUSTERED INDEX UQ_profile_x_positions_public_id_active
    ON app.tbl_profile_x_positions(public_id) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_profile_x_positions_profile
    ON app.tbl_profile_x_positions(profile_id, deleted_at);

    CREATE NONCLUSTERED INDEX IX_profile_x_positions_position
    ON app.tbl_profile_x_positions(position_id, deleted_at);

    -- Índice para obtener el perfil activo actual de una posición
    CREATE NONCLUSTERED INDEX IX_profile_x_positions_position_active
    ON app.tbl_profile_x_positions(position_id, is_active, start_date DESC)
    WHERE is_active = 1 AND deleted_at IS NULL;

    -- Índice para obtener historial completo de una posición
    CREATE NONCLUSTERED INDEX IX_profile_x_positions_position_historical
    ON app.tbl_profile_x_positions(position_id, start_date DESC, deleted_at)
    WHERE deleted_at IS NULL;

    -- Índice para obtener historial completo de un perfil
    CREATE NONCLUSTERED INDEX IX_profile_x_positions_profile_historical
    ON app.tbl_profile_x_positions(profile_id, start_date DESC, deleted_at)
    WHERE deleted_at IS NULL;
END
GO

