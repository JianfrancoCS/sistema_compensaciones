-- =============================================
-- Add Foreign Keys to tbl_concepts from tbl_employees
-- IMPORTANTE: Esta migración debe ir DESPUÉS de V083 (Create conceptos table)
-- =============================================

-- Agregar FK para retirement_concept_id
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_employees_retirement_concept')
BEGIN
    ALTER TABLE app.tbl_employees
    ADD CONSTRAINT FK_employees_retirement_concept
        FOREIGN KEY (retirement_concept_id)
        REFERENCES app.tbl_concepts(id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;
END
GO

-- Agregar FK para health_insurance_concept_id
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_employees_health_insurance_concept')
BEGIN
    ALTER TABLE app.tbl_employees
    ADD CONSTRAINT FK_employees_health_insurance_concept
        FOREIGN KEY (health_insurance_concept_id)
        REFERENCES app.tbl_concepts(id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;
END
GO

-- Agregar índices para los campos de conceptos
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_employees_retirement_concept' AND object_id = OBJECT_ID('app.tbl_employees'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_employees_retirement_concept
    ON app.tbl_employees(retirement_concept_id)
    WHERE retirement_concept_id IS NOT NULL;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_employees_health_insurance_concept' AND object_id = OBJECT_ID('app.tbl_employees'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_employees_health_insurance_concept
    ON app.tbl_employees(health_insurance_concept_id)
    WHERE health_insurance_concept_id IS NOT NULL;
END
GO