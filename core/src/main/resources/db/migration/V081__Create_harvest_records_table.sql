-- =============================================
-- Tabla: Registros de Cosecha
-- =============================================
CREATE TABLE app.tbl_harvest_records (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    temporal_id NVARCHAR(255) NULL,

    qr_code_id BIGINT NOT NULL,
    scanned_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL,

    CONSTRAINT FK_harvest_records_qr_code
        FOREIGN KEY (qr_code_id) REFERENCES app.tbl_qr_codes(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- Índices
CREATE UNIQUE NONCLUSTERED INDEX UQ_harvest_records_public_id_active
    ON app.tbl_harvest_records(public_id) WHERE deleted_at IS NULL;

CREATE UNIQUE NONCLUSTERED INDEX UQ_harvest_records_temporal_id_active
    ON app.tbl_harvest_records(temporal_id)
    WHERE temporal_id IS NOT NULL AND deleted_at IS NULL;

-- Un ticket QR solo se puede usar UNA VEZ
CREATE UNIQUE NONCLUSTERED INDEX UQ_harvest_records_qr_code_active
    ON app.tbl_harvest_records(qr_code_id) WHERE deleted_at IS NULL;

CREATE NONCLUSTERED INDEX IX_harvest_records_scanned_at
    ON app.tbl_harvest_records(scanned_at) WHERE deleted_at IS NULL;
GO