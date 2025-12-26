-- =============================================
-- V152: CREAR ELEMENTO "FOTCHECK" Y ASOCIARLO AL PERFIL COLABORADOR
-- =============================================
-- Elemento básico asociado al colaborador que permite visualizar
-- e imprimir el fotocheck del empleado

-- =============================================
-- CREAR ELEMENTO FOTCHECK
-- =============================================
IF NOT EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'fotocheck')
BEGIN
    INSERT INTO app.tbl_elements (name, display_name, route, icon, container_id, order_index, is_active)
    VALUES ('fotocheck', 'Fotocheck', '/system/fotocheck', 'pi pi-id-card', NULL, 2, 1);
END
GO

-- =============================================
-- ASIGNAR ELEMENTO AL PERFIL COLABORADOR
-- =============================================
DECLARE @colaborador_profile_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Colaborador' AND deleted_at IS NULL);

IF @colaborador_profile_id IS NOT NULL
BEGIN
    -- Asignar elemento: Fotocheck
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @colaborador_profile_id, e.id
    FROM app.tbl_elements e
    WHERE e.name = 'fotocheck'
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @colaborador_profile_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );
END
GO

