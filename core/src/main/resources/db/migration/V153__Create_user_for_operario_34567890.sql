-- =============================================
-- V153: CREAR USUARIO PARA OPERARIO 34567890
-- =============================================
-- Verifica y crea el usuario para el empleado 34567890 (Carlos Ramírez)
-- Este empleado participa en tareos desde agosto, septiembre, octubre y noviembre 2025
-- y necesita un usuario para probar la visualización de boletas

DECLARE @empleado_document_number NVARCHAR(15) = '34567890';
DECLARE @perfil_colaborador_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Colaborador' AND deleted_at IS NULL);
DECLARE @password_hash NVARCHAR(255) = '$2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG'; -- Hash de 'admin123'

-- Verificar que el empleado existe
IF EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = @empleado_document_number AND deleted_at IS NULL)
BEGIN
    PRINT 'Empleado encontrado: ' + @empleado_document_number + ' (Carlos Ramírez - Operario de Campo)';
    
    -- Verificar si ya tiene usuario
    IF EXISTS (SELECT 1 FROM app.tbl_users WHERE username = @empleado_document_number AND deleted_at IS NULL)
    BEGIN
        PRINT 'El empleado ' + @empleado_document_number + ' ya tiene un usuario creado.';
        
        -- Verificar que el usuario esté correctamente asociado
        SELECT 
            u.id AS user_id,
            u.username,
            u.employee_id,
            e.person_document_number AS employee_document_number,
            p.names + ' ' + p.paternal_lastname + ' ' + ISNULL(p.maternal_lastname, '') AS employee_full_name,
            pr.name AS profile_name
        FROM app.tbl_users u
        LEFT JOIN app.tbl_employees e ON u.employee_id = e.person_document_number
        LEFT JOIN app.tbl_persons p ON e.person_document_number = p.document_number
        LEFT JOIN app.tbl_profiles pr ON u.profile_id = pr.id
        WHERE u.username = @empleado_document_number
          AND u.deleted_at IS NULL;
    END
    ELSE
    BEGIN
        -- Crear usuario si no existe
        IF @perfil_colaborador_id IS NOT NULL
        BEGIN
            DECLARE @user_id SMALLINT;
            
            INSERT INTO app.tbl_users (employee_id, profile_id, username, password_hash, is_active)
            VALUES (
                @empleado_document_number, -- Número de documento del empleado
                @perfil_colaborador_id,    -- Perfil Colaborador (perfil principal)
                @empleado_document_number, -- Username
                @password_hash,            -- Contraseña hashed (password = username = '34567890')
                1                          -- Activo
            );
            
            SET @user_id = SCOPE_IDENTITY();
            
            PRINT 'Usuario creado exitosamente para empleado: ' + @empleado_document_number + ' (ID: ' + CAST(@user_id AS NVARCHAR(10)) + ')';
            PRINT 'Perfil asignado: Colaborador';
            PRINT 'Username: ' + @empleado_document_number;
            PRINT 'Password: ' + @empleado_document_number + ' (mismo que username)';
            PRINT '';
            PRINT 'Este usuario puede:';
            PRINT '- Ver sus propias boletas de pago';
            PRINT '- Acceder a tareos desde agosto, septiembre, octubre y noviembre 2025';
        END
        ELSE
        BEGIN
            PRINT 'ERROR: Perfil Colaborador no encontrado. No se puede crear el usuario.';
        END
    END
END
ELSE
BEGIN
    PRINT 'ERROR: El empleado ' + @empleado_document_number + ' no existe en la base de datos.';
    PRINT 'Por favor, ejecutar primero la migración V118 que crea este empleado.';
END
GO

