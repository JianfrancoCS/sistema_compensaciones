-- =============================================
-- Tabla: Labores
-- =============================================
CREATE TABLE app.tbl_labors (
    id SMALLINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500) NULL,

    -- Tareo mínimo requerido (puede ser null si no aplica)
    min_task_requirement DECIMAL(10, 2) NULL,
    labor_unit_id SMALLINT NOT NULL,

    -- Precios
    is_piecework BIT NOT NULL DEFAULT 0,
    base_price DECIMAL(10, 2) NULL,

    -- Auditoría
    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL,

    CONSTRAINT FK_labors_labor_unit
        FOREIGN KEY (labor_unit_id) REFERENCES app.tbl_labor_units(id)
        ON DELETE NO ACTION ON UPDATE CASCADE
);
GO

-- Índices
CREATE UNIQUE NONCLUSTERED INDEX UQ_labors_public_id_active
    ON app.tbl_labors(public_id) WHERE deleted_at IS NULL;

CREATE UNIQUE NONCLUSTERED INDEX UQ_labors_name_active
    ON app.tbl_labors(name) WHERE deleted_at IS NULL;

CREATE NONCLUSTERED INDEX IX_labors_labor_unit_active
    ON app.tbl_labors(labor_unit_id, deleted_at);
GO

-- =============================================
-- Inserciones: Labores
-- =============================================
DECLARE @jabas_unit_id SMALLINT, @jarras_unit_id SMALLINT, @jornales_unit_id SMALLINT, @hectareas_unit_id SMALLINT, @plantas_unit_id SMALLINT;

SELECT @jabas_unit_id = id FROM app.tbl_labor_units WHERE name = 'Jabas';
SELECT @jarras_unit_id = id FROM app.tbl_labor_units WHERE name = 'Jarras';
SELECT @jornales_unit_id = id FROM app.tbl_labor_units WHERE name = 'Jornales';
SELECT @hectareas_unit_id = id FROM app.tbl_labor_units WHERE name = 'Hectáreas';
SELECT @plantas_unit_id = id FROM app.tbl_labor_units WHERE name = 'Plantas';

-- Labores de cosecha (a destajo)
IF @jabas_unit_id IS NOT NULL
BEGIN
    INSERT INTO app.tbl_labors (name, description, min_task_requirement, labor_unit_id, is_piecework, base_price)
    VALUES
        ('Cosecha de Uva', 'Recolección de uva en campo', 25.00, @jabas_unit_id, 1, 1.50),
        ('Cosecha de Palta', 'Recolección de palta en campo', 30.00, @jabas_unit_id, 1, 1.20),
        ('Cosecha de Mandarina', 'Recolección de mandarina en campo', 40.00, @jabas_unit_id, 1, 1.00);
END

IF @jarras_unit_id IS NOT NULL
BEGIN
    INSERT INTO app.tbl_labors (name, description, min_task_requirement, labor_unit_id, is_piecework, base_price)
    VALUES
        ('Cosecha de Arandano', 'Recolección de arándano en campo', 10.00, @jarras_unit_id, 1, 2.50);
END

-- Labores generales (por jornal/hora)
IF @jornales_unit_id IS NOT NULL
BEGIN
    INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
    VALUES
        ('Limpieza de Terreno', 'Limpieza y desbroce de terreno', @jornales_unit_id, 0);
END

IF @hectareas_unit_id IS NOT NULL
BEGIN
    INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
    VALUES
        ('Riego', 'Aplicación de riego en campo', @hectareas_unit_id, 0),
        ('Fertilización', 'Aplicación de fertilizantes', @hectareas_unit_id, 0);
END

IF @plantas_unit_id IS NOT NULL
BEGIN
    INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
    VALUES
        ('Poda', 'Poda de formación y mantenimiento de plantas', @plantas_unit_id, 0);
END
GO
