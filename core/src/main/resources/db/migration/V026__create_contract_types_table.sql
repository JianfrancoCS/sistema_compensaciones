-- V026: Creación de la tabla de tipos de contrato en el esquema 'app'
CREATE TABLE app.tbl_contract_types (
    id SMALLINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    code NVARCHAR(20) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2,
    created_by NVARCHAR(255),
    updated_by NVARCHAR(255),
    deleted_at DATETIME2,
    deleted_by NVARCHAR(255)
);
GO

CREATE UNIQUE INDEX UQ_contract_types_public_id ON app.tbl_contract_types(public_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX UQ_contract_types_code ON app.tbl_contract_types(code) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX UQ_contract_types_name ON app.tbl_contract_types(name) WHERE deleted_at IS NULL;
GO
