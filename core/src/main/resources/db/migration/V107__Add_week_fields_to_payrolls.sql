-- =============================================
-- V107: AGREGAR CAMPOS DE SEMANAS A PAYROLLS
-- =============================================
-- Agrega campos para almacenar información de semanas trabajadas
-- según la estructura de boletas de pago peruanas
-- Ejemplo: "DE SEMANA: 37 DESDE 31/07/2023 / A SEMANA: 39 HASTA 13/08/2023"

-- =============================================
-- PAYROLLS: Agregar week_start y week_end
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_payrolls') AND name = 'week_start')
BEGIN
    ALTER TABLE app.tbl_payrolls
    ADD week_start TINYINT NULL; -- Número de semana de inicio (ISO 8601)
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_payrolls') AND name = 'week_end')
BEGIN
    ALTER TABLE app.tbl_payrolls
    ADD week_end TINYINT NULL; -- Número de semana de fin (ISO 8601)
END
GO

-- =============================================
-- COMENTARIOS
-- =============================================
-- Estos campos se calculan automáticamente durante el procesamiento
-- basándose en period_start y period_end usando ISO 8601 week numbering

