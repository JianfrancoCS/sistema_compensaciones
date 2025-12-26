IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_departments' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_departments (
        id INT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        name NVARCHAR(100) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL
    );

    -- Índices combinados con deleted_at
    CREATE UNIQUE NONCLUSTERED INDEX UQ_departments_name_active
    ON app.tbl_departments(name, deleted_at) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_departments_public_id_active
    ON app.tbl_departments(public_id, deleted_at);

    CREATE NONCLUSTERED INDEX IX_departments_created_at_active
    ON app.tbl_departments(created_at, deleted_at);
END
GO