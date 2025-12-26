-- =============================================
-- Add FK constraint from tbl_payrolls to tbl_payroll_configuration
-- This must be done after V087 creates tbl_payroll_configuration
-- =============================================

ALTER TABLE app.tbl_payrolls
ADD CONSTRAINT FK_payrolls_payroll_configuration
    FOREIGN KEY (payroll_configuration_id)
    REFERENCES app.tbl_payroll_configuration(id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
GO