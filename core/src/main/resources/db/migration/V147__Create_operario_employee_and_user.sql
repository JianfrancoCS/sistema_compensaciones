-- =============================================
-- V147: CREAR EMPLEADO Y USUARIO OPERARIO
-- =============================================
-- Crea el empleado 70000001 y su usuario si no existen

-- 1. Crear persona si no existe
IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '70000001')
BEGIN
    INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
    VALUES ('70000001', 1, 'JUAN', 'OPERARIO', 'PRUEBA', '1990-01-15', 'M');
    PRINT 'Persona 70000001 creada.';
END
ELSE
BEGIN
    PRINT 'Persona 70000001 ya existe.';
END
GO

-- 2. Crear empleado si no existe
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '70000001' AND deleted_at IS NULL)
BEGIN
    DECLARE @fundo1_id SMALLINT = (SELECT TOP 1 id FROM app.tbl_subsidiaries WHERE deleted_at IS NULL ORDER BY id);
    DECLARE @position_operario_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Operario' AND deleted_at IS NULL);
    DECLARE @employee_state_id SMALLINT = (SELECT id FROM app.tbl_states WHERE name = 'Activo' AND deleted_at IS NULL);
    DECLARE @onp_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'ONP' AND deleted_at IS NULL);
    
    IF @fundo1_id IS NULL
    BEGIN
        PRINT 'ERROR: No se encontró ninguna subsidiaria activa.';
    END
    ELSE IF @position_operario_id IS NULL
    BEGIN
        PRINT 'ERROR: No se encontró el cargo Operario.';
    END
    ELSE IF @employee_state_id IS NULL
    BEGIN
        PRINT 'ERROR: No se encontró el estado Activo.';
    END
    ELSE IF @onp_id IS NULL
    BEGIN
        PRINT 'ERROR: No se encontró el concepto ONP.';
    END
    ELSE
    BEGIN
        INSERT INTO app.tbl_employees (
            person_document_number, subsidiary_id, position_id, state_id,
            afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
        )
        VALUES (
            '70000001', @fundo1_id, @position_operario_id, @employee_state_id,
            '70000001TEST01', '001-70000001-0-01', 'BANCO DE LA NACION', '2024-01-01', 50.00, 1500.00, @onp_id
        );
        PRINT 'Empleado 70000001 creado exitosamente.';
    END
END
ELSE
BEGIN
    PRINT 'Empleado 70000001 ya existe.';
END
GO

-- 3. Crear usuario si no existe
IF EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '70000001' AND deleted_at IS NULL)
   AND NOT EXISTS (SELECT 1 FROM app.tbl_users WHERE username = '70000001' AND deleted_at IS NULL)
BEGIN
    DECLARE @perfil_operario_basico_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Operario Básico' AND deleted_at IS NULL);
    DECLARE @perfil_colaborador_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Colaborador' AND deleted_at IS NULL);
    DECLARE @user_operario_id SMALLINT;
    
    IF @perfil_operario_basico_id IS NULL
    BEGIN
        PRINT 'ERROR: No se encontró el perfil Operario Básico.';
    END
    ELSE
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
END
ELSE IF EXISTS (SELECT 1 FROM app.tbl_users WHERE username = '70000001' AND deleted_at IS NULL)
BEGIN
    PRINT 'Usuario 70000001 ya existe.';
END
ELSE
BEGIN
    PRINT 'ERROR: No se puede crear el usuario porque el empleado 70000001 no existe.';
END
GO

