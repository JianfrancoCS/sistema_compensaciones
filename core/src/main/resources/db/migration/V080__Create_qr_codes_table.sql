-- =============================================
-- Tabla: Tickets QR (pertenecen a un rollo)
-- =============================================
CREATE TABLE app.tbl_qr_codes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    qr_roll_id INT NOT NULL,
    is_used BIT NOT NULL DEFAULT 0,
    is_printed BIT NOT NULL DEFAULT 0,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL,

    CONSTRAINT FK_qr_codes_roll
        FOREIGN KEY (qr_roll_id) REFERENCES app.tbl_qr_rolls(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- Índices
CREATE UNIQUE NONCLUSTERED INDEX UQ_qr_codes_public_id_active
    ON app.tbl_qr_codes(public_id) WHERE deleted_at IS NULL;

CREATE NONCLUSTERED INDEX IX_qr_codes_roll
    ON app.tbl_qr_codes(qr_roll_id, deleted_at);
GO