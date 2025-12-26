IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_dynamic_variable_methods' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_dynamic_variable_methods(
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        dynamic_variable_id SMALLINT NOT NULL,
        validation_method_id SMALLINT NOT NULL,
        value NVARCHAR(100) NULL, -- Para métodos que requieren valor
        execution_order INT NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        -- Foreign Keys
        CONSTRAINT FK_dynamic_variable_methods_dynamic_variable
            FOREIGN KEY (dynamic_variable_id) REFERENCES app.tbl_dynamic_variables(id),

        CONSTRAINT FK_dynamic_variable_methods_validation_method
            FOREIGN KEY (validation_method_id) REFERENCES app.tbl_validation_methods(id)
    );

    -- Índices únicos (considerando soft delete)
    CREATE UNIQUE NONCLUSTERED INDEX UQ_dynamic_variable_methods_variable_order_active
    ON app.tbl_dynamic_variable_methods(dynamic_variable_id, execution_order) WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_dynamic_variable_methods_variable_method_active
    ON app.tbl_dynamic_variable_methods(dynamic_variable_id, validation_method_id) WHERE deleted_at IS NULL;

    -- Índices de consulta
    CREATE NONCLUSTERED INDEX IX_dynamic_variable_methods_dynamic_variable_id
    ON app.tbl_dynamic_variable_methods(dynamic_variable_id);

    CREATE NONCLUSTERED INDEX IX_dynamic_variable_methods_validation_method_id
    ON app.tbl_dynamic_variable_methods(validation_method_id);

    CREATE NONCLUSTERED INDEX IX_dynamic_variable_methods_execution_order
    ON app.tbl_dynamic_variable_methods(execution_order);
END
GO