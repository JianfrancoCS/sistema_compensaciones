-- =============================================
-- Insert Payroll Concepts
-- IMPORTANTE: Los códigos deben coincidir EXACTAMENTE con el enum ConceptCode.java
-- IMPORTANTE: calculation_priority determina el orden de ejecución en el batch job
-- IMPORTANTE: Los nombres de visualización para boletas están en ConceptCode.getPayslipDisplayName()
--             Este enum es el ÚNICO punto de verdad para códigos y nombres de conceptos
-- =============================================

-- Obtener IDs de categorías
DECLARE @incomeId SMALLINT;
DECLARE @retirementId SMALLINT;
DECLARE @employerContributionId SMALLINT;

SELECT @incomeId = id FROM app.tbl_concept_categories WHERE code = 'INCOME';
SELECT @retirementId = id FROM app.tbl_concept_categories WHERE code = 'RETIREMENT';
SELECT @employerContributionId = id FROM app.tbl_concept_categories WHERE code = 'EMPLOYER_CONTRIBUTION';

-- =============================================
-- INGRESOS (Se ejecutan primero para acumular en totalIncome)
-- =============================================

-- 1. BASIC_SALARY - Sueldo básico (priority 10)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'BASIC_SALARY')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'BASIC_SALARY',
        'Sueldo Básico',
        'Remuneración básica mensual del empleado',
        @incomeId,
        NULL, -- Se toma de employee.custom_salary o position.salary
        10,
        GETUTCDATE(),
        'SYSTEM'
    );
END

-- 2. OVERTIME - Horas extras (priority 20)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'OVERTIME')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'OVERTIME',
        'Horas Extras',
        'Pago por horas extras: 25% primeras 2 horas, 100% adicionales y domingos/feriados',
        @incomeId,
        NULL, -- Se calcula dinámicamente
        20,
        GETUTCDATE(),
        'SYSTEM'
    );
END

-- 3. PRODUCTIVITY_BONUS - Bono de productividad (priority 30)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'PRODUCTIVITY_BONUS')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'PRODUCTIVITY_BONUS',
        'Bono de Productividad',
        'Bono por día otorgado si el empleado supera el mínimo de productividad (>=100%) en ese día. Solo aplica para labores de destajo. El valor configurado es el monto por día (ej: 3.00 soles por día).',
        @incomeId,
        3.00, -- Monto por día configurable (ej: 3 soles por día)
        30,
        GETUTCDATE(),
        'SYSTEM'
    );
END
ELSE
BEGIN
    -- Actualizar descripción si el concepto ya existe
    UPDATE app.tbl_concepts
    SET description = 'Bono por día otorgado si el empleado supera el mínimo de productividad (>=100%) en ese día. Solo aplica para labores de destajo. El valor configurado es el monto por día (ej: 3.00 soles por día).',
        value = 3.00, -- Actualizar valor por defecto a monto por día
        updated_at = GETUTCDATE(),
        updated_by = 'SYSTEM'
    WHERE code = 'PRODUCTIVITY_BONUS';
END

-- 3.1. PIECEWORK_EXCESS - Pago por excedente de destajo (priority 31)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'PIECEWORK_EXCESS')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'PIECEWORK_EXCESS',
        'Destajo Excedente',
        'Pago por unidades excedentes en labores de destajo. Se calcula día por día: (productividad_del_día - min_task_requirement) * base_price. Solo aplica si la productividad supera el mínimo requerido. El valor se calcula dinámicamente según la labor.',
        @incomeId,
        NULL, -- Se calcula dinámicamente según la labor (excedente * base_price)
        31,
        GETUTCDATE(),
        'SYSTEM'
    );
END
ELSE
BEGIN
    -- Actualizar descripción si el concepto ya existe
    UPDATE app.tbl_concepts
    SET description = 'Pago por unidades excedentes en labores de destajo. Se calcula día por día: (productividad_del_día - min_task_requirement) * base_price. Solo aplica si la productividad supera el mínimo requerido. El valor se calcula dinámicamente según la labor.',
        value = NULL, -- Se calcula dinámicamente
        updated_at = GETUTCDATE(),
        updated_by = 'SYSTEM'
    WHERE code = 'PIECEWORK_EXCESS';
END

-- 4. ATTENDANCE_BONUS - Bono de asistencia (priority 40)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'ATTENDANCE_BONUS')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'ATTENDANCE_BONUS',
        'Bono de Asistencia',
        'Bono otorgado si el empleado asistió TODOS los días laborables del período',
        @incomeId,
        150.00, -- Monto base configurable
        40,
        GETUTCDATE(),
        'SYSTEM'
    );
END

-- 5. FAMILY_ALLOWANCE - Asignación familiar (priority 50)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'FAMILY_ALLOWANCE')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'FAMILY_ALLOWANCE',
        'Asignación Familiar',
        'Asignación familiar por hijos menores de edad (10% RMV según D.S. 035-90-TR)',
        @incomeId,
        102.50, -- 10% de S/. 1,025 (RMV 2025)
        50,
        GETUTCDATE(),
        'SYSTEM'
    );
END

-- 6. DOMINICAL - Pago dominical (priority 35)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'DOMINICAL')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'DOMINICAL',
        'Pago Dominical',
        'Pago dominical otorgado si el empleado cumplió con todos los días laborales de la semana según el calendario de la empresa',
        @incomeId,
        NULL, -- Se calcula como porcentaje del básico diario o monto fijo configurable
        35,
        GETUTCDATE(),
        'SYSTEM'
    );
END

-- =============================================
-- DESCUENTOS - JUBILACIÓN (Se ejecutan DESPUÉS de calcular todos los ingresos)
-- IMPORTANTE: Calculan sobre totalIncome (remuneración computable)
-- =============================================

-- 6. AFP_INTEGRA (priority 100)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'AFP_INTEGRA')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'AFP_INTEGRA',
        'AFP Integra',
        'Aporte obligatorio AFP Integra (10% fondo + comisión ~3%)',
        @retirementId,
        13.00, -- Porcentaje aproximado (10% fondo + 3% comisión)
        100,
        GETUTCDATE(),
        'SYSTEM'
    );
END

-- 7. AFP_PRIMA (priority 100)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'AFP_PRIMA')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'AFP_PRIMA',
        'AFP Prima',
        'Aporte obligatorio AFP Prima (10% fondo + comisión ~3.25%)',
        @retirementId,
        13.25, -- Porcentaje aproximado
        100,
        GETUTCDATE(),
        'SYSTEM'
    );
END

-- 8. AFP_PROFUTURO (priority 100)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'AFP_PROFUTURO')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'AFP_PROFUTURO',
        'AFP Profuturo',
        'Aporte obligatorio AFP Profuturo (10% fondo + comisión ~2.92%)',
        @retirementId,
        12.92, -- Porcentaje aproximado
        100,
        GETUTCDATE(),
        'SYSTEM'
    );
END

-- 9. AFP_HABITAT (priority 100)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'AFP_HABITAT')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'AFP_HABITAT',
        'AFP Habitat',
        'Aporte obligatorio AFP Habitat (10% fondo + comisión ~3.47%)',
        @retirementId,
        13.47, -- Porcentaje aproximado
        100,
        GETUTCDATE(),
        'SYSTEM'
    );
END

-- 10. ONP (priority 100)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'ONP')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'ONP',
        'ONP',
        'Aporte obligatorio a la Oficina de Normalización Previsional (D.L. 19990)',
        @retirementId,
        13.00, -- Porcentaje fijo por ley
        100,
        GETUTCDATE(),
        'SYSTEM'
    );
END

-- =============================================
-- APORTES DEL EMPLEADOR (Se ejecutan al FINAL)
-- =============================================

-- 11. ESSALUD (priority 200)
IF NOT EXISTS (SELECT 1 FROM app.tbl_concepts WHERE code = 'ESSALUD')
BEGIN
    INSERT INTO app.tbl_concepts (code, name, description, category_id, value, calculation_priority, updated_at, updated_by)
    VALUES (
        'ESSALUD',
        'EsSalud',
        'Seguro Social de Salud - Aporte del empleador (Ley 26790)',
        @employerContributionId,
        9.00, -- Porcentaje fijo por ley
        200,
        GETUTCDATE(),
        'SYSTEM'
    );
END

-- =============================================
-- Verificación final
-- =============================================
DECLARE @totalConceptos INT;
SELECT @totalConceptos = COUNT(*) FROM app.tbl_concepts WHERE deleted_at IS NULL;

IF @totalConceptos != 13
BEGIN
    RAISERROR('Error: Se esperaban 13 conceptos pero se encontraron %d', 16, 1, @totalConceptos);
END
ELSE
BEGIN
    PRINT 'Migración exitosa: 12 conceptos de planilla insertados correctamente';
    PRINT 'Los códigos coinciden con ConceptCode.java enum';
END
GO