-- =============================================
-- V143: CREAR USUARIO PARA EMPLEADO EN TAREOS
-- =============================================
-- Verifica si el usuario 70000002 está en algún tareo
-- Si no está, busca cualquier empleado que esté en tareos y le crea un usuario
-- con el perfil más básico (Colaborador)

-- =============================================
-- VERIFICAR Y CREAR USUARIO SI ES NECESARIO
-- =============================================

DECLARE @usuario_70000002_en_tareo BIT = 0;
DECLARE @empleado_document_number NVARCHAR(15) = NULL;

-- Verificar si el empleado 70000002 está en algún tareo
IF EXISTS (
    SELECT 1 
    FROM app.tbl_tareo_employees te
    INNER JOIN app.tbl_tareos t ON te.tareo_id = t.id
    INNER JOIN app.tbl_employees e ON te.employee_document_number = e.person_document_number
    WHERE e.person_document_number = '70000002'
      AND te.deleted_at IS NULL
      AND t.deleted_at IS NULL
      AND e.deleted_at IS NULL
)
BEGIN
    SET @usuario_70000002_en_tareo = 1;
    SET @empleado_document_number = '70000002';
    PRINT 'El usuario 70000002 SÍ está en tareos. No se necesita crear usuario.';
END
ELSE
BEGIN
    PRINT 'El usuario 70000002 NO está en tareos. Buscando otro empleado en tareos...';
    
    -- Buscar cualquier empleado que esté en tareos y que no tenga usuario
    SELECT TOP 1 @empleado_document_number = e.person_document_number
    FROM app.tbl_tareo_employees te
    INNER JOIN app.tbl_tareos t ON te.tareo_id = t.id
    INNER JOIN app.tbl_employees e ON te.employee_document_number = e.person_document_number
    WHERE te.deleted_at IS NULL
      AND t.deleted_at IS NULL
      AND e.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM app.tbl_users u 
          WHERE u.employee_id = e.person_document_number 
            AND u.deleted_at IS NULL
      )
    ORDER BY t.created_at DESC; -- Tomar el más reciente
    
    IF @empleado_document_number IS NOT NULL
    BEGIN
        PRINT 'Empleado encontrado en tareos: ' + @empleado_document_number;
        
        -- Obtener ID del perfil Colaborador
        DECLARE @perfil_colaborador_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Colaborador' AND deleted_at IS NULL);
        
        -- Verificar que el empleado existe y no tiene usuario
        IF EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = @empleado_document_number AND deleted_at IS NULL)
           AND NOT EXISTS (SELECT 1 FROM app.tbl_users WHERE username = @empleado_document_number AND deleted_at IS NULL)
           AND @perfil_colaborador_id IS NOT NULL
        BEGIN
            DECLARE @user_id SMALLINT;
            
            -- Crear usuario con perfil Colaborador (el más básico)
            INSERT INTO app.tbl_users (employee_id, profile_id, username, password_hash, is_active)
            VALUES (
                @empleado_document_number,
                @perfil_colaborador_id, -- Perfil Colaborador (perfil principal)
                @empleado_document_number,
                '$2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG', -- Mismo hash que admin123 (password = username)
                1
            );
            
            SET @user_id = SCOPE_IDENTITY();
            
            PRINT 'Usuario creado exitosamente para empleado: ' + @empleado_document_number + ' (ID: ' + CAST(@user_id AS NVARCHAR(10)) + ')';
            PRINT 'Perfil asignado: Colaborador';
            PRINT 'Username y password: ' + @empleado_document_number;
        END
        ELSE
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = @empleado_document_number AND deleted_at IS NULL)
            BEGIN
                PRINT 'ERROR: El empleado ' + @empleado_document_number + ' no existe.';
            END
            ELSE IF EXISTS (SELECT 1 FROM app.tbl_users WHERE username = @empleado_document_number AND deleted_at IS NULL)
            BEGIN
                PRINT 'INFO: El empleado ' + @empleado_document_number + ' ya tiene un usuario.';
            END
            ELSE IF @perfil_colaborador_id IS NULL
            BEGIN
                PRINT 'ERROR: El perfil Colaborador no existe.';
            END
        END
    END
    ELSE
    BEGIN
        PRINT 'No se encontró ningún empleado en tareos sin usuario.';
    END
END
GO

