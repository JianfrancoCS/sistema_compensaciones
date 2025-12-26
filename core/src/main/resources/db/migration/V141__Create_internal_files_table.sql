-- =============================================
-- V141: CREAR TABLA POLIMÓRFICA PARA ARCHIVOS INTERNOS
-- =============================================
-- Tabla polimórfica para almacenar archivos (PDFs, imágenes, etc.) en SQL Server
-- Permite asociar archivos a diferentes entidades del sistema (contratos, boletas, firmas, etc.)
-- =============================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_internal_files' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_internal_files (
        id BIGINT PRIMARY KEY IDENTITY(1,1),
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        
        -- Relación polimórfica: permite asociar el archivo a cualquier entidad
        fileable_id BIGINT NOT NULL,
        fileable_type NVARCHAR(100) NOT NULL, -- Ej: 'Contract', 'Payslip', 'SubsidiarySigner', etc.
        
        -- Información del archivo
        file_name NVARCHAR(255) NOT NULL,
        file_type NVARCHAR(100) NOT NULL, -- MIME type: 'application/pdf', 'image/png', etc.
        file_size BIGINT NOT NULL, -- Tamaño en bytes
        file_content VARBINARY(MAX) NOT NULL, -- Contenido del archivo
        
        -- Metadatos adicionales
        description NVARCHAR(500) NULL,
        category NVARCHAR(50) NULL, -- Ej: 'CONTRACT', 'PAYSLIP', 'SIGNATURE', etc.
        
        -- Auditoría
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL,
        updated_at DATETIME2 NULL,
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,
        
        -- Índices
        CONSTRAINT UQ_internal_files_public_id UNIQUE (public_id)
    );
    
    -- Índices para búsquedas rápidas
    CREATE INDEX IX_internal_files_fileable ON app.tbl_internal_files(fileable_id, fileable_type) WHERE deleted_at IS NULL;
    CREATE INDEX IX_internal_files_category ON app.tbl_internal_files(category) WHERE deleted_at IS NULL;
    CREATE INDEX IX_internal_files_created_at ON app.tbl_internal_files(created_at) WHERE deleted_at IS NULL;
    
    PRINT N'Tabla app.tbl_internal_files creada exitosamente';
END
ELSE
BEGIN
    PRINT N'Tabla app.tbl_internal_files ya existe';
END
GO

