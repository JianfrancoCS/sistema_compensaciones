-- V047: Agregar campo salary a positions (si no existe)
-- Nota: Si la columna ya existe (por ejemplo, si se creó en V008), esta migración no hace nada

IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('app.tbl_positions') 
    AND name = 'salary'
)
BEGIN
    ALTER TABLE app.tbl_positions
    ADD salary DECIMAL(10, 2) NULL;
    
    PRINT 'Campo salary agregado a tbl_positions';
END
ELSE
BEGIN
    PRINT 'Campo salary ya existe en tbl_positions';
END
GO