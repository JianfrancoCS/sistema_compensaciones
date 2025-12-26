-- =============================================
-- V142: CREAR USUARIOS DE PRUEBA PARA BOLETAS
-- =============================================
-- Crea usuarios de prueba (operario, administrativo y jefe de RRHH) con perfiles específicos
-- para poder generar boletas y probar el sistema
-- Todos los usuarios tienen el perfil base "Colaborador" + su perfil específico

-- =============================================
-- 1. CREAR PERFILES
-- =============================================

-- Perfil: Operario Básico (solo boletas y mi perfil)
IF NOT EXISTS (SELECT 1 FROM app.tbl_profiles WHERE name = 'Operario Básico' AND deleted_at IS NULL)
BEGIN
    INSERT INTO app.tbl_profiles (name, description, is_active)
    VALUES ('Operario Básico', 'Perfil básico para operarios. Permite ver boletas y actualizar datos personales', 1);
END
GO

-- Perfil: Administrativo Completo (boletas + módulos administrativos)
IF NOT EXISTS (SELECT 1 FROM app.tbl_profiles WHERE name = 'Administrativo Completo' AND deleted_at IS NULL)
BEGIN
    INSERT INTO app.tbl_profiles (name, description, is_active)
    VALUES ('Administrativo Completo', 'Perfil completo para personal administrativo. Acceso a planillas, empleados, contratos y más', 1);
END
GO

-- =============================================
-- 2. ASIGNAR ELEMENTOS A PERFILES
-- =============================================

-- Perfil Operario Básico: Solo boletas y mi perfil
-- NOTA: El perfil "Colaborador" ya tiene estos elementos (my-profile y payslips)
-- Este perfil es específico para operarios, pero el perfil Colaborador se asignará como adicional
-- para que todos tengan acceso base a boletas y mi perfil
DECLARE @perfil_operario_basico_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Operario Básico' AND deleted_at IS NULL);

IF @perfil_operario_basico_id IS NOT NULL
BEGIN
    -- Asignar: Mi Perfil (aunque ya está en Colaborador, lo asignamos aquí también por si acaso)
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @perfil_operario_basico_id, e.id
    FROM app.tbl_elements e
    WHERE e.name = 'my-profile'
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @perfil_operario_basico_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );

    -- Asignar: Mis Boletas (aunque ya está en Colaborador, lo asignamos aquí también por si acaso)
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @perfil_operario_basico_id, e.id
    FROM app.tbl_elements e
    WHERE e.name = 'payslips'
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @perfil_operario_basico_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );
END
GO

-- Perfil Administrativo Completo: Boletas + módulos administrativos
-- NOTA: El perfil "Colaborador" ya tiene my-profile y payslips
-- Este perfil agrega módulos administrativos adicionales
DECLARE @perfil_admin_completo_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Administrativo Completo' AND deleted_at IS NULL);

IF @perfil_admin_completo_id IS NOT NULL
BEGIN
    -- Asignar: Mi Perfil (aunque ya está en Colaborador, lo asignamos aquí también)
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @perfil_admin_completo_id, e.id
    FROM app.tbl_elements e
    WHERE e.name = 'my-profile'
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @perfil_admin_completo_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );

    -- Asignar: Mis Boletas (aunque ya está en Colaborador, lo asignamos aquí también)
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @perfil_admin_completo_id, e.id
    FROM app.tbl_elements e
    WHERE e.name = 'payslips'
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @perfil_admin_completo_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );

    -- Asignar módulos administrativos: Planillas, Empleados, Contratos, Asistencia, Calendario, Períodos
    INSERT INTO app.tbl_profiles_elements (profile_id, element_id)
    SELECT @perfil_admin_completo_id, e.id
    FROM app.tbl_elements e
    WHERE e.name IN ('payrolls', 'employees', 'contracts', 'attendance', 'calendar', 'periods')
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_profiles_elements pe
          WHERE pe.profile_id = @perfil_admin_completo_id
            AND pe.element_id = e.id
            AND pe.deleted_at IS NULL
      );
END
GO

-- =============================================
-- 3. CREAR EMPLEADOS DE PRUEBA
-- =============================================

-- Empleado 1: Operario de prueba (con bonos por producción)
-- DNI: 70000001
IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '70000001')
BEGIN
    INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
    VALUES ('70000001', 1, 'JUAN', 'OPERARIO', 'PRUEBA', '1990-01-15', 'M');
END
GO

-- Obtener IDs necesarios para el empleado operario
DECLARE @fundo1_id SMALLINT = (SELECT TOP 1 id FROM app.tbl_subsidiaries WHERE deleted_at IS NULL ORDER BY id);
DECLARE @position_operario_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Operario' AND deleted_at IS NULL);
DECLARE @employee_state_id SMALLINT = (SELECT id FROM app.tbl_states WHERE name = 'Activo' AND deleted_at IS NULL);
DECLARE @onp_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'ONP' AND deleted_at IS NULL);

IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '70000001')
BEGIN
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    SELECT 
        '70000001', @fundo1_id, @position_operario_id, @employee_state_id,
        '70000001TEST01', '001-70000001-0-01', 'BANCO DE LA NACION', '2024-01-01', 50.00, 1500.00, @onp_id
    WHERE @fundo1_id IS NOT NULL AND @position_operario_id IS NOT NULL AND @employee_state_id IS NOT NULL AND @onp_id IS NOT NULL;
END
GO

-- Empleado 2: Administrativo de prueba
-- DNI: 70000002
IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '70000002')
BEGIN
    INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
    VALUES ('70000002', 1, 'MARIA', 'ADMINISTRATIVA', 'PRUEBA', '1988-05-20', 'F');
END
GO

-- Obtener IDs necesarios para el empleado administrativo
DECLARE @fundo1_id_admin SMALLINT = (SELECT TOP 1 id FROM app.tbl_subsidiaries WHERE deleted_at IS NULL ORDER BY id);
DECLARE @position_asistente_admin_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Asistente Administrativo' AND deleted_at IS NULL);
DECLARE @employee_state_id_admin SMALLINT = (SELECT id FROM app.tbl_states WHERE name = 'Activo' AND deleted_at IS NULL);
DECLARE @afp_integra_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'AFP_INTEGRA' AND deleted_at IS NULL);

IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '70000002')
BEGIN
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    SELECT 
        '70000002', @fundo1_id_admin, @position_asistente_admin_id, @employee_state_id_admin,
        '70000002TEST02', '002-70000002-0-02', 'BANCO DE CREDITO DEL PERU', '2023-06-01', 73.33, 2200.00, @afp_integra_id
    WHERE @fundo1_id_admin IS NOT NULL AND @position_asistente_admin_id IS NOT NULL AND @employee_state_id_admin IS NOT NULL AND @afp_integra_id IS NOT NULL;
END
GO

-- Empleado 3: Jefe de Recursos Humanos de prueba
-- Número de documento: 70000003
IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '70000003')
BEGIN
    INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
    VALUES ('70000003', 1, 'CARLOS', 'RRHH', 'JEFE', '1985-03-10', 'M');
END
GO

-- Obtener IDs necesarios para el empleado jefe de RRHH
DECLARE @fundo1_id_rrhh SMALLINT = (SELECT TOP 1 id FROM app.tbl_subsidiaries WHERE deleted_at IS NULL ORDER BY id);
DECLARE @position_jefe_rrhh_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Encargado de Recursos Humanos' AND deleted_at IS NULL);
DECLARE @employee_state_id_rrhh SMALLINT = (SELECT id FROM app.tbl_states WHERE name = 'Activo' AND deleted_at IS NULL);
DECLARE @afp_integra_id_rrhh SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'AFP_INTEGRA' AND deleted_at IS NULL);

IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '70000003')
BEGIN
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    SELECT 
        '70000003', @fundo1_id_rrhh, @position_jefe_rrhh_id, @employee_state_id_rrhh,
        '70000003TEST03', '003-70000003-0-03', 'BANCO DE CREDITO DEL PERU', '2022-01-15', 106.67, 3200.00, @afp_integra_id_rrhh
    WHERE @fundo1_id_rrhh IS NOT NULL AND @position_jefe_rrhh_id IS NOT NULL AND @employee_state_id_rrhh IS NOT NULL AND @afp_integra_id_rrhh IS NOT NULL;
END
GO

-- =============================================
-- 4. CREAR USUARIOS DE PRUEBA
-- =============================================

-- Usuario 1: Operario (username = password = '70000001')
-- Usar el mismo hash de contraseña que admin123 (password = username, pero hash de admin123)
-- Hash: $2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG
-- Obtener IDs de perfiles
DECLARE @perfil_operario_basico_id_final SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Operario Básico' AND deleted_at IS NULL);
DECLARE @perfil_colaborador_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Colaborador' AND deleted_at IS NULL);

-- Verificar que el empleado existe antes de crear el usuario
IF EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '70000001' AND deleted_at IS NULL)
   AND NOT EXISTS (SELECT 1 FROM app.tbl_users WHERE username = '70000001' AND deleted_at IS NULL)
BEGIN
    DECLARE @user_operario_id SMALLINT;
    
    INSERT INTO app.tbl_users (employee_id, profile_id, username, password_hash, is_active)
    VALUES (
        '70000001', -- Número de documento del empleado
        @perfil_operario_basico_id_final, -- Perfil Operario Básico (perfil principal)
        '70000001',
        '$2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG', -- Mismo hash que admin123 (password = username)
        1
    );
    
    SET @user_operario_id = SCOPE_IDENTITY();
    
    -- Asignar perfil Colaborador como adicional (perfil base para todos)
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
    END
END
GO

-- Usuario 2: Administrativo (username = password = '70000002')
-- Usar el mismo hash de contraseña que admin123 (password = username, pero hash de admin123)
-- Hash: $2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG
-- Obtener IDs de perfiles
DECLARE @perfil_admin_completo_id_final SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Administrativo Completo' AND deleted_at IS NULL);
DECLARE @perfil_colaborador_id_admin SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Colaborador' AND deleted_at IS NULL);

-- Verificar que el empleado existe antes de crear el usuario
IF EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '70000002' AND deleted_at IS NULL)
   AND NOT EXISTS (SELECT 1 FROM app.tbl_users WHERE username = '70000002' AND deleted_at IS NULL)
BEGIN
    DECLARE @user_admin_id SMALLINT;
    
    INSERT INTO app.tbl_users (employee_id, profile_id, username, password_hash, is_active)
    VALUES (
        '70000002', -- Número de documento del empleado
        @perfil_admin_completo_id_final, -- Perfil Administrativo Completo (perfil principal)
        '70000002',
        '$2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG', -- Mismo hash que admin123 (password = username)
        1
    );
    
    SET @user_admin_id = SCOPE_IDENTITY();
    
    -- Asignar perfil Colaborador como adicional (perfil base para todos)
    IF @perfil_colaborador_id_admin IS NOT NULL AND @user_admin_id IS NOT NULL
    BEGIN
        INSERT INTO app.tbl_users_profiles (user_id, profile_id, is_active)
        SELECT @user_admin_id, @perfil_colaborador_id_admin, 1
        WHERE NOT EXISTS (
            SELECT 1 FROM app.tbl_users_profiles up
            WHERE up.user_id = @user_admin_id
              AND up.profile_id = @perfil_colaborador_id_admin
              AND up.deleted_at IS NULL
        );
    END
END
GO

-- Usuario 3: Jefe de Recursos Humanos (username = número de documento = '70000003')
-- Usar el mismo hash de contraseña que admin123 (password = username, pero hash de admin123)
-- Hash: $2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG
-- Obtener IDs de perfiles
DECLARE @perfil_rrhh_id_final SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'RRHH' AND deleted_at IS NULL);
DECLARE @perfil_colaborador_id_rrhh SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Colaborador' AND deleted_at IS NULL);

-- Verificar que el empleado existe antes de crear el usuario
IF EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '70000003' AND deleted_at IS NULL)
   AND NOT EXISTS (SELECT 1 FROM app.tbl_users WHERE username = '70000003' AND deleted_at IS NULL)
BEGIN
    DECLARE @user_rrhh_id SMALLINT;
    
    INSERT INTO app.tbl_users (employee_id, profile_id, username, password_hash, is_active)
    VALUES (
        '70000003', -- Número de documento del empleado
        @perfil_rrhh_id_final, -- Perfil RRHH (perfil principal)
        '70000003', -- Username = número de documento
        '$2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG', -- Mismo hash que admin123 (password = username)
        1
    );
    
    SET @user_rrhh_id = SCOPE_IDENTITY();
    
    -- Asignar perfil Colaborador como adicional (perfil base para todos)
    IF @perfil_colaborador_id_rrhh IS NOT NULL AND @user_rrhh_id IS NOT NULL
    BEGIN
        INSERT INTO app.tbl_users_profiles (user_id, profile_id, is_active)
        SELECT @user_rrhh_id, @perfil_colaborador_id_rrhh, 1
        WHERE NOT EXISTS (
            SELECT 1 FROM app.tbl_users_profiles up
            WHERE up.user_id = @user_rrhh_id
              AND up.profile_id = @perfil_colaborador_id_rrhh
              AND up.deleted_at IS NULL
        );
    END
END
GO

