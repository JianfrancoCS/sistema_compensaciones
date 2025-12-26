IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_states' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_states(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        domain_id SMALLINT NOT NULL,
        name NVARCHAR(50) NOT NULL,
        code NVARCHAR(50) NOT NULL,
        is_default BIT NOT NULL DEFAULT 0,

        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        CONSTRAINT FK_tbl_states_tbl_domains
        FOREIGN KEY (domain_id)
        REFERENCES app.tbl_domains(id)
        ON DELETE NO ACTION
        ON UPDATE CASCADE
    );

    -- Índices


    CREATE UNIQUE NONCLUSTERED INDEX UQ_tbl_states_domains_name_active
    ON app.tbl_states(domain_id, name)
    WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_tbl_states_code_active
    ON app.tbl_states(code)
    WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_tbl_states_domains ON app.tbl_states(domain_id);
    CREATE NONCLUSTERED INDEX IX_tbl_states_public_id ON app.tbl_states(public_id);
    CREATE NONCLUSTERED INDEX IX_tbl_states_default ON app.tbl_states(is_default);
END
GO