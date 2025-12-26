-- =============================================
-- Tabla: Rollos QR
-- =============================================
CREATE TABLE app.tbl_qr_rolls (
    id INT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    max_qr_codes_per_day INT NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL
);
GO

-- Índices
CREATE UNIQUE NONCLUSTERED INDEX UQ_qr_rolls_public_id_active
    ON app.tbl_qr_rolls(public_id) WHERE deleted_at IS NULL;
GO