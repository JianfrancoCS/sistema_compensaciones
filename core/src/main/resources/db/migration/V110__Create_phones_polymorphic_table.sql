-- =============================================
-- V110: CREAR TABLA POLIMÓRFICA DE TELÉFONOS
-- =============================================
-- Tabla polimórfica para teléfonos siguiendo el mismo patrón que direcciones
-- Permite asociar teléfonos a cualquier entidad (Company, Person, Subsidiary, etc.)

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tb_phones' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tb_phones (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),

        -- Polimorfismo (mismo patrón que addresses)
        phoneable_type NVARCHAR(100) NOT NULL, -- Tipo de entidad (CompanyEntity, PersonEntity, etc.)
        phoneable_id NVARCHAR(50) NOT NULL,    -- ID de la entidad (puede ser Long, String, etc.)

        -- Datos del teléfono
        phone_number NVARCHAR(20) NOT NULL,    -- Número de teléfono
        phone_type NVARCHAR(50) NULL,          -- Tipo: 'MOBILE', 'LANDLINE', 'FAX', etc.
        extension NVARCHAR(10) NULL,            -- Extensión (opcional)

        -- Metadatos
        is_primary BIT NOT NULL DEFAULT 0,     -- Teléfono principal
        notes NVARCHAR(255) NULL,               -- Notas adicionales

        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL,
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL
    );

    -- Índices
    CREATE UNIQUE NONCLUSTERED INDEX UQ_phones_public_id_active
        ON app.tb_phones(public_id)
        WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_phones_phoneable
        ON app.tb_phones(phoneable_type, phoneable_id, deleted_at);

    CREATE NONCLUSTERED INDEX IX_phones_primary
        ON app.tb_phones(phoneable_type, phoneable_id, is_primary)
        WHERE deleted_at IS NULL AND is_primary = 1;
END
GO

