-- =============================================
-- V104: ELIMINAR auth_role DE tbl_positions
-- =============================================
-- Elimina la columna auth_role (UUID que apuntaba al microservicio auth)
-- La relación entre positions y profiles se maneja a través de tbl_profile_x_positions (tabla histórica)

-- Eliminar la columna auth_role si existe
IF EXISTS (
    SELECT 1 
    FROM sys.columns 
    WHERE object_id = OBJECT_ID('app.tbl_positions') 
    AND name = 'auth_role'
)
BEGIN
    ALTER TABLE app.tbl_positions
    DROP COLUMN auth_role;
    
    PRINT 'Columna auth_role eliminada de tbl_positions';
END
GO

