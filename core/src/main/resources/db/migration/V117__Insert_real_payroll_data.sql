-- =============================================
-- V117: INSERTAR DATOS BASE PARA GENERACIÓN DE PLANILLAS
-- =============================================
-- NOTA: Esta migración NO crea planillas ni boletas directamente.
-- Las planillas y boletas se generan automáticamente por el sistema
-- basándose en asistencias, tareos y configuraciones.
-- 
-- Esta migración solo crea:
-- - Períodos de planilla (para que el sistema sepa qué períodos procesar)
-- - Configuración de planilla (conceptos y valores base)

-- =============================================
-- 1. CREAR PERÍODOS DE PLANILLA SEGÚN CONFIGURACIÓN DE EMPRESA
-- =============================================
-- Los períodos se calculan dinámicamente basándose en:
-- - payroll_payment_interval: Intervalo de pago en días (7=semanal, 15=quincenal, 30=mensual)
-- - payroll_declaration_day: Día del mes para declaración
-- - payroll_anticipation_days: Días de anticipación antes del cierre

DECLARE @company_id_periods BIGINT = (SELECT id FROM app.tbl_companies WHERE ruc = '20520866630' AND deleted_at IS NULL);
DECLARE @payment_interval INT;
DECLARE @declaration_day TINYINT;
DECLARE @anticipation_days TINYINT;

IF @company_id_periods IS NOT NULL
BEGIN
    SELECT 
        @payment_interval = payroll_payment_interval,
        @declaration_day = payroll_declaration_day,
        @anticipation_days = payroll_anticipation_days
    FROM app.tbl_companies 
    WHERE id = @company_id_periods;
    
    -- Si no hay configuración, usar valores por defecto (mensual)
    IF @payment_interval IS NULL SET @payment_interval = 30;
    IF @declaration_day IS NULL SET @declaration_day = 28;
    IF @anticipation_days IS NULL SET @anticipation_days = 3;
    
    -- Crear períodos desde Agosto 2025 hasta ahora (Noviembre 2025)
    -- Basándose en el intervalo de pago configurado
    -- La lógica sigue exactamente el mismo algoritmo del backend PayrollPeriodService
    
    DECLARE @period_start DATE = '2025-08-01'; -- Inicio: 1 de Agosto 2025
    DECLARE @period_end DATE;
    DECLARE @declaration_date DATE;
    DECLARE @period_year SMALLINT;
    DECLARE @period_month TINYINT;
    DECLARE @potential_declaration_date DATE;
    DECLARE @fecha_hoy_periods DATE = CAST(GETDATE() AS DATE);
    DECLARE @fecha_fin_periods DATE = @fecha_hoy_periods; -- Hasta hoy
    
    DECLARE @period_exists BIT; -- Variable para verificar si el período ya existe
    
    -- Continuar creando períodos hasta cubrir desde Agosto hasta ahora
    -- (Para mensual: ~4 períodos, para quincenal: ~8 períodos, para semanal: ~17 períodos)
    
    WHILE @period_start <= @fecha_fin_periods
    BEGIN
        -- Calcular fin del período: periodStart + (paymentInterval - 1) días
        SET @period_end = DATEADD(DAY, @payment_interval - 1, @period_start);
        
        -- Asegurar que no exceda la fecha de hoy
        IF @period_end > @fecha_fin_periods
            SET @period_end = @fecha_fin_periods;
        
        -- El período pertenece al año y mes en que termina
        SET @period_year = YEAR(@period_end);
        SET @period_month = MONTH(@period_end);
        
        -- NOTA: period_number se agregará en V130 y se actualizará en V131
        -- Por ahora, solo creamos períodos sin period_number (usará el valor por defecto 1)
        
        -- Calcular fecha de declaración según lógica del backend:
        -- 1. Intentar usar el día de declaración del mes en que termina el período
        SET @potential_declaration_date = DATEFROMPARTS(@period_year, @period_month, @declaration_day);
        
        -- 2. Si ese día es antes del fin del período, usar el mes siguiente
        IF @potential_declaration_date < @period_end
        BEGIN
            -- Mover al mes siguiente
            SET @declaration_date = DATEADD(MONTH, 1, @potential_declaration_date);
        END
        ELSE
        BEGIN
            -- Usar el día de declaración del mismo mes
            SET @declaration_date = @potential_declaration_date;
        END
        
        -- Insertar período solo si no existe (verificar por year/month)
        -- NOTA: Después de V130, se actualizará period_number en V131
        SET @period_exists = 0;
        
        IF EXISTS (SELECT 1 FROM app.tbl_payroll_periods 
                   WHERE year = @period_year 
                   AND month = @period_month 
                   AND deleted_at IS NULL)
            SET @period_exists = 1;
        
        -- Insertar solo si no existe
        IF @period_exists = 0
        BEGIN
            INSERT INTO app.tbl_payroll_periods (year, month, period_start, period_end, declaration_date)
            VALUES (@period_year, @period_month, @period_start, @period_end, @declaration_date);
        END
        
        -- Preparar siguiente período: el día siguiente al fin del período actual
        SET @period_start = DATEADD(DAY, 1, @period_end);
        
        -- Si ya llegamos o pasamos la fecha de hoy, salir
        IF @period_start > @fecha_fin_periods
            BREAK;
    END
END
GO

-- =============================================
-- 2. CREAR CONFIGURACIÓN DE PLANILLA
-- =============================================

DECLARE @payroll_config_id BIGINT;
DECLARE @basic_salary_id SMALLINT;
DECLARE @overtime_id SMALLINT;
DECLARE @productivity_bonus_id SMALLINT;
DECLARE @attendance_bonus_id SMALLINT;
DECLARE @family_allowance_id SMALLINT;
DECLARE @afp_integra_id SMALLINT;
DECLARE @essalud_id SMALLINT;

IF NOT EXISTS (SELECT 1 FROM app.tbl_payroll_configuration WHERE code = 'CONF_PLANILLA_20241118')
BEGIN
    INSERT INTO app.tbl_payroll_configuration (code, description)
    VALUES ('CONF_PLANILLA_20241118', 'Configuración de planilla estándar - Noviembre 2025');
    
    SET @payroll_config_id = SCOPE_IDENTITY();
    
    -- Asignar conceptos a la configuración
    SET @basic_salary_id = (SELECT id FROM app.tbl_concepts WHERE code = 'BASIC_SALARY');
    SET @overtime_id = (SELECT id FROM app.tbl_concepts WHERE code = 'OVERTIME');
    SET @productivity_bonus_id = (SELECT id FROM app.tbl_concepts WHERE code = 'PRODUCTIVITY_BONUS');
    SET @attendance_bonus_id = (SELECT id FROM app.tbl_concepts WHERE code = 'ATTENDANCE_BONUS');
    SET @family_allowance_id = (SELECT id FROM app.tbl_concepts WHERE code = 'FAMILY_ALLOWANCE');
    SET @afp_integra_id = (SELECT id FROM app.tbl_concepts WHERE code = 'AFP_INTEGRA');
    SET @essalud_id = (SELECT id FROM app.tbl_concepts WHERE code = 'ESSALUD');
    
    -- Insertar conceptos en la configuración
    IF @basic_salary_id IS NOT NULL
        INSERT INTO app.tbl_payroll_configuration_concepts (payroll_configuration_id, concept_id) VALUES (@payroll_config_id, @basic_salary_id);
    
    IF @overtime_id IS NOT NULL
        INSERT INTO app.tbl_payroll_configuration_concepts (payroll_configuration_id, concept_id) VALUES (@payroll_config_id, @overtime_id);
    
    IF @productivity_bonus_id IS NOT NULL
        INSERT INTO app.tbl_payroll_configuration_concepts (payroll_configuration_id, concept_id, value) VALUES (@payroll_config_id, @productivity_bonus_id, 100.00);
    
    IF @attendance_bonus_id IS NOT NULL
        INSERT INTO app.tbl_payroll_configuration_concepts (payroll_configuration_id, concept_id, value) VALUES (@payroll_config_id, @attendance_bonus_id, 150.00);
    
    IF @family_allowance_id IS NOT NULL
        INSERT INTO app.tbl_payroll_configuration_concepts (payroll_configuration_id, concept_id, value) VALUES (@payroll_config_id, @family_allowance_id, 102.50);
    
    IF @afp_integra_id IS NOT NULL
        INSERT INTO app.tbl_payroll_configuration_concepts (payroll_configuration_id, concept_id, value) VALUES (@payroll_config_id, @afp_integra_id, 13.00);
    
    IF @essalud_id IS NOT NULL
        INSERT INTO app.tbl_payroll_configuration_concepts (payroll_configuration_id, concept_id, value) VALUES (@payroll_config_id, @essalud_id, 9.00);
END
GO

PRINT 'Migración V117 completada: Períodos y configuración de planilla creados. Las planillas se generarán automáticamente por el sistema.';
GO
