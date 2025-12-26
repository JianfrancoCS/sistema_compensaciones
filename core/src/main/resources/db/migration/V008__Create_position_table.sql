IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_positions' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_positions(
        id SMALLINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        name NVARCHAR(50) NOT NULL,
        salary DECIMAL(10,2) NULL, -- Salario mensual de la posición
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,
        area_id SMALLINT NULL,
        parent_position_id SMALLINT NULL,

        -- Solo la FK externa (hacia areas)
        CONSTRAINT FK_positions_areas
        FOREIGN KEY (area_id) REFERENCES app.tbl_areas(id)
        ON DELETE NO ACTION ON UPDATE CASCADE
    );

    -- Índices optimizados (corregidos)
    CREATE UNIQUE NONCLUSTERED INDEX UQ_positions_name_active
    ON app.tbl_positions(name) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_positions_public_id_active
    ON app.tbl_positions(public_id, deleted_at);

    CREATE NONCLUSTERED INDEX IX_positions_area_active
    ON app.tbl_positions(area_id, deleted_at);

    CREATE NONCLUSTERED INDEX IX_positions_parent_active
    ON app.tbl_positions(parent_position_id, deleted_at);
END
GO