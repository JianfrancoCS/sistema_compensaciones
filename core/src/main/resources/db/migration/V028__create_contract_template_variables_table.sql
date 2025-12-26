-- V028: Creación de la tabla de variables por plantilla de contrato en el esquema 'app'
CREATE TABLE app.tbl_contract_template_variables (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    contract_template_id SMALLINT NOT NULL,
    variable_id SMALLINT NOT NULL,
    is_required BIT DEFAULT 1,
    display_order INT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2,
    created_by NVARCHAR(255),
    updated_by NVARCHAR(255),
    deleted_at DATETIME2,
    deleted_by NVARCHAR(255),
    FOREIGN KEY (contract_template_id) REFERENCES app.tbl_contract_templates(id),
    FOREIGN KEY (variable_id) REFERENCES app.tbl_variables(id)
);
GO

CREATE UNIQUE INDEX UQ_contract_template_variables_template_variable ON app.tbl_contract_template_variables(contract_template_id, variable_id);
GO