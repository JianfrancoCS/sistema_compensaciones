IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_validation_methods' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_validation_methods(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        code NVARCHAR(50) NOT NULL,
        name NVARCHAR(100) NOT NULL,
        regex_pattern NVARCHAR(500) NULL,
        method_type NVARCHAR(20) NOT NULL CHECK (method_type IN ('REGEX', 'COMPARISON', 'LENGTH')),
        requires_value BIT NOT NULL DEFAULT 0,
        description NVARCHAR(200) NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL
    );

    -- Índices únicos parciales (solo para registros activos)
    CREATE UNIQUE NONCLUSTERED INDEX UQ_validation_methods_public_id_active
    ON app.tbl_validation_methods(public_id) WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_validation_methods_code_active
    ON app.tbl_validation_methods(code) WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_validation_methods_name_active
    ON app.tbl_validation_methods(name) WHERE deleted_at IS NULL;

    -- Índices de consulta
    CREATE NONCLUSTERED INDEX IX_validation_methods_method_type
    ON app.tbl_validation_methods(method_type);
END
GO