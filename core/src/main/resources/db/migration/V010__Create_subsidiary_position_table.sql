IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_subsidiaries_positions' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_subsidiaries_positions(
        id INT IDENTITY(1,1) PRIMARY KEY,
        subsidiary_id SMALLINT NOT NULL,
        position_id SMALLINT NOT NULL,
        is_active BIT NOT NULL DEFAULT 1,
        description NVARCHAR(255) NULL,

        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        CONSTRAINT FK_subsidiaries_positions_subsidiaries
        FOREIGN KEY (subsidiary_id)
        REFERENCES app.tbl_subsidiaries(id)
        ON DELETE NO ACTION
        ON UPDATE CASCADE,

        CONSTRAINT FK_subsidiaries_positions_positions
        FOREIGN KEY (position_id)
        REFERENCES app.tbl_positions(id)
        ON DELETE NO ACTION
        ON UPDATE CASCADE
    );

    -- Índices
    CREATE NONCLUSTERED INDEX IX_subsidiaries_positions_subsidiaries ON app.tbl_subsidiaries_positions(subsidiary_id);
    CREATE NONCLUSTERED INDEX IX_subsidiaries_positions_positions ON app.tbl_subsidiaries_positions(position_id);
    CREATE NONCLUSTERED INDEX IX_subsidiaries_positions_deleted ON app.tbl_subsidiaries_positions(deleted_at);

    -- Índice único para evitar duplicados activos
    CREATE UNIQUE NONCLUSTERED INDEX UQ_subsidiaries_positions_active
    ON app.tbl_subsidiaries_positions(subsidiary_id, position_id)
    WHERE deleted_at IS NULL;
END
GO