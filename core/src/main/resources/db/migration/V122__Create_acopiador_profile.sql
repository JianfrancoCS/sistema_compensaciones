-- =============================================
-- V122: CREAR PERFIL ACOPIADOR PARA MÓVIL
-- =============================================
-- Perfil específico para usuarios móviles que trabajan como acopiadores
-- Este perfil tendrá acceso a funcionalidades específicas del móvil

-- =============================================
-- CREAR PERFIL ACOPIADOR
-- =============================================
IF NOT EXISTS (SELECT 1 FROM app.tbl_profiles WHERE name = 'Acopiador' AND deleted_at IS NULL)
BEGIN
    INSERT INTO app.tbl_profiles (name, description, is_active)
    VALUES ('Acopiador', 'Perfil para acopiadores que usan la aplicación móvil', 1);
END
GO

PRINT 'Migración V122 completada: Perfil Acopiador creado';
GO

