-- =============================================
-- V140: CREAR QR ROLLS, QR CODES Y HARVEST RECORDS PARA TAREOS DE DESTAJO
-- =============================================
-- Crea QR rolls, QR codes y harvest records consistentes con los tareos de destajo
-- existentes en OCTUBRE y NOVIEMBRE 2025 (meses 10 y 11), asegurando que la productividad sea razonable
-- Solo procesa labores de destajo (is_piecework = 1)
-- Los tareos administrativos (is_piecework = 0) mantienen productividad NULL

-- =============================================
-- 1. OBTENER DATOS BASE
-- =============================================

-- Obtener todas las labores de destajo para procesar
-- Procesaremos todas las labores de destajo en octubre y noviembre
PRINT N'Procesando tareos de destajo para OCTUBRE y NOVIEMBRE 2025 (meses 10 y 11)';

-- =============================================
-- 2. PROCESAR TAREOS DE DESTAJO
-- =============================================

-- Cursor para iterar sobre tareos de destajo en OCTUBRE y NOVIEMBRE 2025 (meses 10 y 11)
-- Solo procesa labores de destajo (is_piecework = 1)
-- Los tareos administrativos (is_piecework = 0) NO se procesan y mantienen productividad NULL
DECLARE tareo_cursor CURSOR FOR
SELECT 
    t.id AS tareo_id,
    t.temporal_id,
    t.created_at AS tareo_date,
    te.id AS tareo_employee_id,
    te.employee_document_number,
    te.public_id AS tareo_employee_public_id,
    l.id AS labor_id,
    l.min_task_requirement AS min_task_requirement
FROM app.tbl_tareos t
INNER JOIN app.tbl_tareo_employees te ON te.tareo_id = t.id
INNER JOIN app.tbl_labors l ON l.id = t.labor_id
WHERE l.is_piecework = 1  -- SOLO labores de destajo
  AND t.deleted_at IS NULL
  AND te.deleted_at IS NULL
  AND l.deleted_at IS NULL
  AND YEAR(t.created_at) = 2025
  AND MONTH(t.created_at) IN (10, 11)  -- OCTUBRE y NOVIEMBRE (meses 10 y 11)
  AND l.min_task_requirement IS NOT NULL  -- Solo labores con min_task_requirement definido
  AND l.min_task_requirement > 0
ORDER BY t.created_at, te.employee_document_number;

DECLARE @tareo_id INT;
DECLARE @temporal_id NVARCHAR(255);
DECLARE @tareo_date DATETIME2;
DECLARE @tareo_employee_id BIGINT;
DECLARE @employee_doc NVARCHAR(15);
DECLARE @tareo_employee_public_id UNIQUEIDENTIFIER;
DECLARE @assigned_date DATE;
DECLARE @labor_id SMALLINT;
DECLARE @min_task_requirement DECIMAL(10,2);

DECLARE @qr_roll_id INT;
DECLARE @qr_roll_public_id UNIQUEIDENTIFIER;
DECLARE @qr_roll_employee_id BIGINT;

-- Contador para variar la productividad
DECLARE @productivity_counter INT = 0;

OPEN tareo_cursor;
FETCH NEXT FROM tareo_cursor INTO @tareo_id, @temporal_id, @tareo_date, @tareo_employee_id, @employee_doc, @tareo_employee_public_id, @labor_id, @min_task_requirement;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @assigned_date = CAST(@tareo_date AS DATE);
    SET @productivity_counter = @productivity_counter + 1;
    
    -- =============================================
    -- 3. CREAR QR ROLL PARA ESTE EMPLEADO EN ESTA FECHA
    -- =============================================
    
    -- Verificar si ya existe un qr_roll_employee para este empleado en esta fecha
    IF NOT EXISTS (
        SELECT 1 FROM app.tbl_qr_roll_employees qre
        WHERE qre.employee_document_number = @employee_doc
          AND qre.assigned_date = @assigned_date
          AND qre.deleted_at IS NULL
    )
    BEGIN
        -- Crear nuevo QR Roll
        INSERT INTO app.tbl_qr_rolls (max_qr_codes_per_day, created_by)
        VALUES (50, 'SYSTEM'); -- 50 códigos por día es razonable
        
        SET @qr_roll_id = SCOPE_IDENTITY();
        SELECT @qr_roll_public_id = public_id FROM app.tbl_qr_rolls WHERE id = @qr_roll_id;
        
        -- Asignar QR Roll al empleado en esta fecha
        INSERT INTO app.tbl_qr_roll_employees (qr_roll_id, employee_document_number, assigned_date, created_by)
        VALUES (@qr_roll_id, @employee_doc, @assigned_date, 'SYSTEM');
        
        SET @qr_roll_employee_id = SCOPE_IDENTITY();
        
        PRINT N'QR Roll creado para empleado ' + @employee_doc + N' en fecha ' + CAST(@assigned_date AS NVARCHAR(10)) + N'. Roll ID: ' + CAST(@qr_roll_id AS NVARCHAR(10));
        
        -- =============================================
        -- 4. CREAR QR CODES PARA ESTE ROLL
        -- =============================================
        
        -- Crear 40-50 QR codes por roll (suficientes para la productividad esperada)
        DECLARE @qr_code_count INT = 40 + (@productivity_counter % 11); -- Entre 40 y 50 códigos
        DECLARE @qr_code_counter INT = 0;
        
        WHILE @qr_code_counter < @qr_code_count
        BEGIN
            INSERT INTO app.tbl_qr_codes (qr_roll_id, is_used, is_printed, created_by)
            VALUES (@qr_roll_id, 0, 1, 'SYSTEM'); -- Creados e impresos, pero no usados aún
            
            SET @qr_code_counter = @qr_code_counter + 1;
        END
        
        PRINT N'Creados ' + CAST(@qr_code_count AS NVARCHAR(10)) + N' QR codes para roll ' + CAST(@qr_roll_id AS NVARCHAR(10));
        
        -- =============================================
        -- 5. CREAR HARVEST RECORDS CON PRODUCTIVIDAD RAZONABLE
        -- =============================================
        
        -- Calcular productividad variada pero razonable:
        -- - Algunos empleados cumplen el mínimo (>= 100%)
        -- - Otros no lo cumplen (< 100%)
        -- - Productividad entre 80% y 150% del mínimo
        -- Usar el min_task_requirement de la labor específica de este tareo
        
        DECLARE @productivity_multiplier DECIMAL(5,2) = 0.80 + ((@productivity_counter % 8) * 0.10); -- Entre 0.80 y 1.50
        DECLARE @harvest_count INT = CAST(@min_task_requirement * @productivity_multiplier AS INT);
        
        -- Asegurar que haya al menos algunos registros (mínimo 20)
        IF @harvest_count < 20
            SET @harvest_count = 20;
        
        -- Asegurar que no exceda los QR codes disponibles
        IF @harvest_count > @qr_code_count
            SET @harvest_count = @qr_code_count;
        
        -- Obtener los QR codes de este roll (los primeros @harvest_count)
        DECLARE harvest_cursor CURSOR FOR
        SELECT TOP (@harvest_count) id, public_id
        FROM app.tbl_qr_codes
        WHERE qr_roll_id = @qr_roll_id
          AND deleted_at IS NULL
        ORDER BY id;
        
        DECLARE @qr_code_id BIGINT;
        DECLARE @qr_code_public_id UNIQUEIDENTIFIER;
        DECLARE @scanned_at DATETIME2;
        DECLARE @hour_offset INT;
        
        OPEN harvest_cursor;
        FETCH NEXT FROM harvest_cursor INTO @qr_code_id, @qr_code_public_id;
        
        SET @hour_offset = 0;
        
        WHILE @@FETCH_STATUS = 0
        BEGIN
            -- Distribuir los escaneos a lo largo del día (de 6:00 AM a 2:00 PM)
            -- Cada registro se escanea aproximadamente cada 12 minutos (8 horas = 480 minutos / 40 registros = 12 min)
            SET @scanned_at = DATEADD(MINUTE, 360 + (@hour_offset * 12), @tareo_date); -- Empezar a las 6:00 AM
            
            -- Crear harvest record
            INSERT INTO app.tbl_harvest_records (qr_code_id, scanned_at, created_by)
            VALUES (@qr_code_id, @scanned_at, 'SYSTEM');
            
            -- Marcar QR code como usado
            UPDATE app.tbl_qr_codes
            SET is_used = 1, updated_at = GETUTCDATE(), updated_by = 'SYSTEM'
            WHERE id = @qr_code_id;
            
            SET @hour_offset = @hour_offset + 1;
            FETCH NEXT FROM harvest_cursor INTO @qr_code_id, @qr_code_public_id;
        END
        
        CLOSE harvest_cursor;
        DEALLOCATE harvest_cursor;
        
        -- Actualizar productividad en tareo_employee (si el campo existe)
        -- Esto asegura que la productividad precalculada coincida con los harvest_records
        IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_tareo_employees') AND name = 'productivity')
        BEGIN
            UPDATE app.tbl_tareo_employees
            SET productivity = @harvest_count, updated_at = GETUTCDATE(), updated_by = 'SYSTEM'
            WHERE id = @tareo_employee_id;
        END
        
        -- Marcar tareo como cerrado (si el campo existe)
        IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_tareos') AND name = 'closed_at')
        BEGIN
            UPDATE app.tbl_tareos
            SET closed_at = DATEADD(HOUR, 8, @tareo_date), updated_at = GETUTCDATE(), updated_by = 'SYSTEM'
            WHERE id = @tareo_id AND closed_at IS NULL;
        END
        
        DECLARE @productivity_pct DECIMAL(5,2) = CAST(@harvest_count AS DECIMAL(10,2)) / @min_task_requirement * 100;
        PRINT N'Creados ' + CAST(@harvest_count AS NVARCHAR(10)) + N' harvest records para empleado ' + @employee_doc + 
              N' en tareo ' + @temporal_id + 
              N' (Labor ID: ' + CAST(@labor_id AS NVARCHAR(10)) + 
              N', Min: ' + CAST(@min_task_requirement AS NVARCHAR(10)) + 
              N', Productividad: ' + CAST(@productivity_pct AS NVARCHAR(10)) + N'%)';
    END
    ELSE
    BEGIN
        PRINT N'Ya existe QR roll para empleado ' + @employee_doc + N' en fecha ' + CAST(@assigned_date AS NVARCHAR(10)) + N'. Saltando.';
    END
    
    FETCH NEXT FROM tareo_cursor INTO @tareo_id, @temporal_id, @tareo_date, @tareo_employee_id, @employee_doc, @tareo_employee_public_id, @labor_id, @min_task_requirement;
END

CLOSE tareo_cursor;
DEALLOCATE tareo_cursor;

-- =============================================
-- 3. CREAR PROCEDIMIENTO ALMACENADO AUXILIAR
-- =============================================

-- Crear procedimiento almacenado auxiliar para crear harvest records
-- Este procedimiento crea QR roll, QR codes y harvest records para un tareo_employee
IF OBJECT_ID('app.CreateHarvestRecordsForTareoEmployee', 'P') IS NOT NULL
    DROP PROCEDURE app.CreateHarvestRecordsForTareoEmployee;
GO

CREATE PROCEDURE app.CreateHarvestRecordsForTareoEmployee
    @tareo_employee_id BIGINT,
    @employee_doc NVARCHAR(15),
    @tareo_date DATETIME2,
    @labor_id SMALLINT,
    @min_task_requirement DECIMAL(10,2),
    @productivity_counter INT
AS
BEGIN
    DECLARE @assigned_date DATE = CAST(@tareo_date AS DATE);
    DECLARE @qr_roll_id INT;
    DECLARE @qr_roll_public_id UNIQUEIDENTIFIER;
    DECLARE @qr_roll_employee_id BIGINT;
    
    -- Verificar si ya existe un qr_roll_employee para este empleado en esta fecha
    IF NOT EXISTS (
        SELECT 1 FROM app.tbl_qr_roll_employees qre
        WHERE qre.employee_document_number = @employee_doc
          AND qre.assigned_date = @assigned_date
          AND qre.deleted_at IS NULL
    )
    BEGIN
        -- Crear nuevo QR Roll
        INSERT INTO app.tbl_qr_rolls (max_qr_codes_per_day, created_by)
        VALUES (50, 'SYSTEM');
        
        SET @qr_roll_id = SCOPE_IDENTITY();
        SELECT @qr_roll_public_id = public_id FROM app.tbl_qr_rolls WHERE id = @qr_roll_id;
        
        -- Asignar QR Roll al empleado en esta fecha
        INSERT INTO app.tbl_qr_roll_employees (qr_roll_id, employee_document_number, assigned_date, created_by)
        VALUES (@qr_roll_id, @employee_doc, @assigned_date, 'SYSTEM');
        
        SET @qr_roll_employee_id = SCOPE_IDENTITY();
        
        -- Crear 40-50 QR codes por roll
        DECLARE @qr_code_count INT = 40 + (@productivity_counter % 11); -- Entre 40 y 50 códigos
        DECLARE @qr_code_counter INT = 0;
        
        WHILE @qr_code_counter < @qr_code_count
        BEGIN
            INSERT INTO app.tbl_qr_codes (qr_roll_id, is_used, is_printed, created_by)
            VALUES (@qr_roll_id, 0, 1, 'SYSTEM');
            
            SET @qr_code_counter = @qr_code_counter + 1;
        END
        
        -- Calcular productividad variada pero razonable
        DECLARE @productivity_multiplier DECIMAL(5,2) = 0.80 + ((@productivity_counter % 8) * 0.10); -- Entre 0.80 y 1.50
        DECLARE @harvest_count INT = CAST(@min_task_requirement * @productivity_multiplier AS INT);
        
        -- Asegurar que haya al menos algunos registros (mínimo 20)
        IF @harvest_count < 20
            SET @harvest_count = 20;
        
        -- Asegurar que no exceda los QR codes disponibles
        IF @harvest_count > @qr_code_count
            SET @harvest_count = @qr_code_count;
        
        -- Obtener los QR codes de este roll (los primeros @harvest_count)
        DECLARE @qr_code_id BIGINT;
        DECLARE @scanned_at DATETIME2;
        DECLARE @hour_offset INT = 0;
        
        DECLARE harvest_cursor CURSOR FOR
        SELECT TOP (@harvest_count) id
        FROM app.tbl_qr_codes
        WHERE qr_roll_id = @qr_roll_id
          AND deleted_at IS NULL
        ORDER BY id;
        
        OPEN harvest_cursor;
        FETCH NEXT FROM harvest_cursor INTO @qr_code_id;
        
        WHILE @@FETCH_STATUS = 0
        BEGIN
            -- Distribuir los escaneos a lo largo del día (de 6:00 AM a 2:00 PM)
            SET @scanned_at = DATEADD(MINUTE, 360 + (@hour_offset * 12), @tareo_date);
            
            -- Crear harvest record
            INSERT INTO app.tbl_harvest_records (qr_code_id, scanned_at, created_by)
            VALUES (@qr_code_id, @scanned_at, 'SYSTEM');
            
            -- Marcar QR code como usado
            UPDATE app.tbl_qr_codes
            SET is_used = 1, updated_at = GETUTCDATE(), updated_by = 'SYSTEM'
            WHERE id = @qr_code_id;
            
            SET @hour_offset = @hour_offset + 1;
            FETCH NEXT FROM harvest_cursor INTO @qr_code_id;
        END
        
        CLOSE harvest_cursor;
        DEALLOCATE harvest_cursor;
        
        -- Actualizar productividad en tareo_employee
        IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_tareo_employees') AND name = 'productivity')
        BEGIN
            UPDATE app.tbl_tareo_employees
            SET productivity = @harvest_count, updated_at = GETUTCDATE(), updated_by = 'SYSTEM'
            WHERE id = @tareo_employee_id;
        END
    END
END;
GO

-- =============================================
-- 4. CREAR NUEVOS TAREOS DE DESTAJO CON HARVEST RECORDS
-- =============================================

PRINT N'Creando nuevos tareos de destajo con harvest records...';

-- Obtener IDs de labores de destajo
DECLARE @labor_cosecha_uva_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Cosecha de Uva' AND deleted_at IS NULL);
DECLARE @labor_cosecha_palta_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Cosecha de Palta' AND deleted_at IS NULL);
DECLARE @labor_cosecha_mandarina_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Cosecha de Mandarina' AND deleted_at IS NULL);
DECLARE @labor_cosecha_arandano_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Cosecha de Arandano' AND deleted_at IS NULL);

-- Obtener IDs de lotes
DECLARE @lote_a_id INT = (SELECT id FROM app.tbl_lotes WHERE name = 'LOTE A' AND deleted_at IS NULL);
DECLARE @lote_b_id INT = (SELECT id FROM app.tbl_lotes WHERE name = 'LOTE B' AND deleted_at IS NULL);

-- Obtener supervisor (usar un empleado existente como supervisor)
DECLARE @supervisor_doc NVARCHAR(15) = '23456789'; -- María López

-- Empleados para tareos de destajo (operarios de campo)
DECLARE @empleado1_doc NVARCHAR(15) = '12345678';
DECLARE @empleado2_doc NVARCHAR(15) = '34567890';
DECLARE @empleado3_doc NVARCHAR(15) = '45678901';
DECLARE @empleado4_doc NVARCHAR(15) = '56789012';
DECLARE @empleado5_doc NVARCHAR(15) = '78901234';

-- Contador para variar productividad
DECLARE @new_tareo_counter INT = 0;

-- =============================================
-- Crear tareos de destajo en NOVIEMBRE 2025
-- =============================================

-- Tareo 1: Cosecha de Uva - 2025-11-20
IF @labor_cosecha_uva_id IS NOT NULL AND @lote_a_id IS NOT NULL AND @supervisor_doc IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-DESTAJO-2025-11-20-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-DESTAJO-2025-11-20-001', @supervisor_doc, @labor_cosecha_uva_id, @lote_a_id, @supervisor_doc, '2025-11-20 06:00:00');
        
        DECLARE @tareo_new1_id INT = SCOPE_IDENTITY();
        DECLARE @tareo_new1_date DATETIME2 = '2025-11-20 06:00:00';
        DECLARE @min_task_uva DECIMAL(10,2) = (SELECT min_task_requirement FROM app.tbl_labors WHERE id = @labor_cosecha_uva_id);
        
        -- Empleado 1
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours, created_at)
        VALUES (@tareo_new1_id, @empleado1_doc, '06:00:00', '14:00:00', 8.00, 8.00, @tareo_new1_date);
        DECLARE @te_new1_1_id BIGINT = SCOPE_IDENTITY();
        
        SET @new_tareo_counter = @new_tareo_counter + 1;
        EXEC app.CreateHarvestRecordsForTareoEmployee @te_new1_1_id, @empleado1_doc, @tareo_new1_date, @labor_cosecha_uva_id, @min_task_uva, @new_tareo_counter;
        
        -- Empleado 2
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours, created_at)
        VALUES (@tareo_new1_id, @empleado2_doc, '06:00:00', '14:00:00', 8.00, 8.00, @tareo_new1_date);
        DECLARE @te_new1_2_id BIGINT = SCOPE_IDENTITY();
        
        SET @new_tareo_counter = @new_tareo_counter + 1;
        EXEC app.CreateHarvestRecordsForTareoEmployee @te_new1_2_id, @empleado2_doc, @tareo_new1_date, @labor_cosecha_uva_id, @min_task_uva, @new_tareo_counter;
        
        -- Empleado 3
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours, created_at)
        VALUES (@tareo_new1_id, @empleado3_doc, '06:00:00', '14:00:00', 8.00, 8.00, @tareo_new1_date);
        DECLARE @te_new1_3_id BIGINT = SCOPE_IDENTITY();
        
        SET @new_tareo_counter = @new_tareo_counter + 1;
        EXEC app.CreateHarvestRecordsForTareoEmployee @te_new1_3_id, @empleado3_doc, @tareo_new1_date, @labor_cosecha_uva_id, @min_task_uva, @new_tareo_counter;
        
        -- Cerrar tareo
        UPDATE app.tbl_tareos SET closed_at = DATEADD(HOUR, 8, @tareo_new1_date), updated_at = GETUTCDATE(), updated_by = 'SYSTEM'
        WHERE id = @tareo_new1_id;
        
        PRINT N'Tareo de destajo creado: Cosecha de Uva - 2025-11-20 (ID: ' + CAST(@tareo_new1_id AS NVARCHAR(10)) + N')';
    END
END

-- Tareo 2: Cosecha de Palta - 2025-11-21
IF @labor_cosecha_palta_id IS NOT NULL AND @lote_b_id IS NOT NULL AND @supervisor_doc IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-DESTAJO-2025-11-21-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-DESTAJO-2025-11-21-001', @supervisor_doc, @labor_cosecha_palta_id, @lote_b_id, @supervisor_doc, '2025-11-21 06:00:00');
        
        DECLARE @tareo_new2_id INT = SCOPE_IDENTITY();
        DECLARE @tareo_new2_date DATETIME2 = '2025-11-21 06:00:00';
        DECLARE @min_task_palta DECIMAL(10,2) = (SELECT min_task_requirement FROM app.tbl_labors WHERE id = @labor_cosecha_palta_id);
        
        -- Empleado 4
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours, created_at)
        VALUES (@tareo_new2_id, @empleado4_doc, '06:00:00', '14:00:00', 8.00, 8.00, @tareo_new2_date);
        DECLARE @te_new2_1_id BIGINT = SCOPE_IDENTITY();
        
        SET @new_tareo_counter = @new_tareo_counter + 1;
        EXEC app.CreateHarvestRecordsForTareoEmployee @te_new2_1_id, @empleado4_doc, @tareo_new2_date, @labor_cosecha_palta_id, @min_task_palta, @new_tareo_counter;
        
        -- Empleado 5
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours, created_at)
        VALUES (@tareo_new2_id, @empleado5_doc, '06:00:00', '14:00:00', 8.00, 8.00, @tareo_new2_date);
        DECLARE @te_new2_2_id BIGINT = SCOPE_IDENTITY();
        
        SET @new_tareo_counter = @new_tareo_counter + 1;
        EXEC app.CreateHarvestRecordsForTareoEmployee @te_new2_2_id, @empleado5_doc, @tareo_new2_date, @labor_cosecha_palta_id, @min_task_palta, @new_tareo_counter;
        
        -- Cerrar tareo
        UPDATE app.tbl_tareos SET closed_at = DATEADD(HOUR, 8, @tareo_new2_date), updated_at = GETUTCDATE(), updated_by = 'SYSTEM'
        WHERE id = @tareo_new2_id;
        
        PRINT N'Tareo de destajo creado: Cosecha de Palta - 2025-11-21 (ID: ' + CAST(@tareo_new2_id AS NVARCHAR(10)) + N')';
    END
END

-- Tareo 3: Cosecha de Mandarina - 2025-11-22
IF @labor_cosecha_mandarina_id IS NOT NULL AND @lote_a_id IS NOT NULL AND @supervisor_doc IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-DESTAJO-2025-11-22-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-DESTAJO-2025-11-22-001', @supervisor_doc, @labor_cosecha_mandarina_id, @lote_a_id, @supervisor_doc, '2025-11-22 06:00:00');
        
        DECLARE @tareo_new3_id INT = SCOPE_IDENTITY();
        DECLARE @tareo_new3_date DATETIME2 = '2025-11-22 06:00:00';
        DECLARE @min_task_mandarina DECIMAL(10,2) = (SELECT min_task_requirement FROM app.tbl_labors WHERE id = @labor_cosecha_mandarina_id);
        
        -- Empleado 1
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours, created_at)
        VALUES (@tareo_new3_id, @empleado1_doc, '06:00:00', '14:00:00', 8.00, 8.00, @tareo_new3_date);
        DECLARE @te_new3_1_id BIGINT = SCOPE_IDENTITY();
        
        SET @new_tareo_counter = @new_tareo_counter + 1;
        EXEC app.CreateHarvestRecordsForTareoEmployee @te_new3_1_id, @empleado1_doc, @tareo_new3_date, @labor_cosecha_mandarina_id, @min_task_mandarina, @new_tareo_counter;
        
        -- Empleado 3
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours, created_at)
        VALUES (@tareo_new3_id, @empleado3_doc, '06:00:00', '14:00:00', 8.00, 8.00, @tareo_new3_date);
        DECLARE @te_new3_2_id BIGINT = SCOPE_IDENTITY();
        
        SET @new_tareo_counter = @new_tareo_counter + 1;
        EXEC app.CreateHarvestRecordsForTareoEmployee @te_new3_2_id, @empleado3_doc, @tareo_new3_date, @labor_cosecha_mandarina_id, @min_task_mandarina, @new_tareo_counter;
        
        -- Cerrar tareo
        UPDATE app.tbl_tareos SET closed_at = DATEADD(HOUR, 8, @tareo_new3_date), updated_at = GETUTCDATE(), updated_by = 'SYSTEM'
        WHERE id = @tareo_new3_id;
        
        PRINT N'Tareo de destajo creado: Cosecha de Mandarina - 2025-11-22 (ID: ' + CAST(@tareo_new3_id AS NVARCHAR(10)) + N')';
    END
END

-- Tareo 4: Cosecha de Arandano - 2025-11-25
IF @labor_cosecha_arandano_id IS NOT NULL AND @lote_b_id IS NOT NULL AND @supervisor_doc IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-DESTAJO-2025-11-25-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-DESTAJO-2025-11-25-001', @supervisor_doc, @labor_cosecha_arandano_id, @lote_b_id, @supervisor_doc, '2025-11-25 06:00:00');
        
        DECLARE @tareo_new4_id INT = SCOPE_IDENTITY();
        DECLARE @tareo_new4_date DATETIME2 = '2025-11-25 06:00:00';
        DECLARE @min_task_arandano DECIMAL(10,2) = (SELECT min_task_requirement FROM app.tbl_labors WHERE id = @labor_cosecha_arandano_id);
        
        -- Empleado 2
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours, created_at)
        VALUES (@tareo_new4_id, @empleado2_doc, '06:00:00', '14:00:00', 8.00, 8.00, @tareo_new4_date);
        DECLARE @te_new4_1_id BIGINT = SCOPE_IDENTITY();
        
        SET @new_tareo_counter = @new_tareo_counter + 1;
        EXEC app.CreateHarvestRecordsForTareoEmployee @te_new4_1_id, @empleado2_doc, @tareo_new4_date, @labor_cosecha_arandano_id, @min_task_arandano, @new_tareo_counter;
        
        -- Empleado 4
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours, created_at)
        VALUES (@tareo_new4_id, @empleado4_doc, '06:00:00', '14:00:00', 8.00, 8.00, @tareo_new4_date);
        DECLARE @te_new4_2_id BIGINT = SCOPE_IDENTITY();
        
        SET @new_tareo_counter = @new_tareo_counter + 1;
        EXEC app.CreateHarvestRecordsForTareoEmployee @te_new4_2_id, @empleado4_doc, @tareo_new4_date, @labor_cosecha_arandano_id, @min_task_arandano, @new_tareo_counter;
        
        -- Empleado 5
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours, created_at)
        VALUES (@tareo_new4_id, @empleado5_doc, '06:00:00', '14:00:00', 8.00, 8.00, @tareo_new4_date);
        DECLARE @te_new4_3_id BIGINT = SCOPE_IDENTITY();
        
        SET @new_tareo_counter = @new_tareo_counter + 1;
        EXEC app.CreateHarvestRecordsForTareoEmployee @te_new4_3_id, @empleado5_doc, @tareo_new4_date, @labor_cosecha_arandano_id, @min_task_arandano, @new_tareo_counter;
        
        -- Cerrar tareo
        UPDATE app.tbl_tareos SET closed_at = DATEADD(HOUR, 8, @tareo_new4_date), updated_at = GETUTCDATE(), updated_by = 'SYSTEM'
        WHERE id = @tareo_new4_id;
        
        PRINT N'Tareo de destajo creado: Cosecha de Arandano - 2025-11-25 (ID: ' + CAST(@tareo_new4_id AS NVARCHAR(10)) + N')';
    END
END

EndMigration:
PRINT N'Migración completada: QR rolls, QR codes y harvest records creados para tareos de destajo en octubre y noviembre 2025.';
PRINT N'NOTA: Los tareos administrativos (is_piecework = 0) mantienen productividad NULL, lo cual es correcto.';
PRINT N'Se crearon nuevos tareos de destajo con harvest records completos.';
GO

