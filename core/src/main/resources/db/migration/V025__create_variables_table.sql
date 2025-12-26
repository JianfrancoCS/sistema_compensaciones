CREATE TABLE app.tbl_variables (
    id SMALLINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    default_value NVARCHAR(500) NULL, -- Valor por defecto de la variable
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2,
    created_by NVARCHAR(255),
    updated_by NVARCHAR(255),
    deleted_at DATETIME2,
    deleted_by NVARCHAR(255)
);
GO

CREATE UNIQUE INDEX UQ_variables_public_id ON app.tbl_variables(public_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX UQ_variables_code ON app.tbl_variables(code) WHERE deleted_at IS NULL;
GO