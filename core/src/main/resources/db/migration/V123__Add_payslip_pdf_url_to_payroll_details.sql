-- =============================================
-- V123: AGREGAR CAMPO PAYSLIP_PDF_URL A PAYROLL_DETAILS
-- =============================================
-- Agrega campo para almacenar la URL del PDF de la boleta de pago generada
-- El PDF se almacena en Cloudinary y solo se guarda la URL en la BD

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE Name = N'payslip_pdf_url' AND Object_ID = Object_ID(N'app.tbl_payroll_details'))
BEGIN
    ALTER TABLE app.tbl_payroll_details
    ADD payslip_pdf_url NVARCHAR(500) NULL; -- URL del PDF en Cloudinary

    PRINT 'Columna payslip_pdf_url agregada a app.tbl_payroll_details';
END
GO

-- Crear índice para búsquedas rápidas por URL
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_payroll_details_payslip_pdf_url' AND object_id = OBJECT_ID('app.tbl_payroll_details'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_payroll_details_payslip_pdf_url 
    ON app.tbl_payroll_details(payslip_pdf_url) 
    WHERE payslip_pdf_url IS NOT NULL AND deleted_at IS NULL;
    
    PRINT 'Índice IX_payroll_details_payslip_pdf_url creado en app.tbl_payroll_details';
END
GO

