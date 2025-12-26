-- =============================================
-- V146: CREAR USUARIO OPERARIO SI NO EXISTE
-- =============================================
-- Verifica y crea el usuario operario 70000001 si no existe

-- Verificar si el empleado existe
IF EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '70000001' AND deleted_at IS NULL)
BEGIN
    PRINT 'Empleado 70000001 existe. Verificando usuario...';
    
    -- Verificar si el usuario existe
    IF NOT EXISTS (SELECT 1 FROM app.tbl_users WHERE username = '70000001' AND deleted_at IS NULL)
    BEGIN
        PRINT 'Usuario 70000001 no existe. Creando...';
        
        DECLARE @perfil_operario_basico_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Operario Básico' AND deleted_at IS NULL);
        DECLARE @perfil_colaborador_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Colaborador' AND deleted_at IS NULL);
        DECLARE @user_operario_id SMALLINT;
        
        IF @perfil_operario_basico_id IS NOT NULL
        BEGIN
            INSERT INTO app.tbl_users (employee_id, profile_id, username, password_hash, is_active)
            VALUES (
                '70000001', -- Número de documento del empleado
                @perfil_operario_basico_id, -- Perfil Operario Básico (perfil principal)
                '70000001',
                '$2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG', -- Hash de admin123
                1
            );
            
            SET @user_operario_id = SCOPE_IDENTITY();
            PRINT 'Usuario 70000001 creado exitosamente. ID: ' + CAST(@user_operario_id AS NVARCHAR);
            
            -- Asignar perfil Colaborador como adicional
            IF @perfil_colaborador_id IS NOT NULL AND @user_operario_id IS NOT NULL
            BEGIN
                INSERT INTO app.tbl_users_profiles (user_id, profile_id, is_active)
                SELECT @user_operario_id, @perfil_colaborador_id, 1
                WHERE NOT EXISTS (
                    SELECT 1 FROM app.tbl_users_profiles up
                    WHERE up.user_id = @user_operario_id
                      AND up.profile_id = @perfil_colaborador_id
                      AND up.deleted_at IS NULL
                );
                PRINT 'Perfil Colaborador asignado como adicional.';
            END
        END
        ELSE
        BEGIN
            PRINT 'ERROR: Perfil Operario Básico no encontrado. No se puede crear el usuario.';
        END
    END
    ELSE
    BEGIN
        PRINT 'Usuario 70000001 ya existe. No se creará uno nuevo.';
    END
END
ELSE
BEGIN
    PRINT 'ERROR: Empleado 70000001 no existe. No se puede crear el usuario.';
END
GO

