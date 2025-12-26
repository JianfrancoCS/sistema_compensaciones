-- =============================================
-- V138: AGREGAR CAMPO RMV A TABLA COMPANIES
-- =============================================
-- Agrega el campo rmv (Remuneración Mínima Vital) a la tabla companies
-- Si el campo ya existe, no hace nada

IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('app.tbl_companies') 
    AND name = 'rmv'
)
BEGIN
    ALTER TABLE app.tbl_companies
    ADD rmv DECIMAL(10,2) NULL DEFAULT 1025.00; -- Remuneración Mínima Vital (RMV) de Perú 2025
    
    -- Actualizar registros existentes con el valor por defecto
    UPDATE app.tbl_companies
    SET rmv = 1025.00
    WHERE rmv IS NULL;
    
    PRINT 'Campo rmv agregado a tbl_companies';
END
ELSE
BEGIN
    PRINT 'Campo rmv ya existe en tbl_companies';
END
GO

PRINT 'Migración V138 completada: Campo rmv agregado a companies';
GO

