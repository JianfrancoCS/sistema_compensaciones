-- =============================================
-- V145: VERIFICAR ASOCIACIÓN USUARIO-EMPLEADO
-- =============================================
-- Verifica que los usuarios de prueba tengan correctamente asociado su employeeId

-- Verificar usuario operario (70000001)
SELECT 
    u.id AS user_id,
    u.username,
    u.employee_id,
    e.person_document_number AS employee_document_number,
    p.names + ' ' + p.paternal_lastname + ' ' + ISNULL(p.maternal_lastname, '') AS employee_full_name
FROM app.tbl_users u
LEFT JOIN app.tbl_employees e ON u.employee_id = e.person_document_number
LEFT JOIN app.tbl_persons p ON e.person_document_number = p.document_number
WHERE u.username = '70000001'
  AND u.deleted_at IS NULL;

-- Verificar usuario administrativo (70000002)
SELECT 
    u.id AS user_id,
    u.username,
    u.employee_id,
    e.person_document_number AS employee_document_number,
    p.names + ' ' + p.paternal_lastname + ' ' + ISNULL(p.maternal_lastname, '') AS employee_full_name
FROM app.tbl_users u
LEFT JOIN app.tbl_employees e ON u.employee_id = e.person_document_number
LEFT JOIN app.tbl_persons p ON e.person_document_number = p.document_number
WHERE u.username = '70000002'
  AND u.deleted_at IS NULL;

-- Verificar usuario RRHH (70000003)
SELECT 
    u.id AS user_id,
    u.username,
    u.employee_id,
    e.person_document_number AS employee_document_number,
    p.names + ' ' + p.paternal_lastname + ' ' + ISNULL(p.maternal_lastname, '') AS employee_full_name
FROM app.tbl_users u
LEFT JOIN app.tbl_employees e ON u.employee_id = e.person_document_number
LEFT JOIN app.tbl_persons p ON e.person_document_number = p.document_number
WHERE u.username = '70000003'
  AND u.deleted_at IS NULL;

-- Verificar usuario de tareo (34567890)
SELECT 
    u.id AS user_id,
    u.username,
    u.employee_id,
    e.person_document_number AS employee_document_number,
    p.names + ' ' + p.paternal_lastname + ' ' + ISNULL(p.maternal_lastname, '') AS employee_full_name
FROM app.tbl_users u
LEFT JOIN app.tbl_employees e ON u.employee_id = e.person_document_number
LEFT JOIN app.tbl_persons p ON e.person_document_number = p.document_number
WHERE u.username = '34567890'
  AND u.deleted_at IS NULL;

GO

