IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_dynamic_variables' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_dynamic_variables(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        code NVARCHAR(50) NOT NULL,
        name NVARCHAR(100) NOT NULL,
        final_regex NVARCHAR(MAX) NULL, -- Se genera automáticamente
        error_message NVARCHAR(500) NULL,
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL
    );

    -- Índices únicos parciales (solo para registros activos)
    CREATE UNIQUE NONCLUSTERED INDEX UQ_dynamic_variables_public_id_active
    ON app.tbl_dynamic_variables(public_id) WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_dynamic_variables_code_active
    ON app.tbl_dynamic_variables(code) WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_dynamic_variables_name_active
    ON app.tbl_dynamic_variables(name) WHERE deleted_at IS NULL;

    -- Índices de consulta
    CREATE NONCLUSTERED INDEX IX_dynamic_variables_is_active
    ON app.tbl_dynamic_variables(is_active);
END
GO