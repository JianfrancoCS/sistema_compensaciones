-- =============================================
-- V111: AGREGAR CAMPOS NECESARIOS PARA BOLETAS DE PAGO
-- =============================================
-- Campos adicionales para empleados que se muestran en las boletas de pago

-- Número de afiliación AFP (ej: 674580YVUA01)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_employees') AND name = 'afp_affiliation_number')
BEGIN
    ALTER TABLE app.tbl_employees
    ADD afp_affiliation_number NVARCHAR(50) NULL;
END
GO

-- Cuenta bancaria (ej: 570-72718712-0-97)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_employees') AND name = 'bank_account_number')
BEGIN
    ALTER TABLE app.tbl_employees
    ADD bank_account_number NVARCHAR(50) NULL;
END
GO

-- Nombre del banco (ej: BANCO DE CREDITO DEL PERU)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_employees') AND name = 'bank_name')
BEGIN
    ALTER TABLE app.tbl_employees
    ADD bank_name NVARCHAR(100) NULL;
END
GO

-- Fecha de ingreso del empleado
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_employees') AND name = 'hire_date')
BEGIN
    ALTER TABLE app.tbl_employees
    ADD hire_date DATE NULL;
END
GO

-- Sueldo básico diario (para mostrar en boleta)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_employees') AND name = 'daily_basic_salary')
BEGIN
    ALTER TABLE app.tbl_employees
    ADD daily_basic_salary DECIMAL(10,2) NULL;
END
GO

