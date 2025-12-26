-- =============================================
-- Alter: Add period_id to tbl_payrolls
-- Descripción: Relacionar planillas con períodos
-- =============================================

-- Agregar columna period_id (INT para coincidir con tbl_payroll_periods.id)
ALTER TABLE app.tbl_payrolls
    ADD period_id INT NULL;
GO

-- Agregar FK a tbl_payroll_periods
ALTER TABLE app.tbl_payrolls
    ADD CONSTRAINT FK_payrolls_period
        FOREIGN KEY (period_id)
        REFERENCES app.tbl_payroll_periods(id)
        ON DELETE NO ACTION
        ON UPDATE CASCADE;
GO

-- Índice para búsquedas por período
CREATE NONCLUSTERED INDEX IX_payrolls_period
    ON app.tbl_payrolls(period_id)
    WHERE period_id IS NOT NULL;
GO