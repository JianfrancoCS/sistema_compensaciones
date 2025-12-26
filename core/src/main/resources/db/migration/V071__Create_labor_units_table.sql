-- =============================================
-- Tabla: Unidades de Labor
-- =============================================
CREATE TABLE app.tbl_labor_units (
    id SMALLINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    name NVARCHAR(50) NOT NULL,
    abbreviation NVARCHAR(10) NOT NULL,
    description NVARCHAR(200) NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL
);
GO

-- Índices
CREATE UNIQUE NONCLUSTERED INDEX UQ_labor_units_public_id_active
    ON app.tbl_labor_units(public_id) WHERE deleted_at IS NULL;

CREATE UNIQUE NONCLUSTERED INDEX UQ_labor_units_name_active
    ON app.tbl_labor_units(name) WHERE deleted_at IS NULL;

CREATE UNIQUE NONCLUSTERED INDEX UQ_labor_units_abbreviation_active
    ON app.tbl_labor_units(abbreviation) WHERE deleted_at IS NULL;
GO

-- =============================================
-- Inserciones: Unidades de Labor
-- =============================================
INSERT INTO app.tbl_labor_units (name, abbreviation, description)
VALUES
    ('Kilogramos', 'kg', 'Peso en kilogramos'),
    ('Toneladas', 't', 'Peso en toneladas'),
    ('Unidades', 'und', 'Cantidad en unidades'),
    ('Hectáreas', 'ha', 'Área en hectáreas'),
    ('Metros', 'm', 'Distancia en metros'),
    ('Jornales', 'jor', 'Días de trabajo'),
    ('Horas', 'hrs', 'Horas de trabajo'),
    ('Sacos', 'scs', 'Cantidad en sacos'),
    ('Cajas', 'cjs', 'Cantidad en cajas'),
    ('Plantas', 'plt', 'Cantidad de plantas'),
    ('Jarras', 'jrr', 'Cantidad en jarras'),
    ('Jabas', 'jbs', 'Cantidad en jabas');
GO
