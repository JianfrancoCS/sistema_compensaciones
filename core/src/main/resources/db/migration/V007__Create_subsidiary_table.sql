IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_subsidiaries' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_subsidiaries(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        name NVARCHAR(50) NOT NULL,

        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        company_id BIGINT NULL,

        CONSTRAINT FK_tbl_subsidiaries_tbl_companies
        FOREIGN KEY (company_id)
        REFERENCES app.tbl_companies(id)
        ON DELETE NO ACTION
        ON UPDATE CASCADE
    );

    -- Índice único solo para registros activos (permite soft delete y recreación)
    CREATE UNIQUE NONCLUSTERED INDEX UQ_tbl_subsidiaries_name_active
    ON app.tbl_subsidiaries(name)
    WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_tbl_subsidiaries_public_id
    ON app.tbl_subsidiaries(public_id);

END
GO
