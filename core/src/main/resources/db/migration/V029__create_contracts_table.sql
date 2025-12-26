-- V029: Creación de la tabla de contratos en el esquema 'app'
CREATE TABLE app.tbl_contracts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    contract_number NVARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    content NVARCHAR(MAX) NOT NULL, -- Storing JSON data in NVARCHAR(MAX) for SQL Server compatibility
    variables NVARCHAR(MAX) NOT NULL, -- Storing JSON data in NVARCHAR(MAX) for SQL Server compatibility
    person_document_number NVARCHAR(15) NOT NULL, -- Corrected column name and length to match tbl_persons(document_number)
    contract_type_id SMALLINT NOT NULL,
    state_id SMALLINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2,
    created_by NVARCHAR(255),
    updated_by NVARCHAR(255),
    deleted_at DATETIME2,
    deleted_by NVARCHAR(255),
    FOREIGN KEY (contract_type_id) REFERENCES app.tbl_contract_types(id),
    FOREIGN KEY (person_document_number) REFERENCES app.tbl_persons(document_number), -- Corrected foreign key reference
    FOREIGN KEY (state_id) REFERENCES app.tbl_states(id)
);
GO

CREATE UNIQUE INDEX UQ_contracts_public_id ON app.tbl_contracts(public_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX UQ_contracts_contract_number ON app.tbl_contracts(contract_number) WHERE deleted_at IS NULL;
GO
