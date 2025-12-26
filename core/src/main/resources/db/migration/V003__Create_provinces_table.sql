IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_provinces' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_provinces (
        id INT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        department_id INT NOT NULL,
        name NVARCHAR(100) NOT NULL,

        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        CONSTRAINT FK_provinces_departments
            FOREIGN KEY (department_id)
            REFERENCES app.tbl_departments(id)
            ON DELETE NO ACTION
            ON UPDATE CASCADE
    );

    -- Índices
    CREATE NONCLUSTERED INDEX IX_provinces_name ON app.tbl_provinces(name);
    CREATE NONCLUSTERED INDEX IX_provinces_departments ON app.tbl_provinces(department_id);
    CREATE NONCLUSTERED INDEX IX_provinces_public_id ON app.tbl_provinces(public_id);
    CREATE NONCLUSTERED INDEX IX_provinces_deleted_at ON app.tbl_provinces(deleted_at);
    CREATE NONCLUSTERED INDEX IX_provinces_created_at ON app.tbl_provinces(created_at);
    CREATE NONCLUSTERED INDEX IX_provinces_dept_name ON app.tbl_provinces(department_id, name);
    CREATE NONCLUSTERED INDEX IX_provinces_dept_deleted ON app.tbl_provinces(department_id, deleted_at);

    -- Índice único compuesto solo para registros activos
    CREATE UNIQUE NONCLUSTERED INDEX UQ_provinces_name_dept_active
    ON app.tbl_provinces(name, department_id)
    WHERE deleted_at IS NULL;
END
GO