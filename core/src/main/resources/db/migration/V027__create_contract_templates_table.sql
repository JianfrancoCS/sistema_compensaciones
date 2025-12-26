-- V027: Creación de la tabla de plantillas de contrato en el esquema 'app'
CREATE TABLE app.tbl_contract_templates (
    id SMALLINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL,
    template_content NVARCHAR(MAX) NOT NULL, -- Storing JSON data in NVARCHAR(MAX) for SQL Server compatibility
    contract_type_id SMALLINT NOT NULL,
    state_id SMALLINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2,
    created_by NVARCHAR(255),
    updated_by NVARCHAR(255),
    deleted_at DATETIME2,
    deleted_by NVARCHAR(255),
    FOREIGN KEY (contract_type_id) REFERENCES app.tbl_contract_types(id),
    FOREIGN KEY (state_id) REFERENCES app.tbl_states(id)
);
GO

CREATE UNIQUE INDEX UQ_contract_templates_public_id ON app.tbl_contract_templates(public_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX UQ_contract_templates_name ON app.tbl_contract_templates(name) WHERE deleted_at IS NULL;
GO
