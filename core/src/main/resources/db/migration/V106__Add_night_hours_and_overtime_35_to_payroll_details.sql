-- =============================================
-- V106: AGREGAR CAMPOS DE HORAS NOCTURNAS Y HORAS EXTRAS 35% A PAYROLL DETAILS
-- =============================================
-- Agrega campos para soportar horas nocturnas y horas extras al 35%
-- según la estructura de boletas de pago peruanas

-- =============================================
-- PAYROLL DETAILS: Agregar night_hours y overtime_hours_35
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_payroll_details') AND name = 'night_hours')
BEGIN
    ALTER TABLE app.tbl_payroll_details
    ADD night_hours DECIMAL(8,2) NULL; -- Horas trabajadas en turno nocturno (22:00-06:00)
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_payroll_details') AND name = 'overtime_hours_35')
BEGIN
    ALTER TABLE app.tbl_payroll_details
    ADD overtime_hours_35 DECIMAL(8,2) NULL; -- Horas extras con recargo del 35%
END
GO

-- =============================================
-- ÍNDICES (si es necesario para consultas frecuentes)
-- =============================================
-- Los índices existentes ya cubren las consultas principales
-- No se requieren índices adicionales para estos campos

