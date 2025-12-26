IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_domains' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_domains(
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

    -- Índices
    CREATE UNIQUE NONCLUSTERED INDEX UQ_tbl_domains_name_active
    ON app.tbl_domains(name)
    WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_tbl_domains_public_id
    ON app.tbl_domains(public_id);
END
GO