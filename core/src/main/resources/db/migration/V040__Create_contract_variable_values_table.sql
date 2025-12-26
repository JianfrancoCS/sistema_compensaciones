CREATE TABLE [app].[tbl_contract_variable_values] (
    [contract_id] BIGINT NOT NULL,
    [variable_id] SMALLINT NOT NULL,
    [value] NVARCHAR(500) NOT NULL,
    [created_at] DATETIME2(7) NOT NULL DEFAULT SYSDATETIME(),
    [updated_at] DATETIME2(7) NULL,
    [created_by] NVARCHAR(255) NULL,
    [updated_by] NVARCHAR(255) NULL,
    [deleted_at] DATETIME2(7) NULL,
    [deleted_by] NVARCHAR(255) NULL,

    -- Clave primaria compuesta
    CONSTRAINT PK_contract_variable_values PRIMARY KEY ([contract_id], [variable_id]),

    -- Foreign keys
    CONSTRAINT FK_contract_variable_values_contract
        FOREIGN KEY ([contract_id]) REFERENCES [app].[tbl_contracts] ([id]),
    CONSTRAINT FK_contract_variable_values_variable
        FOREIGN KEY ([variable_id]) REFERENCES [app].[tbl_variables] ([id])
);

-- Índice de unicidad parcial para la clave primaria compuesta (solo registros no eliminados)
CREATE UNIQUE INDEX UQ_contract_variable_values_composite ON app.tbl_contract_variable_values(contract_id, variable_id) WHERE deleted_at IS NULL;
