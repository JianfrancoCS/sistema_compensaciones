-- =============================================
-- V108.5: CREAR TABLA POLIMÓRFICA DE DIRECCIONES
-- =============================================
-- Esta tabla permite asociar múltiples direcciones a diferentes
-- entidades (empresas, personas, sucursales, etc.) de forma polimórfica.
-- IMPORTANTE: Esta migración debe ejecutarse ANTES de V109

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tb_addresses' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tb_addresses (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),

        -- Polimorfismo
        addressable_type NVARCHAR(100) NOT NULL, -- Nombre de la entidad (ej. 'CompanyEntity', 'PersonEntity')
        addressable_id NVARCHAR(50) NOT NULL,    -- ID de la entidad (ej. ID de la empresa, DNI de la persona)

        -- Coordenadas geográficas
        longitude NVARCHAR(50) NULL,
        latitude NVARCHAR(50) NULL,

        -- Dirección textual (se agregará en V109, pero se crea aquí para evitar errores)
        address_text NVARCHAR(500) NULL,

        -- Metadatos
        is_primary BIT NOT NULL DEFAULT 0,

        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL,
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        CONSTRAINT UQ_tb_addresses_public_id UNIQUE (public_id)
    );

    CREATE NONCLUSTERED INDEX IX_tb_addresses_addressable
        ON app.tb_addresses(addressable_type, addressable_id);

    CREATE NONCLUSTERED INDEX IX_tb_addresses_primary
        ON app.tb_addresses(addressable_type, addressable_id, is_primary)
        WHERE deleted_at IS NULL AND is_primary = 1;
END
GO

