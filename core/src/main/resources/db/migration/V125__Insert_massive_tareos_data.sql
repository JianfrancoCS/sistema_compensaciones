-- =============================================
-- V125: INSERTAR DATOS MASIVOS DE TAREOS
-- =============================================
-- Crea un caso real con:
-- - 50 trabajadores (8 existentes + 42 nuevos)
-- - Al menos 1000 registros de tareo_employees
-- - Tareos distribuidos desde Agosto 2025 hasta el día actual
-- - Compatible con la creación de períodos basados en configuración de empresa
-- - Diferentes labores, lotes y horarios realistas

-- =============================================
-- 1. CREAR 42 EMPLEADOS ADICIONALES
-- =============================================

DECLARE @fundo1_id SMALLINT = (SELECT id FROM app.tbl_subsidiaries WHERE name = 'FUNDO 1' AND deleted_at IS NULL);
DECLARE @fundo_santa_rosa_id SMALLINT = (SELECT id FROM app.tbl_subsidiaries WHERE name = 'FUNDO SANTA ROSA' AND deleted_at IS NULL);
DECLARE @fundo_esperanza_id SMALLINT = (SELECT id FROM app.tbl_subsidiaries WHERE name = 'FUNDO LA ESPERANZA' AND deleted_at IS NULL);
DECLARE @position_operario_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Operario de Campo' AND deleted_at IS NULL);
DECLARE @position_supervisor_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Supervisor de Campo' AND deleted_at IS NULL);
DECLARE @employee_state_id SMALLINT = (SELECT TOP 1 id FROM app.tbl_states WHERE domain_id = (SELECT id FROM app.tbl_domains WHERE name = 'tbl_employees') AND is_default = 1);

-- Obtener IDs de conceptos de jubilación para distribuir entre empleados (una sola vez, fuera del loop)
DECLARE @afp_integra_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'AFP_INTEGRA' AND deleted_at IS NULL);
DECLARE @afp_prima_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'AFP_PRIMA' AND deleted_at IS NULL);
DECLARE @afp_profuturo_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'AFP_PROFUTURO' AND deleted_at IS NULL);
DECLARE @afp_habitat_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'AFP_HABITAT' AND deleted_at IS NULL);
DECLARE @onp_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'ONP' AND deleted_at IS NULL);

-- Nombres y apellidos para generar empleados realistas
DECLARE @nombres TABLE (nombre NVARCHAR(50));
INSERT INTO @nombres VALUES ('MIGUEL'), ('ROBERTO'), ('FERNANDO'), ('JORGE'), ('RICARDO'), ('MANUEL'), ('ALBERTO'), ('MARIO'), ('VICTOR'), ('EDUARDO'),
                            ('JULIO'), ('ANTONIO'), ('FRANCISCO'), ('ALEXANDER'), ('DIEGO'), ('JESUS'), ('RAUL'), ('SERGIO'), ('MARTIN'), ('CESAR'),
                            ('CARMEN'), ('ELENA'), ('PATRICIA'), ('GLORIA'), ('MARTHA'), ('SUSANA'), ('NANCY'), ('LILIANA'), ('CLAUDIA'), ('VERONICA'),
                            ('JULIA'), ('MIRIAM'), ('ESTHER'), ('DORIS'), ('MARITZA'), ('YOLANDA'), ('TERESA'), ('MAGDALENA'), ('ROSA'), ('MARIA');

DECLARE @apellidos_paterno TABLE (apellido NVARCHAR(50));
INSERT INTO @apellidos_paterno VALUES ('GONZALEZ'), ('RODRIGUEZ'), ('FERNANDEZ'), ('LOPEZ'), ('MARTINEZ'), ('GARCIA'), ('PEREZ'), ('SANCHEZ'), ('RAMIREZ'), ('TORRES'),
                                      ('FLORES'), ('RIVERA'), ('GOMEZ'), ('DIAZ'), ('CRUZ'), ('MORALES'), ('ORTEGA'), ('JIMENEZ'), ('VARGAS'), ('CASTRO'),
                                      ('ROMERO'), ('HERRERA'), ('MENDOZA'), ('SILVA'), ('RUIZ'), ('ALVAREZ'), ('MORENO'), ('DELGADO'), ('GUERRERO'), ('RAMOS');

DECLARE @apellidos_materno TABLE (apellido NVARCHAR(50));
INSERT INTO @apellidos_materno VALUES ('VEGA'), ('CAMPOS'), ('SALAZAR'), ('VALDEZ'), ('AGUILAR'), ('MEDINA'), ('CORTEZ'), ('NAVARRO'), ('LEON'), ('MOLINA'),
                                      ('CARRASCO'), ('FUENTES'), ('PALACIOS'), ('SOTO'), ('CONTRERAS'), ('VALENCIA'), ('FIGUEROA'), ('CARDENAS'), ('AVILA'), ('PEÑA'),
                                      ('CAMPOS'), ('SANDOVAL'), ('GUZMAN'), ('HERNANDEZ'), ('MUNOZ'), ('CASTILLO'), ('JARA'), ('QUIROZ'), ('ESPINOZA'), ('ACOSTA');

-- Crear 42 empleados adicionales
DECLARE @doc_base INT = 90000000; -- Empezar desde 90000000
DECLARE @counter INT = 1;
DECLARE @doc_number NVARCHAR(15);
DECLARE @nombre NVARCHAR(50);
DECLARE @apellido_p NVARCHAR(50);
DECLARE @apellido_m NVARCHAR(50);
DECLARE @subsidiary_id SMALLINT;
DECLARE @gender CHAR(1);
DECLARE @dob DATE;

WHILE @counter <= 42
BEGIN
    SET @doc_number = CAST(@doc_base + @counter AS NVARCHAR(15));
    
    -- Seleccionar nombre y apellidos aleatorios
    SELECT TOP 1 @nombre = nombre FROM @nombres ORDER BY NEWID();
    SELECT TOP 1 @apellido_p = apellido FROM @apellidos_paterno ORDER BY NEWID();
    SELECT TOP 1 @apellido_m = apellido FROM @apellidos_materno ORDER BY NEWID();
    
    -- Determinar género basado en el nombre
    IF @nombre IN ('CARMEN', 'ELENA', 'PATRICIA', 'GLORIA', 'MARTHA', 'SUSANA', 'NANCY', 'LILIANA', 'CLAUDIA', 'VERONICA', 'JULIA', 'MIRIAM', 'ESTHER', 'DORIS', 'MARITZA', 'YOLANDA', 'TERESA', 'MAGDALENA', 'ROSA', 'MARIA')
        SET @gender = 'F'
    ELSE
        SET @gender = 'M'
    
    -- Fecha de nacimiento aleatoria entre 1985 y 2000 (25-40 años)
    DECLARE @anos_edad INT = 25 + CAST(RAND(CHECKSUM(NEWID())) * 15 AS INT); -- Entre 25 y 40 años
    DECLARE @dias_aleatorios INT = CAST(RAND(CHECKSUM(NEWID())) * 365 AS INT);
    SET @dob = DATEADD(DAY, -@dias_aleatorios, DATEADD(YEAR, -@anos_edad, GETDATE()));
    
    -- Asignar subsidiaria (distribuir entre los 3 fundos)
    IF @counter % 3 = 0
        SET @subsidiary_id = @fundo1_id
    ELSE IF @counter % 3 = 1
        SET @subsidiary_id = @fundo_santa_rosa_id
    ELSE
        SET @subsidiary_id = @fundo_esperanza_id
    
    -- Crear persona si no existe
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = @doc_number)
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES (@doc_number, 1, @nombre, @apellido_p, @apellido_m, @dob, @gender);
    END
    
    -- Crear empleado si no existe
    IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = @doc_number)
    BEGIN
        DECLARE @afp_number NVARCHAR(20) = CAST(CAST(RAND(CHECKSUM(NEWID())) * 999999 AS INT) AS NVARCHAR(6)) + 'ABCDEF' + RIGHT('00' + CAST(@counter AS NVARCHAR(2)), 2);
        DECLARE @bank_account NVARCHAR(30) = RIGHT('000' + CAST(@counter AS NVARCHAR(3)), 3) + '-' + CAST(CAST(RAND(CHECKSUM(NEWID())) * 99999999 AS BIGINT) AS NVARCHAR(8)) + '-0-' + RIGHT('00' + CAST((@counter % 10) AS NVARCHAR(2)), 2);
        DECLARE @bank_name NVARCHAR(50) = CASE (@counter % 5)
            WHEN 0 THEN 'BANCO DE CREDITO DEL PERU'
            WHEN 1 THEN 'BANCO DE LA NACION'
            WHEN 2 THEN 'BBVA'
            WHEN 3 THEN 'INTERBANK'
            ELSE 'SCOTIABANK'
        END;
        DECLARE @hire_date DATE = DATEADD(DAY, -RAND(CHECKSUM(NEWID())) * 730, GETDATE()); -- Últimos 2 años
        
        -- Distribuir conceptos de jubilación de manera equitativa usando el módulo del counter
        DECLARE @retirement_concept_id SMALLINT = CASE (@counter % 5)
            WHEN 0 THEN @afp_integra_id
            WHEN 1 THEN @afp_prima_id
            WHEN 2 THEN @afp_profuturo_id
            WHEN 3 THEN @afp_habitat_id
            ELSE @onp_id
        END;
        
        INSERT INTO app.tbl_employees (
            person_document_number, subsidiary_id, position_id, state_id,
            afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
        )
        VALUES (
            @doc_number, @subsidiary_id, @position_operario_id, @employee_state_id,
            @afp_number, @bank_account, @bank_name, @hire_date, 50.00, 1500.00, @retirement_concept_id
        );
    END
    
    SET @counter = @counter + 1;
END
GO

-- =============================================
-- 2. CREAR TAREOS DESDE AGOSTO 2025 HASTA HOY
-- =============================================

DECLARE @supervisor_doc NVARCHAR(15) = '23456789'; -- María López es supervisora
DECLARE @labor_cosecha_uva_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Cosecha de Uva' AND deleted_at IS NULL);
DECLARE @labor_cosecha_palta_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Cosecha de Palta' AND deleted_at IS NULL);
DECLARE @labor_cosecha_arandano_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Cosecha de Arandano' AND deleted_at IS NULL);
DECLARE @labor_limpieza_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Limpieza de Terreno' AND deleted_at IS NULL);
DECLARE @labor_riego_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Riego' AND deleted_at IS NULL);
DECLARE @labor_poda_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Poda' AND deleted_at IS NULL);

DECLARE @lote_a_id INT = (SELECT id FROM app.tbl_lotes WHERE name = 'LOTE A' AND deleted_at IS NULL);
DECLARE @lote_b_id INT = (SELECT id FROM app.tbl_lotes WHERE name = 'LOTE B' AND deleted_at IS NULL);
DECLARE @lote_c_id INT = (SELECT id FROM app.tbl_lotes WHERE name = 'LOTE C' AND deleted_at IS NULL);
DECLARE @lote_1_id INT = (SELECT id FROM app.tbl_lotes WHERE name = 'LOTE 1' AND deleted_at IS NULL);
DECLARE @lote_2_id INT = (SELECT id FROM app.tbl_lotes WHERE name = 'LOTE 2' AND deleted_at IS NULL);

-- Obtener lista de empleados (todos los operarios, excluyendo supervisores y jefes)
DECLARE @empleados TABLE (doc_number NVARCHAR(15), subsidiary_id SMALLINT);
INSERT INTO @empleados (doc_number, subsidiary_id)
SELECT e.person_document_number, e.subsidiary_id
FROM app.tbl_employees e
INNER JOIN app.tbl_positions p ON e.position_id = p.id
WHERE p.name = 'Operario de Campo'
  AND e.deleted_at IS NULL
  AND p.deleted_at IS NULL;

-- Crear tabla temporal para planificar tareos
CREATE TABLE #tareos_plan (
    fecha DATE,
    tareo_num INT,
    labor_id SMALLINT,
    lote_id INT,
    lote_subsidiary_id SMALLINT,
    empleados_por_tareo INT
);

-- Generar plan de tareos desde Agosto 2025 hasta el 30 de Noviembre 2025
-- Fecha de inicio: 1 de Agosto 2025
-- Fecha de fin: 30 de Noviembre 2025 (no incluir Diciembre)
DECLARE @fecha_inicio DATE = DATEFROMPARTS(2025, 8, 1); -- 1 de Agosto 2025
DECLARE @fecha_fin DATE = DATEFROMPARTS(2025, 11, 30); -- 30 de Noviembre 2025

DECLARE @fecha DATE = @fecha_inicio;
DECLARE @tareo_dia INT;

WHILE @fecha <= @fecha_fin
BEGIN
    -- Verificar si el día es laborable según el calendario laboral
    DECLARE @es_dia_laborable BIT = 1;
    SELECT @es_dia_laborable = is_working_day
    FROM app.tbl_work_calendar
    WHERE date = @fecha AND deleted_at IS NULL;
    
    -- Si no existe en el calendario, considerar domingos como no laborables
    IF @es_dia_laborable IS NULL
    BEGIN
        -- Si es domingo, no es laborable
        IF DATEPART(WEEKDAY, @fecha) = 1
            SET @es_dia_laborable = 0
        ELSE
            SET @es_dia_laborable = 1
    END
    
    -- Solo crear tareos en días laborables
    IF @es_dia_laborable = 1
    BEGIN
        -- Crear entre 1 y 3 tareos por día
        DECLARE @tareos_por_dia INT = CASE 
            WHEN DATEPART(WEEKDAY, @fecha) IN (2, 3, 4, 5) THEN 3  -- Lunes a Jueves: 3 tareos
            WHEN DATEPART(WEEKDAY, @fecha) = 6 THEN 2              -- Viernes: 2 tareos
            ELSE 1                                                  -- Sábado: 1 tareo
        END;
        
        SET @tareo_dia = 1;
        
        WHILE @tareo_dia <= @tareos_por_dia
        BEGIN
            -- Seleccionar labor aleatoria de las que existen
            DECLARE @labor_id SMALLINT = NULL;
            DECLARE @rand_labor INT = CAST(RAND(CHECKSUM(NEWID())) * 6 AS INT) + 1;
            
            -- Intentar seleccionar labor según el número aleatorio, pero solo si existe
            IF @rand_labor = 1 AND @labor_cosecha_uva_id IS NOT NULL
                SET @labor_id = @labor_cosecha_uva_id
            ELSE IF @rand_labor = 2 AND @labor_cosecha_palta_id IS NOT NULL
                SET @labor_id = @labor_cosecha_palta_id
            ELSE IF @rand_labor = 3 AND @labor_cosecha_arandano_id IS NOT NULL
                SET @labor_id = @labor_cosecha_arandano_id
            ELSE IF @rand_labor = 4 AND @labor_limpieza_id IS NOT NULL
                SET @labor_id = @labor_limpieza_id
            ELSE IF @rand_labor = 5 AND @labor_riego_id IS NOT NULL
                SET @labor_id = @labor_riego_id
            ELSE IF @labor_poda_id IS NOT NULL
                SET @labor_id = @labor_poda_id
            
            -- Si ninguna labor existe, usar la primera disponible
            IF @labor_id IS NULL
            BEGIN
                SELECT TOP 1 @labor_id = id FROM app.tbl_labors WHERE deleted_at IS NULL ORDER BY id;
            END
            
            -- Seleccionar lote aleatorio de los que existen
            DECLARE @lote_id INT = NULL;
            DECLARE @rand_lote INT = CAST(RAND(CHECKSUM(NEWID())) * 5 AS INT) + 1;
            
            IF @rand_lote = 1 AND @lote_a_id IS NOT NULL
                SET @lote_id = @lote_a_id
            ELSE IF @rand_lote = 2 AND @lote_b_id IS NOT NULL
                SET @lote_id = @lote_b_id
            ELSE IF @rand_lote = 3 AND @lote_c_id IS NOT NULL
                SET @lote_id = @lote_c_id
            ELSE IF @rand_lote = 4 AND @lote_1_id IS NOT NULL
                SET @lote_id = @lote_1_id
            ELSE IF @lote_2_id IS NOT NULL
                SET @lote_id = @lote_2_id
            
            -- Si ningún lote existe, usar el primero disponible
            IF @lote_id IS NULL
            BEGIN
                SELECT TOP 1 @lote_id = id FROM app.tbl_lotes WHERE deleted_at IS NULL ORDER BY id;
            END
            
            -- Solo insertar si tenemos labor y lote válidos
            IF @labor_id IS NOT NULL AND @lote_id IS NOT NULL
            BEGIN
                DECLARE @lote_subsidiary_id SMALLINT = (SELECT subsidiary_id FROM app.tbl_lotes WHERE id = @lote_id);
                DECLARE @empleados_por_tareo INT = 15 + CAST(RAND(CHECKSUM(NEWID())) * 11 AS INT); -- 15-25 empleados
                
                INSERT INTO #tareos_plan (fecha, tareo_num, labor_id, lote_id, lote_subsidiary_id, empleados_por_tareo)
                VALUES (@fecha, @tareo_dia, @labor_id, @lote_id, @lote_subsidiary_id, @empleados_por_tareo);
            END
            
            SET @tareo_dia = @tareo_dia + 1;
        END
    END -- Fin IF @es_dia_laborable = 1
    
    SET @fecha = DATEADD(DAY, 1, @fecha);
END

-- Crear tareos y empleados
DECLARE @total_tareo_employees INT = 0;
DECLARE tareo_cursor CURSOR FOR
SELECT fecha, tareo_num, labor_id, lote_id, lote_subsidiary_id, empleados_por_tareo
FROM #tareos_plan
ORDER BY fecha, tareo_num;

DECLARE @tareo_fecha DATE;
DECLARE @tareo_num INT;
DECLARE @tareo_labor_id SMALLINT;
DECLARE @tareo_lote_id INT;
DECLARE @tareo_lote_subsidiary_id SMALLINT;
DECLARE @tareo_empleados_por_tareo INT;

OPEN tareo_cursor;
FETCH NEXT FROM tareo_cursor INTO @tareo_fecha, @tareo_num, @tareo_labor_id, @tareo_lote_id, @tareo_lote_subsidiary_id, @tareo_empleados_por_tareo;

WHILE @@FETCH_STATUS = 0
BEGIN
    DECLARE @temporal_id NVARCHAR(255) = 'TAREO-' + CAST(YEAR(@tareo_fecha) AS NVARCHAR(4)) + '-' + RIGHT('00' + CAST(MONTH(@tareo_fecha) AS NVARCHAR(2)), 2) + '-' + RIGHT('00' + CAST(DAY(@tareo_fecha) AS NVARCHAR(2)), 2) + '-' + RIGHT('000' + CAST(@tareo_num AS NVARCHAR(3)), 3);
    
    IF NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = @temporal_id AND deleted_at IS NULL)
    BEGIN
        -- Eliminar tabla temporal si existe de una iteración anterior
        IF OBJECT_ID('tempdb..#empleados_tareo') IS NOT NULL
            DROP TABLE #empleados_tareo;
        
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES (@temporal_id, @supervisor_doc, @tareo_labor_id, @tareo_lote_id, @supervisor_doc, @tareo_fecha);
        
        DECLARE @tareo_id INT = SCOPE_IDENTITY();
        
        -- Crear tabla temporal con empleados seleccionados y sus horarios
        CREATE TABLE #empleados_tareo (
            doc_number NVARCHAR(15),
            start_time TIME,
            end_time TIME,
            actual_hours DECIMAL(5,2),
            paid_hours DECIMAL(5,2),
            row_num INT IDENTITY(1,1)
        );
        
        -- Seleccionar empleados aleatorios que NO estén ya en otro tareo (otra labor) ese mismo día
        -- IMPORTANTE: Un empleado solo puede estar en UNA labor por día
        INSERT INTO #empleados_tareo (doc_number)
        SELECT TOP (@tareo_empleados_por_tareo) e.doc_number
        FROM @empleados e
        WHERE e.subsidiary_id = @tareo_lote_subsidiary_id
          -- Excluir empleados que ya están en otro tareo (con diferente labor) ese mismo día
          AND NOT EXISTS (
              SELECT 1 
              FROM app.tbl_tareo_employees te
              INNER JOIN app.tbl_tareos t ON te.tareo_id = t.id
              WHERE te.employee_document_number = e.doc_number
                AND CAST(t.created_at AS DATE) = @tareo_fecha
                AND t.labor_id != @tareo_labor_id  -- Diferente labor
                AND te.deleted_at IS NULL
                AND t.deleted_at IS NULL
          )
        ORDER BY NEWID();
        
        -- Asignar horarios a cada empleado
        DECLARE @emp_counter INT = 1;
        DECLARE @emp_doc NVARCHAR(15);
        DECLARE @emp_start TIME;
        DECLARE @emp_end TIME;
        DECLARE @emp_actual DECIMAL(5,2);
        DECLARE @emp_paid DECIMAL(5,2);
        DECLARE @rand_val INT;
        
        WHILE @emp_counter <= @tareo_empleados_por_tareo
        BEGIN
            SELECT @emp_doc = doc_number FROM #empleados_tareo WHERE row_num = @emp_counter;
            SET @rand_val = CAST(RAND(CHECKSUM(NEWID())) * 100 AS INT);
            
            -- Determinar tipo de jornada
            IF @rand_val < 10  -- 10% con tardanza
            BEGIN
                DECLARE @tardanza DECIMAL(5,2) = 0.5 + (CAST(RAND(CHECKSUM(NEWID())) * 2 AS DECIMAL(5,2)));
                SET @emp_start = DATEADD(MINUTE, CAST(@tardanza * 60 AS INT), CAST('06:00:00' AS TIME));
                SET @emp_end = DATEADD(HOUR, 8, CAST('06:00:00' AS TIME));
                SET @emp_actual = 8.00 - @tardanza;
                SET @emp_paid = @emp_actual;
            END
            ELSE IF @rand_val < 30  -- 20% con 2 horas extras
            BEGIN
                SET @emp_start = '06:00:00';
                SET @emp_end = DATEADD(HOUR, 10, CAST('06:00:00' AS TIME));
                SET @emp_actual = 10.00;
                SET @emp_paid = 10.00;
            END
            ELSE IF @rand_val < 35  -- 5% con 4 horas extras
            BEGIN
                SET @emp_start = '06:00:00';
                SET @emp_end = DATEADD(HOUR, 12, CAST('06:00:00' AS TIME));
                SET @emp_actual = 12.00;
                SET @emp_paid = 12.00;
            END
            ELSE  -- 65% jornada normal
            BEGIN
                SET @emp_start = '06:00:00';
                SET @emp_end = DATEADD(HOUR, 8, CAST('06:00:00' AS TIME));
                SET @emp_actual = 8.00;
                SET @emp_paid = 8.00;
            END
            
            UPDATE #empleados_tareo
            SET start_time = @emp_start,
                end_time = @emp_end,
                actual_hours = @emp_actual,
                paid_hours = @emp_paid
            WHERE row_num = @emp_counter;
            
            SET @emp_counter = @emp_counter + 1;
        END
        
        -- Insertar empleados del tareo
        INSERT INTO app.tbl_tareo_employees (
            tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours, created_at
        )
        SELECT 
            @tareo_id,
            doc_number,
            start_time,
            end_time,
            actual_hours,
            paid_hours,
            @tareo_fecha
        FROM #empleados_tareo;
        
        SET @total_tareo_employees = @total_tareo_employees + @@ROWCOUNT;
        
        DROP TABLE #empleados_tareo;
    END
    
    FETCH NEXT FROM tareo_cursor INTO @tareo_fecha, @tareo_num, @tareo_labor_id, @tareo_lote_id, @tareo_lote_subsidiary_id, @tareo_empleados_por_tareo;
END

CLOSE tareo_cursor;
DEALLOCATE tareo_cursor;

DROP TABLE #tareos_plan;

PRINT 'Migración V125 completada:';
PRINT '  - 42 empleados adicionales creados';
PRINT '  - Tareos creados desde Agosto 2025 (desde ' + CAST(@fecha_inicio AS NVARCHAR(10)) + ' hasta ' + CAST(@fecha_fin AS NVARCHAR(10)) + ')';
PRINT '  - Total de registros de tareo_employees: ' + CAST(@total_tareo_employees AS NVARCHAR(10));
PRINT '  - Los tareos están listos para procesar en períodos basados en la configuración de empresa';
GO

