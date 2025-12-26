-- V052: Create marking reasons catalog table
CREATE TABLE app.tbl_marking_reasons (
    id SMALLINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    code NVARCHAR(20) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2,
    created_by NVARCHAR(255),
    updated_by NVARCHAR(255),
    deleted_at DATETIME2,
    deleted_by NVARCHAR(255)
);
GO

CREATE UNIQUE INDEX UQ_marking_reasons_public_id ON app.tbl_marking_reasons(public_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX UQ_marking_reasons_code ON app.tbl_marking_reasons(code) WHERE deleted_at IS NULL;
GO

INSERT INTO app.tbl_marking_reasons (code, name, created_by)
VALUES
    ('WORK', 'Laborar', 'SYSTEM'),
    ('CONTRACT_SIGNING', 'Firmar Contrato', 'SYSTEM'),
    ('VISIT', 'Visita General', 'SYSTEM'),
    ('SUPERVISION', 'Supervisión/Inspección', 'SYSTEM');
GO