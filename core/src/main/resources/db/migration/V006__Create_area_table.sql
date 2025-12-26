IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_areas' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_areas(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        name NVARCHAR(50) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL
    );

    -- Índices combinados
    CREATE UNIQUE NONCLUSTERED INDEX UQ_areas_name_active
    ON app.tbl_areas(name, deleted_at) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_areas_public_id_active
    ON app.tbl_areas(public_id, deleted_at);
END
GO