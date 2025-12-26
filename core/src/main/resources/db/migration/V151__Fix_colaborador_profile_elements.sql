-- =============================================
-- V151: CORREGIR ELEMENTOS DEL PERFIL COLABORADOR
-- =============================================
-- Asegura que el perfil Colaborador tenga:
-- - my-profile (Mi Perfil)
-- - payslips (Mis Boletas)
-- Y NO tenga:
-- - dashboard (solo para perfiles administrativos)

DECLARE @colaborador_profile_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Colaborador' AND deleted_at IS NULL);

IF @colaborador_profile_id IS NOT NULL
BEGIN
    -- =============================================
    -- 1. Asegurar que my-profile esté asignado
    -- =============================================
    -- Verificar que el elemento existe
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'my-profile' AND deleted_at IS NULL)
    BEGIN
        -- Asignar si no está asignado
        INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
        SELECT @colaborador_profile_id, e.id
        FROM app.tbl_elements e
        WHERE e.name = 'my-profile'
          AND e.deleted_at IS NULL
          AND NOT EXISTS (
              SELECT 1 FROM app.tbl_profiles_elements pe
              WHERE pe.profile_id = @colaborador_profile_id
                AND pe.element_id = e.id
                AND pe.deleted_at IS NULL
          );
        
        PRINT 'Elemento my-profile verificado/agregado al perfil Colaborador';
    END
    ELSE
    BEGIN
        PRINT 'ADVERTENCIA: El elemento my-profile no existe. Debe crearse primero.';
    END

    -- =============================================
    -- 2. Asegurar que payslips esté asignado
    -- =============================================
    IF EXISTS (SELECT 1 FROM app.tbl_elements WHERE name = 'payslips' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
        SELECT @colaborador_profile_id, e.id
        FROM app.tbl_elements e
        WHERE e.name = 'payslips'
          AND e.deleted_at IS NULL
          AND NOT EXISTS (
              SELECT 1 FROM app.tbl_profiles_elements pe
              WHERE pe.profile_id = @colaborador_profile_id
                AND pe.element_id = e.id
                AND pe.deleted_at IS NULL
          );
        
        PRINT 'Elemento payslips verificado/agregado al perfil Colaborador';
    END

    -- =============================================
    -- 3. REMOVER dashboard del perfil Colaborador si está asignado
    -- =============================================
    DECLARE @dashboard_element_id SMALLINT = (SELECT id FROM app.tbl_elements WHERE name = 'dashboard' AND deleted_at IS NULL);
    
    IF @dashboard_element_id IS NOT NULL
    BEGIN
        -- Soft delete de la relación si existe
        UPDATE app.tbl_profiles_elements
        SET deleted_at = GETUTCDATE(),
            deleted_by = 'SYSTEM'
        WHERE profile_id = @colaborador_profile_id
          AND element_id = @dashboard_element_id
          AND deleted_at IS NULL;
        
        IF @@ROWCOUNT > 0
        BEGIN
            PRINT 'Elemento dashboard removido del perfil Colaborador';
        END
        ELSE
        BEGIN
            PRINT 'El perfil Colaborador no tenía dashboard asignado (correcto)';
        END
    END

    -- =============================================
    -- 4. Verificar que la ruta de my-profile sea correcta
    -- =============================================
    UPDATE app.tbl_elements
    SET route = '/system/my-profile'
    WHERE name = 'my-profile'
      AND (route IS NULL OR route != '/system/my-profile')
      AND deleted_at IS NULL;
    
    IF @@ROWCOUNT > 0
    BEGIN
        PRINT 'Ruta de my-profile actualizada a /system/my-profile';
    END
END
ELSE
BEGIN
    PRINT 'ERROR: Perfil Colaborador no encontrado';
END
GO

-- =============================================
-- AGREGAR VARIABLES DEL SISTEMA PARA FIRMAS DE CONTRATOS
-- =============================================
IF NOT EXISTS (SELECT 1 FROM app.tbl_variables WHERE code = 'FIRMA_EMPLEADOR' AND deleted_at IS NULL)
BEGIN
    INSERT INTO app.tbl_variables (code, name, default_value, created_by)
    VALUES ('FIRMA_EMPLEADOR', 'Firma del Empleador', NULL, 'SYSTEM');
    PRINT 'Variable FIRMA_EMPLEADOR creada';
END
GO

IF NOT EXISTS (SELECT 1 FROM app.tbl_variables WHERE code = 'FIRMA_ENCARGADO' AND deleted_at IS NULL)
BEGIN
    INSERT INTO app.tbl_variables (code, name, default_value, created_by)
    VALUES ('FIRMA_ENCARGADO', 'Firma del Encargado del Fundo', NULL, 'SYSTEM');
    PRINT 'Variable FIRMA_ENCARGADO creada';
END
GO

-- =============================================
-- CAMBIAR fileable_id DE BIGINT A NVARCHAR(255) EN tbl_internal_files
-- =============================================
-- Esto permite almacenar tanto IDs numéricos (convertidos a string) como IDs string
-- Los valores existentes se convierten automáticamente de números a strings
-- NO afecta otras tablas porque fileable_id NO es una foreign key real

-- Limpiar columna temporal si existe de una migración anterior fallida
IF EXISTS (SELECT 1 FROM sys.columns 
           WHERE object_id = OBJECT_ID('app.tbl_internal_files') 
           AND name = 'fileable_id_new')
BEGIN
    ALTER TABLE app.tbl_internal_files DROP COLUMN fileable_id_new;
    PRINT 'Columna temporal fileable_id_new eliminada (limpieza de migración anterior)';
END
GO

-- Verificar si la columna ya fue cambiada (ya es NVARCHAR)
IF EXISTS (SELECT 1 FROM sys.columns 
           WHERE object_id = OBJECT_ID('app.tbl_internal_files') 
           AND name = 'fileable_id' 
           AND system_type_id = 167) -- 167 = NVARCHAR en SQL Server
BEGIN
    PRINT 'La columna fileable_id ya es NVARCHAR, no se requiere cambio';
END
ELSE IF EXISTS (SELECT 1 FROM sys.columns 
                WHERE object_id = OBJECT_ID('app.tbl_internal_files') 
                AND name = 'fileable_id')
BEGIN
    -- 1. Agregar columna temporal con el nuevo tipo
    ALTER TABLE app.tbl_internal_files
    ADD fileable_id_new NVARCHAR(255) NULL;
    
    PRINT 'Columna temporal fileable_id_new creada';
END
GO

-- 2. Convertir valores existentes de BIGINT a NVARCHAR (solo si la columna temporal existe)
IF EXISTS (SELECT 1 FROM sys.columns 
           WHERE object_id = OBJECT_ID('app.tbl_internal_files') 
           AND name = 'fileable_id_new')
   AND EXISTS (SELECT 1 FROM sys.columns 
               WHERE object_id = OBJECT_ID('app.tbl_internal_files') 
               AND name = 'fileable_id')
BEGIN
    UPDATE app.tbl_internal_files
    SET fileable_id_new = CAST(fileable_id AS NVARCHAR(255))
    WHERE fileable_id_new IS NULL;
    
    PRINT 'Valores existentes convertidos de BIGINT a NVARCHAR';
END
GO

-- 3. Hacer la columna NOT NULL (solo si la columna temporal existe)
IF EXISTS (SELECT 1 FROM sys.columns 
           WHERE object_id = OBJECT_ID('app.tbl_internal_files') 
           AND name = 'fileable_id_new')
BEGIN
    ALTER TABLE app.tbl_internal_files
    ALTER COLUMN fileable_id_new NVARCHAR(255) NOT NULL;
    
    PRINT 'Columna temporal fileable_id_new configurada como NOT NULL';
END
GO

-- 4. Eliminar índice antiguo (si existe)
IF EXISTS (SELECT 1 FROM sys.indexes 
           WHERE object_id = OBJECT_ID('app.tbl_internal_files') 
           AND name = 'IX_internal_files_fileable')
BEGIN
    DROP INDEX IX_internal_files_fileable ON app.tbl_internal_files;
    PRINT 'Índice antiguo IX_internal_files_fileable eliminado';
END
GO

-- 5. Eliminar columna antigua (solo si la columna temporal existe y la antigua existe)
IF EXISTS (SELECT 1 FROM sys.columns 
           WHERE object_id = OBJECT_ID('app.tbl_internal_files') 
           AND name = 'fileable_id_new')
   AND EXISTS (SELECT 1 FROM sys.columns 
               WHERE object_id = OBJECT_ID('app.tbl_internal_files') 
               AND name = 'fileable_id')
BEGIN
    ALTER TABLE app.tbl_internal_files
    DROP COLUMN fileable_id;
    
    PRINT 'Columna antigua fileable_id eliminada';
END
GO

-- 6. Renombrar columna nueva (solo si existe fileable_id_new)
IF EXISTS (SELECT 1 FROM sys.columns 
           WHERE object_id = OBJECT_ID('app.tbl_internal_files') 
           AND name = 'fileable_id_new')
BEGIN
    EXEC sp_rename 'app.tbl_internal_files.fileable_id_new', 'fileable_id', 'COLUMN';
    
    PRINT 'Columna renombrada a fileable_id';
END
GO

-- 7. Recrear índice (solo si fileable_id existe como NVARCHAR)
IF EXISTS (SELECT 1 FROM sys.columns 
           WHERE object_id = OBJECT_ID('app.tbl_internal_files') 
           AND name = 'fileable_id' 
           AND system_type_id = 167) -- 167 = NVARCHAR
   AND NOT EXISTS (SELECT 1 FROM sys.indexes 
                   WHERE object_id = OBJECT_ID('app.tbl_internal_files') 
                   AND name = 'IX_internal_files_fileable')
BEGIN
    CREATE INDEX IX_internal_files_fileable 
    ON app.tbl_internal_files(fileable_id, fileable_type) 
    WHERE deleted_at IS NULL;
    
    PRINT 'Índice IX_internal_files_fileable recreado';
    PRINT 'Migración de fileable_id de BIGINT a NVARCHAR(255) completada exitosamente';
END
GO

PRINT 'Migración V151 completada exitosamente';
GO

