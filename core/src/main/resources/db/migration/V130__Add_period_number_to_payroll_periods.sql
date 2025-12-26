-- =============================================
-- V130: AGREGAR CAMPO period_number A tbl_payroll_periods
-- =============================================
-- Agrega el campo period_number para permitir múltiples períodos en el mismo mes
-- (necesario cuando el intervalo de pago es de 7 días - semanal)
-- Ejemplo: noviembre-1, noviembre-2, noviembre-3, noviembre-4

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_payroll_periods' AND schema_id = SCHEMA_ID('app'))
BEGIN
    -- Agregar columna period_number
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_payroll_periods') AND name = 'period_number')
    BEGIN
        ALTER TABLE app.tbl_payroll_periods
        ADD period_number TINYINT NOT NULL DEFAULT 1;
        PRINT 'Columna period_number agregada a app.tbl_payroll_periods';
    END

    -- Eliminar el índice único antiguo (year, month)
    IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'UQ_payroll_periods_year_month_active' AND object_id = OBJECT_ID('app.tbl_payroll_periods'))
    BEGIN
        DROP INDEX UQ_payroll_periods_year_month_active ON app.tbl_payroll_periods;
        PRINT 'Índice único UQ_payroll_periods_year_month_active eliminado';
    END

    -- Crear nuevo índice único que incluye period_number
    IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'UQ_payroll_periods_year_month_number_active' AND object_id = OBJECT_ID('app.tbl_payroll_periods'))
    BEGIN
        CREATE UNIQUE NONCLUSTERED INDEX UQ_payroll_periods_year_month_number_active
        ON app.tbl_payroll_periods(year, month, period_number)
        WHERE deleted_at IS NULL;
        PRINT 'Nuevo índice único UQ_payroll_periods_year_month_number_active creado';
    END
END
GO

