-- =============================================
-- Tabla: Lotes
-- =============================================
CREATE TABLE app.tbl_lotes (
    id INT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL,
    hectareage DECIMAL(10,2) NULL,

    -- Relación con subsidiary (filiales)
    subsidiary_id SMALLINT NOT NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL,

    CONSTRAINT FK_lotes_subsidiary
        FOREIGN KEY (subsidiary_id) REFERENCES app.tbl_subsidiaries(id)
        ON DELETE NO ACTION ON UPDATE CASCADE
);
GO

-- Índices
CREATE UNIQUE NONCLUSTERED INDEX UQ_lotes_public_id_active
    ON app.tbl_lotes(public_id) WHERE deleted_at IS NULL;

CREATE UNIQUE NONCLUSTERED INDEX UQ_lotes_name_subsidiary_active
    ON app.tbl_lotes(name, subsidiary_id) WHERE deleted_at IS NULL;

CREATE NONCLUSTERED INDEX IX_lotes_subsidiary_active
    ON app.tbl_lotes(subsidiary_id, deleted_at);
GO
