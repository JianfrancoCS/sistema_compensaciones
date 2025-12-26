-- =============================================
-- Tabla: Motivos de Ajuste de Tareo
-- =============================================
CREATE TABLE app.tbl_tareo_motives (
    id SMALLINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500) NULL,

    -- Tipo de motivo
    is_paid BIT NOT NULL DEFAULT 1, -- 1 = remunerado (autocompleta 8 horas), 0 = no remunerado (calcula horas reales)

    -- Auditoría
    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL
);
GO

-- Índices
CREATE UNIQUE NONCLUSTERED INDEX UQ_tareo_motives_public_id_active
    ON app.tbl_tareo_motives(public_id) WHERE deleted_at IS NULL;

CREATE UNIQUE NONCLUSTERED INDEX UQ_tareo_motives_name_active
    ON app.tbl_tareo_motives(name) WHERE deleted_at IS NULL;
GO

-- =============================================
-- Inserciones
-- =============================================
INSERT INTO app.tbl_tareo_motives (name, description, is_paid)
VALUES
    ('Capacitación inicial', 'En capacitación, se completa jornada', 1),
    ('Salida por trámite personal', 'Salida no remunerada', 0),
    ('Salida por enfermedad', 'Salida por motivos de salud no remunerada', 0),
    ('Permiso sin goce', 'Permiso sin remuneración', 0),
    ('Permiso con goce', 'Permiso remunerado, completa jornada', 1),
    ('Tardanza justificada', 'Llegada tarde justificada, se completa jornada', 1),
    ('Tardanza injustificada', 'Llegada tarde sin justificación', 0);
GO
