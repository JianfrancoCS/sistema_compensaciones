-- =============================================
-- V144: VERIFICAR TAREOS DEL EMPLEADO 34567890
-- =============================================
-- Verifica en qué tareos está el empleado 34567890
-- y en qué fechas, para los períodos: agosto, septiembre, octubre y noviembre

-- =============================================
-- CONSULTA DE TAREOS POR EMPLEADO
-- =============================================

PRINT '========================================';
PRINT 'TAREOS DEL EMPLEADO: 34567890';
PRINT '========================================';
PRINT '';

-- Consulta detallada de tareos
SELECT 
    t.id AS tareo_id,
    t.public_id AS tareo_public_id,
    t.temporal_id,
    CAST(t.created_at AS DATE) AS fecha_tareo,
    FORMAT(t.created_at, 'dd/MM/yyyy HH:mm') AS fecha_hora_completa,
    l.name AS labor_nombre,
    l.is_piecework AS labor_destajo,
    lo.name AS lote_nombre,
    e_supervisor.person_document_number AS supervisor_dni,
    p_supervisor.names + ' ' + p_supervisor.paternal_lastname AS supervisor_nombre,
    te.start_time AS hora_inicio,
    te.end_time AS hora_fin,
    te.actual_hours AS horas_reales,
    te.paid_hours AS horas_pagadas,
    te.productivity AS productividad
FROM app.tbl_tareo_employees te
INNER JOIN app.tbl_tareos t ON te.tareo_id = t.id
INNER JOIN app.tbl_employees e ON te.employee_document_number = e.person_document_number
INNER JOIN app.tbl_labors l ON t.labor_id = l.id
LEFT JOIN app.tbl_lotes lo ON t.lote_id = lo.id
INNER JOIN app.tbl_employees e_supervisor ON t.supervisor_employee_document_number = e_supervisor.person_document_number
INNER JOIN app.tbl_persons p_supervisor ON e_supervisor.person_document_number = p_supervisor.document_number
WHERE e.person_document_number = '34567890'
  AND te.deleted_at IS NULL
  AND t.deleted_at IS NULL
  AND e.deleted_at IS NULL
ORDER BY t.created_at DESC;

PRINT '';
PRINT '========================================';
PRINT 'RESUMEN POR MES';
PRINT '========================================';
PRINT '';

-- Resumen por mes
SELECT 
    YEAR(t.created_at) AS año,
    MONTH(t.created_at) AS mes,
    DATENAME(MONTH, t.created_at) AS nombre_mes,
    COUNT(DISTINCT t.id) AS cantidad_tareos,
    SUM(te.actual_hours) AS total_horas_reales,
    SUM(te.paid_hours) AS total_horas_pagadas,
    SUM(te.productivity) AS total_productividad
FROM app.tbl_tareo_employees te
INNER JOIN app.tbl_tareos t ON te.tareo_id = t.id
INNER JOIN app.tbl_employees e ON te.employee_document_number = e.person_document_number
WHERE e.person_document_number = '34567890'
  AND te.deleted_at IS NULL
  AND t.deleted_at IS NULL
  AND e.deleted_at IS NULL
  AND YEAR(t.created_at) = 2025
  AND MONTH(t.created_at) IN (8, 9, 10, 11) -- Agosto, Septiembre, Octubre, Noviembre
GROUP BY YEAR(t.created_at), MONTH(t.created_at), DATENAME(MONTH, t.created_at)
ORDER BY YEAR(t.created_at), MONTH(t.created_at);

PRINT '';
PRINT '========================================';
PRINT 'VERIFICACIÓN DE PERÍODOS';
PRINT '========================================';
PRINT '';

-- Verificar si hay tareos en los períodos específicos
DECLARE @tareos_agosto INT = (
    SELECT COUNT(DISTINCT t.id)
    FROM app.tbl_tareo_employees te
    INNER JOIN app.tbl_tareos t ON te.tareo_id = t.id
    INNER JOIN app.tbl_employees e ON te.employee_document_number = e.person_document_number
    WHERE e.person_document_number = '34567890'
      AND te.deleted_at IS NULL
      AND t.deleted_at IS NULL
      AND YEAR(t.created_at) = 2025
      AND MONTH(t.created_at) = 8
);

DECLARE @tareos_septiembre INT = (
    SELECT COUNT(DISTINCT t.id)
    FROM app.tbl_tareo_employees te
    INNER JOIN app.tbl_tareos t ON te.tareo_id = t.id
    INNER JOIN app.tbl_employees e ON te.employee_document_number = e.person_document_number
    WHERE e.person_document_number = '34567890'
      AND te.deleted_at IS NULL
      AND t.deleted_at IS NULL
      AND YEAR(t.created_at) = 2025
      AND MONTH(t.created_at) = 9
);

DECLARE @tareos_octubre INT = (
    SELECT COUNT(DISTINCT t.id)
    FROM app.tbl_tareo_employees te
    INNER JOIN app.tbl_tareos t ON te.tareo_id = t.id
    INNER JOIN app.tbl_employees e ON te.employee_document_number = e.person_document_number
    WHERE e.person_document_number = '34567890'
      AND te.deleted_at IS NULL
      AND t.deleted_at IS NULL
      AND YEAR(t.created_at) = 2025
      AND MONTH(t.created_at) = 10
);

DECLARE @tareos_noviembre INT = (
    SELECT COUNT(DISTINCT t.id)
    FROM app.tbl_tareo_employees te
    INNER JOIN app.tbl_tareos t ON te.tareo_id = t.id
    INNER JOIN app.tbl_employees e ON te.employee_document_number = e.person_document_number
    WHERE e.person_document_number = '34567890'
      AND te.deleted_at IS NULL
      AND t.deleted_at IS NULL
      AND YEAR(t.created_at) = 2025
      AND MONTH(t.created_at) = 11
);

PRINT 'Agosto 2025: ' + CAST(@tareos_agosto AS NVARCHAR(10)) + ' tareo(s)';
PRINT 'Septiembre 2025: ' + CAST(@tareos_septiembre AS NVARCHAR(10)) + ' tareo(s)';
PRINT 'Octubre 2025: ' + CAST(@tareos_octubre AS NVARCHAR(10)) + ' tareo(s)';
PRINT 'Noviembre 2025: ' + CAST(@tareos_noviembre AS NVARCHAR(10)) + ' tareo(s)';
PRINT '';

GO

