-- =============================================
-- V149: CREAR USUARIO PARA EMPLEADO EN PLANILLA
-- =============================================
-- Busca un empleado que ya tenga boletas generadas (con payslip_pdf_url)
-- y le crea un usuario con el perfil más básico (Colaborador)

DECLARE @empleado_document_number NVARCHAR(15);
DECLARE @perfil_colaborador_id SMALLINT = (SELECT id FROM app.tbl_profiles WHERE name = 'Colaborador' AND deleted_at IS NULL);
DECLARE @password_hash NVARCHAR(255) = '$2a$10$d6zWfnPOSZZPmNPvpEyTZ.M0Mp2QKU8C5DtRe7j.skP2ZMLHeZvsG'; -- Hash de 'admin123'

-- Buscar un empleado que:
-- 1. Tenga boletas generadas (payslip_pdf_url IS NOT NULL)
-- 2. No tenga usuario ya creado
-- 3. Esté activo
SELECT TOP 1 @empleado_document_number = pd.employee_document_number
FROM app.tbl_payroll_details pd
INNER JOIN app.tbl_employees e ON pd.employee_document_number = e.person_document_number
LEFT JOIN app.tbl_users u ON e.person_document_number = u.employee_id AND u.deleted_at IS NULL
WHERE pd.payslip_pdf_url IS NOT NULL
  AND pd.payslip_pdf_url != ''
  AND pd.deleted_at IS NULL
  AND e.deleted_at IS NULL
  AND u.id IS NULL -- Que no tenga usuario ya creado
ORDER BY pd.created_at DESC; -- Tomar el más reciente con boleta

IF @empleado_document_number IS NOT NULL
BEGIN
    PRINT 'Empleado encontrado en planillas con boletas: ' + @empleado_document_number;
    
    -- Verificar que el perfil Colaborador existe
    IF @perfil_colaborador_id IS NOT NULL
    BEGIN
        -- Crear usuario para el empleado encontrado
        IF NOT EXISTS (SELECT 1 FROM app.tbl_users WHERE username = @empleado_document_number AND deleted_at IS NULL)
        BEGIN
            DECLARE @user_id SMALLINT;
            
            INSERT INTO app.tbl_users (employee_id, profile_id, username, password_hash, is_active)
            VALUES (
                @empleado_document_number, -- Número de documento del empleado
                @perfil_colaborador_id,    -- Perfil Colaborador (perfil principal)
                @empleado_document_number, -- Username
                @password_hash,            -- Contraseña hashed
                1                          -- Activo
            );
            
            SET @user_id = SCOPE_IDENTITY();
            
            PRINT 'Usuario creado exitosamente para empleado: ' + @empleado_document_number + ' (ID: ' + CAST(@user_id AS NVARCHAR) + ')';
            PRINT 'Perfil asignado: Colaborador';
            PRINT 'Username y password: ' + @empleado_document_number;
            PRINT '';
            PRINT 'Este usuario puede:';
            PRINT '- Ver sus propias boletas de pago';
            PRINT '- Actualizar su perfil personal';
        END
        ELSE
        BEGIN
            PRINT 'El empleado ' + @empleado_document_number + ' ya tiene un usuario. No se creará uno nuevo.';
        END
    END
    ELSE
    BEGIN
        PRINT 'ERROR: Perfil Colaborador no encontrado. No se puede crear el usuario.';
    END
END
ELSE
BEGIN
    PRINT 'No se encontró ningún empleado en planillas con boletas generadas que no tenga usuario.';
    PRINT 'Verificando empleados en planillas sin boletas...';
    
    -- Buscar cualquier empleado en planillas (aunque no tenga boleta aún)
    SELECT TOP 1 @empleado_document_number = pd.employee_document_number
    FROM app.tbl_payroll_details pd
    INNER JOIN app.tbl_employees e ON pd.employee_document_number = e.person_document_number
    LEFT JOIN app.tbl_users u ON e.person_document_number = u.employee_id AND u.deleted_at IS NULL
    WHERE pd.deleted_at IS NULL
      AND e.deleted_at IS NULL
      AND u.id IS NULL -- Que no tenga usuario ya creado
    ORDER BY pd.created_at DESC;
    
    IF @empleado_document_number IS NOT NULL
    BEGIN
        PRINT 'Empleado encontrado en planillas (sin boleta aún): ' + @empleado_document_number;
        
        IF @perfil_colaborador_id IS NOT NULL
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM app.tbl_users WHERE username = @empleado_document_number AND deleted_at IS NULL)
            BEGIN
                DECLARE @user_id2 SMALLINT;
                
                INSERT INTO app.tbl_users (employee_id, profile_id, username, password_hash, is_active)
                VALUES (
                    @empleado_document_number,
                    @perfil_colaborador_id,
                    @empleado_document_number,
                    @password_hash,
                    1
                );
                
                SET @user_id2 = SCOPE_IDENTITY();
                
                PRINT 'Usuario creado exitosamente para empleado: ' + @empleado_document_number + ' (ID: ' + CAST(@user_id2 AS NVARCHAR) + ')';
                PRINT 'Perfil asignado: Colaborador';
                PRINT 'Username y password: ' + @empleado_document_number;
            END
        END
    END
    ELSE
    BEGIN
        PRINT 'No se encontró ningún empleado en planillas que no tenga usuario.';
    END
END
GO

