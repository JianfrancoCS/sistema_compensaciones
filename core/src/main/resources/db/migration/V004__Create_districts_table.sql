IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_districts' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_districts (
        id INT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        province_id INT NOT NULL,
        ubigeo_inei NVARCHAR(6) NOT NULL,
        ubigeo_reniec NVARCHAR(6) NOT NULL,
        name NVARCHAR(100) NOT NULL,

        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        CONSTRAINT FK_districts_provinces
            FOREIGN KEY (province_id)
            REFERENCES app.tbl_provinces(id)
            ON DELETE NO ACTION
            ON UPDATE CASCADE
    );

    -- Índices
    CREATE NONCLUSTERED INDEX IX_districts_name ON app.tbl_districts(name);
    CREATE NONCLUSTERED INDEX IX_districts_provinces ON app.tbl_districts(province_id);
    CREATE NONCLUSTERED INDEX IX_districts_public_id ON app.tbl_districts(public_id);
    CREATE NONCLUSTERED INDEX IX_districts_ubigeo_inei ON app.tbl_districts(ubigeo_inei);
    CREATE NONCLUSTERED INDEX IX_districts_ubigeo_reniec ON app.tbl_districts(ubigeo_reniec);
    CREATE NONCLUSTERED INDEX IX_districts_deleted_at ON app.tbl_districts(deleted_at);
    CREATE NONCLUSTERED INDEX IX_districts_created_at ON app.tbl_districts(created_at);
    CREATE NONCLUSTERED INDEX IX_districts_provinces_name ON app.tbl_districts(province_id, name);
    CREATE NONCLUSTERED INDEX IX_districts_provinces_deleted ON app.tbl_districts(province_id, deleted_at);

    -- Índices únicos solo para registros activos
    CREATE UNIQUE NONCLUSTERED INDEX UQ_districts_ubigeo_inei_active
    ON app.tbl_districts(ubigeo_inei)
    WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_districts_ubigeo_reniec_active
    ON app.tbl_districts(ubigeo_reniec)
    WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_districts_name_provinces_active
    ON app.tbl_districts(name, province_id)
    WHERE deleted_at IS NULL;
END
GO