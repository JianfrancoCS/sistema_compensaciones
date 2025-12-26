-- V062: Crear tabla de tipos de documento
CREATE TABLE app.tbl_document_types (
    id SMALLINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    code NVARCHAR(10) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    length INT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
    deleted_at DATETIME2 NULL,
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by NVARCHAR(100) NULL,
    deleted_by NVARCHAR(100) NULL
);
GO

-- Crear índices únicos con filtro para soft delete
CREATE UNIQUE INDEX UQ_document_types_public_id ON app.tbl_document_types(public_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX UQ_document_types_code ON app.tbl_document_types(code) WHERE deleted_at IS NULL;
GO

-- Insertar tipos de documento por defecto
INSERT INTO app.tbl_document_types (code, name, length, created_by) VALUES
('DNI', 'Documento Nacional de Identidad', 8, 'SYSTEM'),
('CE', 'Carnet de Extranjería', 9, 'SYSTEM');
GO