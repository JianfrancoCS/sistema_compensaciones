-- =============================================
-- V118: INSERTAR DATOS COMPLETOS Y REALES
-- =============================================
-- Crea datos completos y realistas para todo el sistema:
-- - Áreas y Cargos
-- - Fundos (Subsidiarias) y Lotes
-- - Personas y Empleados (uno tras otro)
-- - Asistencias (Markings) para Octubre y Noviembre 2024
-- - Tareos con datos reales para generar planillas

-- =============================================
-- 1. CREAR ÁREAS ADICIONALES
-- =============================================

IF NOT EXISTS (SELECT 1 FROM app.tbl_areas WHERE name = 'Producción' AND deleted_at IS NULL)
BEGIN
    INSERT INTO app.tbl_areas (name) VALUES ('Producción');
END
GO

IF NOT EXISTS (SELECT 1 FROM app.tbl_areas WHERE name = 'Mantenimiento' AND deleted_at IS NULL)
BEGIN
    INSERT INTO app.tbl_areas (name) VALUES ('Mantenimiento');
END
GO

IF NOT EXISTS (SELECT 1 FROM app.tbl_areas WHERE name = 'Administración' AND deleted_at IS NULL)
BEGIN
    INSERT INTO app.tbl_areas (name) VALUES ('Administración');
END
GO

-- =============================================
-- 1.1. CREAR LABORES ADMINISTRATIVAS
-- =============================================

-- Obtener unidad de medida para labores administrativas (jornales)
DECLARE @jornales_unit_id SMALLINT = (SELECT id FROM app.tbl_labor_units WHERE name = 'Jornales' AND deleted_at IS NULL);

IF @jornales_unit_id IS NOT NULL
BEGIN
    -- Labores administrativas (no son de destajo, is_piecework = 0)
    IF NOT EXISTS (SELECT 1 FROM app.tbl_labors WHERE name = 'Atención al Cliente' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
        VALUES ('Atención al Cliente', 'Atención y servicio al cliente interno y externo', @jornales_unit_id, 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_labors WHERE name = 'Gestión de Documentos' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
        VALUES ('Gestión de Documentos', 'Procesamiento, archivo y organización de documentos', @jornales_unit_id, 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_labors WHERE name = 'Procesamiento de Planillas' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
        VALUES ('Procesamiento de Planillas', 'Elaboración y procesamiento de planillas de pago', @jornales_unit_id, 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_labors WHERE name = 'Control de Asistencias' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
        VALUES ('Control de Asistencias', 'Registro y control de asistencias de personal', @jornales_unit_id, 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_labors WHERE name = 'Gestión de Contratos' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
        VALUES ('Gestión de Contratos', 'Elaboración y seguimiento de contratos laborales', @jornales_unit_id, 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_labors WHERE name = 'Archivo y Organización' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
        VALUES ('Archivo y Organización', 'Archivo y organización de documentos administrativos', @jornales_unit_id, 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_labors WHERE name = 'Soporte Administrativo' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
        VALUES ('Soporte Administrativo', 'Apoyo administrativo general', @jornales_unit_id, 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_labors WHERE name = 'Contabilidad y Finanzas' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
        VALUES ('Contabilidad y Finanzas', 'Trabajos contables y financieros', @jornales_unit_id, 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_labors WHERE name = 'Recursos Humanos' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
        VALUES ('Recursos Humanos', 'Gestión de recursos humanos y personal', @jornales_unit_id, 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_labors WHERE name = 'Secretaría' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
        VALUES ('Secretaría', 'Trabajos de secretaría y apoyo ejecutivo', @jornales_unit_id, 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_labors WHERE name = 'Mantenimiento de Equipos' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_labors (name, description, labor_unit_id, is_piecework)
        VALUES ('Mantenimiento de Equipos', 'Mantenimiento y reparación de equipos', @jornales_unit_id, 0);
    END
END
GO

-- =============================================
-- 2. CREAR CARGOS (POSICIONES)
-- =============================================

-- Cargos en Producción
DECLARE @area_produccion_id SMALLINT = (SELECT id FROM app.tbl_areas WHERE name = 'Producción' AND deleted_at IS NULL);
IF @area_produccion_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_positions WHERE name = 'Operario de Campo' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_positions (name, area_id, salary) 
        VALUES ('Operario de Campo', @area_produccion_id, 1500.00);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_positions WHERE name = 'Supervisor de Campo' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_positions (name, area_id, salary) 
        VALUES ('Supervisor de Campo', @area_produccion_id, 2400.00);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_positions WHERE name = 'Jefe de Producción' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_positions (name, area_id, salary) 
        VALUES ('Jefe de Producción', @area_produccion_id, 3500.00);
    END
END
GO

-- Cargos en Mantenimiento
DECLARE @area_mantenimiento_id SMALLINT = (SELECT id FROM app.tbl_areas WHERE name = 'Mantenimiento' AND deleted_at IS NULL);
IF @area_mantenimiento_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_positions WHERE name = 'Mecánico' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_positions (name, area_id, salary) 
        VALUES ('Mecánico', @area_mantenimiento_id, 1800.00);
    END
END
GO

-- Cargos en Administración
DECLARE @area_administracion_id SMALLINT = (SELECT id FROM app.tbl_areas WHERE name = 'Administración' AND deleted_at IS NULL);
IF @area_administracion_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_positions WHERE name = 'Asistente Administrativo' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_positions (name, area_id, salary) 
        VALUES ('Asistente Administrativo', @area_administracion_id, 2000.00);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_positions WHERE name = 'Encargado de Recursos Humanos' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_positions (name, area_id, salary) 
        VALUES ('Encargado de Recursos Humanos', @area_administracion_id, 3200.00);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_positions WHERE name = 'Asistente de Recursos Humanos' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_positions (name, area_id, salary) 
        VALUES ('Asistente de Recursos Humanos', @area_administracion_id, 2200.00);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_positions WHERE name = 'Contador' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_positions (name, area_id, salary) 
        VALUES ('Contador', @area_administracion_id, 3000.00);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_positions WHERE name = 'Asistente Contable' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_positions (name, area_id, salary) 
        VALUES ('Asistente Contable', @area_administracion_id, 2100.00);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_positions WHERE name = 'Gerente Administrativo' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_positions (name, area_id, salary) 
        VALUES ('Gerente Administrativo', @area_administracion_id, 4500.00);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_positions WHERE name = 'Secretaria' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_positions (name, area_id, salary) 
        VALUES ('Secretaria', @area_administracion_id, 1900.00);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_positions WHERE name = 'Auxiliar Administrativo' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_positions (name, area_id, salary) 
        VALUES ('Auxiliar Administrativo', @area_administracion_id, 1800.00);
    END
END
GO

-- =============================================
-- 3. CREAR MÁS FUNDOS (SUBSIDIARIAS)
-- =============================================

DECLARE @company_id_fundos BIGINT = (SELECT id FROM app.tbl_companies WHERE ruc = '20520866630');
DECLARE @district_ica_id INT = (SELECT id FROM app.tbl_districts WHERE ubigeo_inei = '110101' AND deleted_at IS NULL);

IF @company_id_fundos IS NOT NULL AND @district_ica_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_subsidiaries WHERE name = 'FUNDO SANTA ROSA' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_subsidiaries (name, company_id, district_id)
        VALUES ('FUNDO SANTA ROSA', @company_id_fundos, @district_ica_id);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_subsidiaries WHERE name = 'FUNDO LA ESPERANZA' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_subsidiaries (name, company_id, district_id)
        VALUES ('FUNDO LA ESPERANZA', @company_id_fundos, @district_ica_id);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_subsidiaries WHERE name = 'FUNDO EL PARAÍSO' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_subsidiaries (name, company_id, district_id)
        VALUES ('FUNDO EL PARAÍSO', @company_id_fundos, @district_ica_id);
    END
END
GO

-- =============================================
-- 4. CREAR LOTES PARA CADA FUNDO
-- =============================================

-- Lotes para FUNDO 1
DECLARE @fundo1_id SMALLINT = (SELECT id FROM app.tbl_subsidiaries WHERE name = 'FUNDO 1' AND deleted_at IS NULL);
IF @fundo1_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_lotes WHERE name = 'LOTE A' AND subsidiary_id = @fundo1_id AND deleted_at IS NULL)
        INSERT INTO app.tbl_lotes (name, hectareage, subsidiary_id) VALUES ('LOTE A', 5.5, @fundo1_id);
    
    IF NOT EXISTS (SELECT 1 FROM app.tbl_lotes WHERE name = 'LOTE B' AND subsidiary_id = @fundo1_id AND deleted_at IS NULL)
        INSERT INTO app.tbl_lotes (name, hectareage, subsidiary_id) VALUES ('LOTE B', 8.2, @fundo1_id);
    
    IF NOT EXISTS (SELECT 1 FROM app.tbl_lotes WHERE name = 'LOTE C' AND subsidiary_id = @fundo1_id AND deleted_at IS NULL)
        INSERT INTO app.tbl_lotes (name, hectareage, subsidiary_id) VALUES ('LOTE C', 12.0, @fundo1_id);
END
GO

-- Lotes para FUNDO SANTA ROSA
DECLARE @fundo_santa_rosa_id SMALLINT = (SELECT id FROM app.tbl_subsidiaries WHERE name = 'FUNDO SANTA ROSA' AND deleted_at IS NULL);
IF @fundo_santa_rosa_id IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_lotes WHERE name = 'LOTE 1' AND subsidiary_id = @fundo_santa_rosa_id AND deleted_at IS NULL)
        INSERT INTO app.tbl_lotes (name, hectareage, subsidiary_id) VALUES ('LOTE 1', 10.0, @fundo_santa_rosa_id);
    
    IF NOT EXISTS (SELECT 1 FROM app.tbl_lotes WHERE name = 'LOTE 2' AND subsidiary_id = @fundo_santa_rosa_id AND deleted_at IS NULL)
        INSERT INTO app.tbl_lotes (name, hectareage, subsidiary_id) VALUES ('LOTE 2', 15.5, @fundo_santa_rosa_id);
END
GO

-- =============================================
-- 5. CREAR PERSONAS (UNA TRAS OTRA)
-- =============================================

-- Persona 1: Juan Pérez
IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '12345678')
BEGIN
    INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
    VALUES ('12345678', 1, 'JUAN', 'PEREZ', 'GARCIA', '1990-05-15', 'M');
END
GO

-- Persona 2: María López
IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '23456789')
BEGIN
    INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
    VALUES ('23456789', 1, 'MARIA', 'LOPEZ', 'RODRIGUEZ', '1988-08-20', 'F');
END
GO

-- Persona 3: Carlos Ramírez
IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '34567890')
BEGIN
    INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
    VALUES ('34567890', 1, 'CARLOS', 'RAMIREZ', 'SILVA', '1992-11-10', 'M');
END
GO

-- Persona 4: Pedro Martínez
IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '45678901')
BEGIN
    INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
    VALUES ('45678901', 1, 'PEDRO', 'MARTINEZ', 'TORRES', '1985-03-25', 'M');
END
GO

-- Persona 5: Ana Sánchez
IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '56789012')
BEGIN
    INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
    VALUES ('56789012', 1, 'ANA', 'SANCHEZ', 'MORALES', '1991-07-12', 'F');
END
GO

-- Persona 6: Luis Fernández
IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '67890123')
BEGIN
    INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
    VALUES ('67890123', 1, 'LUIS', 'FERNANDEZ', 'CASTRO', '1987-12-05', 'M');
END
GO

-- Persona 7: Rosa Vargas
IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '78901234')
BEGIN
    INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
    VALUES ('78901234', 1, 'ROSA', 'VARGAS', 'MENDOZA', '1989-09-18', 'F');
END
GO

-- Persona 8: José Herrera
IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '89012345')
BEGIN
    INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
    VALUES ('89012345', 1, 'JOSE', 'HERRERA', 'RUIZ', '1993-04-30', 'M');
END
GO

-- =============================================
-- 6. CREAR EMPLEADOS (UNO TRAS OTRO)
-- =============================================

DECLARE @fundo1_emp_id SMALLINT = (SELECT id FROM app.tbl_subsidiaries WHERE name = 'FUNDO 1' AND deleted_at IS NULL);
DECLARE @position_operario_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Operario de Campo' AND deleted_at IS NULL);
DECLARE @position_supervisor_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Supervisor de Campo' AND deleted_at IS NULL);
DECLARE @position_jefe_prod_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Jefe de Producción' AND deleted_at IS NULL);
DECLARE @employee_state_id SMALLINT = (SELECT TOP 1 id FROM app.tbl_states WHERE domain_id = (SELECT id FROM app.tbl_domains WHERE name = 'tbl_employees') AND is_default = 1);

-- Obtener IDs de conceptos de jubilación para distribuir entre empleados
DECLARE @afp_integra_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'AFP_INTEGRA' AND deleted_at IS NULL);
DECLARE @afp_prima_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'AFP_PRIMA' AND deleted_at IS NULL);
DECLARE @afp_profuturo_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'AFP_PROFUTURO' AND deleted_at IS NULL);
DECLARE @afp_habitat_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'AFP_HABITAT' AND deleted_at IS NULL);
DECLARE @onp_id SMALLINT = (SELECT id FROM app.tbl_concepts WHERE code = 'ONP' AND deleted_at IS NULL);

-- Empleado 1: Juan Pérez - Operario (AFP_INTEGRA)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '12345678')
BEGIN
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '12345678', @fundo1_emp_id, @position_operario_id, @employee_state_id,
        '674580YVUA01', '570-72718712-0-97', 'BANCO DE CREDITO DEL PERU', '2023-01-15', 50.00, 1500.00, @afp_integra_id
    );
END

-- Empleado 2: María López - Supervisora (AFP_PRIMA)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '23456789')
BEGIN
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '23456789', @fundo1_emp_id, @position_supervisor_id, @employee_state_id,
        '789012ABVC02', '001-12345678-0-01', 'BANCO DE LA NACION', '2022-06-01', 80.00, 2400.00, @afp_prima_id
    );
END

-- Empleado 3: Carlos Ramírez - Operario (AFP_PROFUTURO)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '34567890')
BEGIN
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '34567890', @fundo1_emp_id, @position_operario_id, @employee_state_id,
        '456789CDEF03', '002-23456789-0-02', 'BBVA', '2023-03-10', 50.00, 1500.00, @afp_profuturo_id
    );
END

-- Empleado 4: Pedro Martínez - Operario (AFP_HABITAT)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '45678901')
BEGIN
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '45678901', @fundo1_emp_id, @position_operario_id, @employee_state_id,
        '123456GHIJ04', '003-34567890-0-03', 'INTERBANK', '2023-05-20', 50.00, 1500.00, @afp_habitat_id
    );
END

-- Empleado 5: Ana Sánchez - Operaria (ONP)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '56789012')
BEGIN
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '56789012', @fundo1_emp_id, @position_operario_id, @employee_state_id,
        '234567KLMN05', '004-45678901-0-04', 'SCOTIABANK', '2023-07-08', 50.00, 1500.00, @onp_id
    );
END

-- Empleado 6: Luis Fernández - Jefe de Producción (AFP_INTEGRA)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '67890123')
BEGIN
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '67890123', @fundo1_emp_id, @position_jefe_prod_id, @employee_state_id,
        '345678OPQR06', '005-56789012-0-05', 'BANCO DE CREDITO DEL PERU', '2021-11-15', 116.67, 3500.00, @afp_integra_id
    );
END

-- Empleado 7: Rosa Vargas - Operaria (AFP_PRIMA)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '78901234')
BEGIN
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '78901234', @fundo1_emp_id, @position_operario_id, @employee_state_id,
        '456789STUV07', '006-67890123-0-06', 'BANCO DE LA NACION', '2023-09-12', 50.00, 1500.00, @afp_prima_id
    );
END

-- Empleado 8: José Herrera - Operario (AFP_PROFUTURO)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '89012345')
BEGIN
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '89012345', @fundo1_emp_id, @position_operario_id, @employee_state_id,
        '567890WXYZ08', '007-78901234-0-07', 'BBVA', '2023-10-01', 50.00, 1500.00, @afp_profuturo_id
    );
END

-- Obtener IDs de nuevos puestos
DECLARE @position_rh_encargado_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Encargado de Recursos Humanos' AND deleted_at IS NULL);
DECLARE @position_rh_asistente_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Asistente de Recursos Humanos' AND deleted_at IS NULL);
DECLARE @position_contador_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Contador' AND deleted_at IS NULL);
DECLARE @position_contador_asistente_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Asistente Contable' AND deleted_at IS NULL);
DECLARE @position_gerente_admin_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Gerente Administrativo' AND deleted_at IS NULL);
DECLARE @position_secretaria_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Secretaria' AND deleted_at IS NULL);
DECLARE @position_auxiliar_admin_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Auxiliar Administrativo' AND deleted_at IS NULL);
DECLARE @position_asistente_admin_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Asistente Administrativo' AND deleted_at IS NULL);
DECLARE @position_mecanico_id SMALLINT = (SELECT id FROM app.tbl_positions WHERE name = 'Mecánico' AND deleted_at IS NULL);

-- Empleado 9: Carmen Vega - Encargada de Recursos Humanos (AFP_HABITAT)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '90123456')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '90123456')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('90123456', 1, 'CARMEN', 'VEGA', 'CAMPOS', '1988-05-15', 'F');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '90123456', @fundo1_emp_id, @position_rh_encargado_id, @employee_state_id,
        '678901ABCD09', '008-89012345-0-08', 'BANCO DE CREDITO DEL PERU', '2022-03-20', 106.67, 3200.00, @afp_habitat_id
    );
END

-- Empleado 10: Roberto Salazar - Asistente de Recursos Humanos (ONP)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '91234567')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '91234567')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('91234567', 1, 'ROBERTO', 'SALAZAR', 'VALDEZ', '1990-08-22', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '91234567', @fundo1_emp_id, @position_rh_asistente_id, @employee_state_id,
        '789012EFGH10', '009-90123456-0-09', 'BANCO DE LA NACION', '2023-04-10', 73.33, 2200.00, @onp_id
    );
END

-- Empleado 11: Fernando Aguilar - Contador (AFP_INTEGRA)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '92345678')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '92345678')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('92345678', 1, 'FERNANDO', 'AGUILAR', 'MEDINA', '1985-11-30', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '92345678', @fundo1_emp_id, @position_contador_id, @employee_state_id,
        '890123IJKL11', '010-91234567-0-10', 'BBVA', '2021-09-15', 100.00, 3000.00, @afp_integra_id
    );
END

-- Empleado 12: Patricia Cortez - Asistente Contable (AFP_PRIMA)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '93456789')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '93456789')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('93456789', 1, 'PATRICIA', 'CORTEZ', 'NAVARRO', '1992-02-18', 'F');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '93456789', @fundo1_emp_id, @position_contador_asistente_id, @employee_state_id,
        '901234MNOP12', '011-92345678-0-11', 'INTERBANK', '2023-06-05', 70.00, 2100.00, @afp_prima_id
    );
END

-- Empleado 13: Jorge León - Gerente Administrativo (AFP_PROFUTURO)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '94567890')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '94567890')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('94567890', 1, 'JORGE', 'LEON', 'MOLINA', '1980-07-12', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '94567890', @fundo1_emp_id, @position_gerente_admin_id, @employee_state_id,
        '012345QRST13', '012-93456789-0-12', 'SCOTIABANK', '2020-01-20', 150.00, 4500.00, @afp_profuturo_id
    );
END

-- Empleado 14: Gloria Cardenas - Secretaria (AFP_HABITAT)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '95678901')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '95678901')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('95678901', 1, 'GLORIA', 'CARDENAS', 'AVILA', '1993-09-25', 'F');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '95678901', @fundo1_emp_id, @position_secretaria_id, @employee_state_id,
        '123456UVWX14', '013-94567890-0-13', 'BANCO DE CREDITO DEL PERU', '2023-08-14', 63.33, 1900.00, @afp_habitat_id
    );
END

-- Empleado 15: Ricardo Sandoval - Auxiliar Administrativo (ONP)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '96789012')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '96789012')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('96789012', 1, 'RICARDO', 'SANDOVAL', 'GUZMAN', '1991-12-08', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '96789012', @fundo1_emp_id, @position_auxiliar_admin_id, @employee_state_id,
        '234567YZAB15', '014-95678901-0-14', 'BANCO DE LA NACION', '2023-10-30', 60.00, 1800.00, @onp_id
    );
END

-- Empleado 16: Manuel Hernández - Asistente Administrativo (AFP_INTEGRA)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '97890123')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '97890123')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('97890123', 1, 'MANUEL', 'HERNANDEZ', 'MUNOZ', '1989-04-17', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '97890123', @fundo1_emp_id, @position_asistente_admin_id, @employee_state_id,
        '345678CDEF16', '015-96789012-0-15', 'BBVA', '2022-11-22', 66.67, 2000.00, @afp_integra_id
    );
END

-- Empleado 17: Alberto Castillo - Mecánico (AFP_PRIMA)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '98901234')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '98901234')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('98901234', 1, 'ALBERTO', 'CASTILLO', 'JARA', '1987-06-03', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '98901234', @fundo1_emp_id, @position_mecanico_id, @employee_state_id,
        '456789GHIJ17', '016-97890123-0-16', 'INTERBANK', '2023-02-28', 60.00, 1800.00, @afp_prima_id
    );
END

-- Empleado 18: Mario Quiroz - Asistente de Recursos Humanos (AFP_PROFUTURO)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '99012345')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '99012345')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('99012345', 1, 'MARIO', 'QUIROZ', 'ESPINOZA', '1994-01-19', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '99012345', @fundo1_emp_id, @position_rh_asistente_id, @employee_state_id,
        '567890KLMN18', '017-98901234-0-17', 'SCOTIABANK', '2023-09-11', 73.33, 2200.00, @afp_profuturo_id
    );
END

-- Empleado 19: Victor ACOSTA - Contador (AFP_HABITAT)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '99123456')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '99123456')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('99123456', 1, 'VICTOR', 'ACOSTA', 'PEÑA', '1986-10-07', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '99123456', @fundo1_emp_id, @position_contador_id, @employee_state_id,
        '678901OPQR19', '018-99012345-0-18', 'BANCO DE CREDITO DEL PERU', '2022-05-16', 100.00, 3000.00, @afp_habitat_id
    );
END

-- Empleado 20: Eduardo Figueroa - Asistente Contable (ONP)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '99234567')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '99234567')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('99234567', 1, 'EDUARDO', 'FIGUEROA', 'VALENCIA', '1995-03-14', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '99234567', @fundo1_emp_id, @position_contador_asistente_id, @employee_state_id,
        '789012STUV20', '019-99123456-0-19', 'BANCO DE LA NACION', '2023-07-25', 70.00, 2100.00, @onp_id
    );
END

-- Empleado 21: Julio Contreras - Asistente Administrativo (AFP_INTEGRA)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '99345678')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '99345678')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('99345678', 1, 'JULIO', 'CONTRERAS', 'CAMPOS', '1992-08-29', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '99345678', @fundo1_emp_id, @position_asistente_admin_id, @employee_state_id,
        '890123WXYZ21', '020-99234567-0-20', 'BBVA', '2023-05-08', 66.67, 2000.00, @afp_integra_id
    );
END

-- Empleado 22: Antonio Palacios - Mecánico (AFP_PRIMA)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '99456789')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '99456789')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('99456789', 1, 'ANTONIO', 'PALACIOS', 'SOTO', '1988-11-21', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '99456789', @fundo1_emp_id, @position_mecanico_id, @employee_state_id,
        '901234ABCD22', '021-99345678-0-21', 'INTERBANK', '2022-12-05', 60.00, 1800.00, @afp_prima_id
    );
END

-- Empleado 23: Francisco Fuentes - Secretaria (AFP_PROFUTURO)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '99567890')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '99567890')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('99567890', 1, 'FRANCISCO', 'FUENTES', 'CARRASCO', '1990-07-04', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '99567890', @fundo1_emp_id, @position_secretaria_id, @employee_state_id,
        '012345EFGH23', '022-99456789-0-22', 'SCOTIABANK', '2023-01-18', 63.33, 1900.00, @afp_profuturo_id
    );
END

-- Empleado 24: Alexander Guzman - Auxiliar Administrativo (AFP_HABITAT)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '99678901')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '99678901')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('99678901', 1, 'ALEXANDER', 'GUZMAN', 'HERNANDEZ', '1993-04-26', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '99678901', @fundo1_emp_id, @position_auxiliar_admin_id, @employee_state_id,
        '123456IJKL24', '023-99567890-0-23', 'BANCO DE CREDITO DEL PERU', '2023-11-12', 60.00, 1800.00, @afp_habitat_id
    );
END

-- Empleado 25: Diego Romero - Encargado de Recursos Humanos (ONP)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '99789012')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '99789012')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('99789012', 1, 'DIEGO', 'ROMERO', 'RUIZ', '1984-09-11', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '99789012', @fundo1_emp_id, @position_rh_encargado_id, @employee_state_id,
        '234567MNOP25', '024-99678901-0-24', 'BANCO DE LA NACION', '2021-08-03', 106.67, 3200.00, @onp_id
    );
END

-- Empleado 26: Jesus Alvarez - Gerente Administrativo (AFP_INTEGRA)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '99890123')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '99890123')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('99890123', 1, 'JESUS', 'ALVAREZ', 'MORENO', '1982-12-23', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '99890123', @fundo1_emp_id, @position_gerente_admin_id, @employee_state_id,
        '345678QRST26', '025-99789012-0-25', 'BBVA', '2019-06-15', 150.00, 4500.00, @afp_integra_id
    );
END

-- Empleado 27: Raul Delgado - Asistente de Recursos Humanos (AFP_PRIMA)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '99901234')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '99901234')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('99901234', 1, 'RAUL', 'DELGADO', 'GUERRERO', '1991-05-30', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '99901234', @fundo1_emp_id, @position_rh_asistente_id, @employee_state_id,
        '456789UVWX27', '026-99890123-0-26', 'INTERBANK', '2023-03-27', 73.33, 2200.00, @afp_prima_id
    );
END

-- Empleado 28: Sergio Ramos - Contador (AFP_PROFUTURO)
IF NOT EXISTS (SELECT 1 FROM app.tbl_employees WHERE person_document_number = '99912345')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_persons WHERE document_number = '99912345')
    BEGIN
        INSERT INTO app.tbl_persons (document_number, document_type_id, names, paternal_lastname, maternal_lastname, dob, gender)
        VALUES ('99912345', 1, 'SERGIO', 'RAMOS', 'SILVA', '1987-02-16', 'M');
    END
    INSERT INTO app.tbl_employees (
        person_document_number, subsidiary_id, position_id, state_id,
        afp_affiliation_number, bank_account_number, bank_name, hire_date, daily_basic_salary, custom_salary, retirement_concept_id
    )
    VALUES (
        '99912345', @fundo1_emp_id, @position_contador_id, @employee_state_id,
        '567890YZAB28', '027-99901234-0-27', 'SCOTIABANK', '2022-10-09', 100.00, 3000.00, @afp_profuturo_id
    );
END
GO

-- =============================================
-- 7. CREAR ASISTENCIAS (MARKINGS) - OCTUBRE 2024
-- =============================================

DECLARE @fundo1_marking_id SMALLINT = (SELECT id FROM app.tbl_subsidiaries WHERE name = 'FUNDO 1' AND deleted_at IS NULL);
DECLARE @marking_reason_work_id SMALLINT = (SELECT id FROM app.tbl_marking_reasons WHERE code = 'WORK' AND deleted_at IS NULL);

-- Crear markings para días laborables clave de Octubre 2024
-- Semana 1 (1-6 Octubre)
IF @fundo1_marking_id IS NOT NULL AND @marking_reason_work_id IS NOT NULL
BEGIN
    -- 1 Octubre
    IF NOT EXISTS (SELECT 1 FROM app.tbl_markings WHERE subsidiary_id = @fundo1_marking_id AND marking_date = '2025-10-01' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_markings (subsidiary_id, marking_date, created_by) VALUES (@fundo1_marking_id, '2025-10-01', 'SYSTEM');
        DECLARE @mark_oct01_id BIGINT = SCOPE_IDENTITY();
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct01_id, @marking_reason_work_id, '12345678', 1, '2025-10-01 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct01_id, @marking_reason_work_id, '12345678', 0, '2025-10-01 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct01_id, @marking_reason_work_id, '23456789', 1, '2025-10-01 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct01_id, @marking_reason_work_id, '23456789', 0, '2025-10-01 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct01_id, @marking_reason_work_id, '34567890', 1, '2025-10-01 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct01_id, @marking_reason_work_id, '34567890', 0, '2025-10-01 14:00:00');
    END

    -- 2 Octubre
    IF NOT EXISTS (SELECT 1 FROM app.tbl_markings WHERE subsidiary_id = @fundo1_marking_id AND marking_date = '2025-10-02' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_markings (subsidiary_id, marking_date, created_by) VALUES (@fundo1_marking_id, '2025-10-02', 'SYSTEM');
        DECLARE @mark_oct02_id BIGINT = SCOPE_IDENTITY();
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct02_id, @marking_reason_work_id, '12345678', 1, '2025-10-02 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct02_id, @marking_reason_work_id, '12345678', 0, '2025-10-02 16:00:00'); -- 2 horas extras
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct02_id, @marking_reason_work_id, '23456789', 1, '2025-10-02 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct02_id, @marking_reason_work_id, '23456789', 0, '2025-10-02 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct02_id, @marking_reason_work_id, '34567890', 1, '2025-10-02 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct02_id, @marking_reason_work_id, '34567890', 0, '2025-10-02 14:00:00');
    END

    -- 4 Octubre
    IF NOT EXISTS (SELECT 1 FROM app.tbl_markings WHERE subsidiary_id = @fundo1_marking_id AND marking_date = '2025-10-04' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_markings (subsidiary_id, marking_date, created_by) VALUES (@fundo1_marking_id, '2025-10-04', 'SYSTEM');
        DECLARE @mark_oct04_id BIGINT = SCOPE_IDENTITY();
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct04_id, @marking_reason_work_id, '12345678', 1, '2025-10-04 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct04_id, @marking_reason_work_id, '12345678', 0, '2025-10-04 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct04_id, @marking_reason_work_id, '23456789', 1, '2025-10-04 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct04_id, @marking_reason_work_id, '23456789', 0, '2025-10-04 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct04_id, @marking_reason_work_id, '34567890', 1, '2025-10-04 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct04_id, @marking_reason_work_id, '34567890', 0, '2025-10-04 14:00:00');
    END

    -- 5 Octubre
    IF NOT EXISTS (SELECT 1 FROM app.tbl_markings WHERE subsidiary_id = @fundo1_marking_id AND marking_date = '2025-10-05' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_markings (subsidiary_id, marking_date, created_by) VALUES (@fundo1_marking_id, '2025-10-05', 'SYSTEM');
        DECLARE @mark_oct05_id BIGINT = SCOPE_IDENTITY();
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct05_id, @marking_reason_work_id, '12345678', 1, '2025-10-05 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct05_id, @marking_reason_work_id, '12345678', 0, '2025-10-05 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct05_id, @marking_reason_work_id, '23456789', 1, '2025-10-05 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct05_id, @marking_reason_work_id, '23456789', 0, '2025-10-05 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct05_id, @marking_reason_work_id, '34567890', 1, '2025-10-05 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct05_id, @marking_reason_work_id, '34567890', 0, '2025-10-05 14:00:00');
    END

    -- 15 Octubre (día representativo de mitad de mes)
    IF NOT EXISTS (SELECT 1 FROM app.tbl_markings WHERE subsidiary_id = @fundo1_marking_id AND marking_date = '2025-10-15' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_markings (subsidiary_id, marking_date, created_by) VALUES (@fundo1_marking_id, '2025-10-15', 'SYSTEM');
        DECLARE @mark_oct15_id BIGINT = SCOPE_IDENTITY();
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct15_id, @marking_reason_work_id, '12345678', 1, '2025-10-15 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct15_id, @marking_reason_work_id, '12345678', 0, '2025-10-15 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct15_id, @marking_reason_work_id, '23456789', 1, '2025-10-15 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct15_id, @marking_reason_work_id, '23456789', 0, '2025-10-15 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct15_id, @marking_reason_work_id, '34567890', 1, '2025-10-15 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct15_id, @marking_reason_work_id, '34567890', 0, '2025-10-15 14:00:00');
    END

    -- 30 Octubre (día representativo de fin de mes)
    IF NOT EXISTS (SELECT 1 FROM app.tbl_markings WHERE subsidiary_id = @fundo1_marking_id AND marking_date = '2025-10-30' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_markings (subsidiary_id, marking_date, created_by) VALUES (@fundo1_marking_id, '2025-10-30', 'SYSTEM');
        DECLARE @mark_oct30_id BIGINT = SCOPE_IDENTITY();
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct30_id, @marking_reason_work_id, '12345678', 1, '2025-10-30 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct30_id, @marking_reason_work_id, '12345678', 0, '2025-10-30 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct30_id, @marking_reason_work_id, '23456789', 1, '2025-10-30 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct30_id, @marking_reason_work_id, '23456789', 0, '2025-10-30 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct30_id, @marking_reason_work_id, '34567890', 1, '2025-10-30 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_oct30_id, @marking_reason_work_id, '34567890', 0, '2025-10-30 14:00:00');
    END
END
GO

-- =============================================
-- 8. CREAR ASISTENCIAS (MARKINGS) - NOVIEMBRE 2024
-- =============================================

DECLARE @fundo1_marking_nov_id SMALLINT = (SELECT id FROM app.tbl_subsidiaries WHERE name = 'FUNDO 1' AND deleted_at IS NULL);
DECLARE @marking_reason_work_nov_id SMALLINT = (SELECT id FROM app.tbl_marking_reasons WHERE code = 'WORK' AND deleted_at IS NULL);

-- Crear markings para días laborables clave de Noviembre 2024
IF @fundo1_marking_nov_id IS NOT NULL AND @marking_reason_work_nov_id IS NOT NULL
BEGIN
    -- 1 Noviembre
    IF NOT EXISTS (SELECT 1 FROM app.tbl_markings WHERE subsidiary_id = @fundo1_marking_nov_id AND marking_date = '2025-11-01' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_markings (subsidiary_id, marking_date, created_by) VALUES (@fundo1_marking_nov_id, '2025-11-01', 'SYSTEM');
        DECLARE @mark_nov01_id BIGINT = SCOPE_IDENTITY();
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov01_id, @marking_reason_work_nov_id, '12345678', 1, '2025-11-01 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov01_id, @marking_reason_work_nov_id, '12345678', 0, '2025-11-01 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov01_id, @marking_reason_work_nov_id, '23456789', 1, '2025-11-01 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov01_id, @marking_reason_work_nov_id, '23456789', 0, '2025-11-01 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov01_id, @marking_reason_work_nov_id, '34567890', 1, '2025-11-01 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov01_id, @marking_reason_work_nov_id, '34567890', 0, '2025-11-01 14:00:00');
    END

    -- 2 Noviembre
    IF NOT EXISTS (SELECT 1 FROM app.tbl_markings WHERE subsidiary_id = @fundo1_marking_nov_id AND marking_date = '2025-11-02' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_markings (subsidiary_id, marking_date, created_by) VALUES (@fundo1_marking_nov_id, '2025-11-02', 'SYSTEM');
        DECLARE @mark_nov02_id BIGINT = SCOPE_IDENTITY();
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov02_id, @marking_reason_work_nov_id, '12345678', 1, '2025-11-02 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov02_id, @marking_reason_work_nov_id, '12345678', 0, '2025-11-02 16:00:00'); -- 2 horas extras
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov02_id, @marking_reason_work_nov_id, '23456789', 1, '2025-11-02 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov02_id, @marking_reason_work_nov_id, '23456789', 0, '2025-11-02 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov02_id, @marking_reason_work_nov_id, '34567890', 1, '2025-11-02 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov02_id, @marking_reason_work_nov_id, '34567890', 0, '2025-11-02 14:00:00');
    END

    -- 3 Noviembre
    IF NOT EXISTS (SELECT 1 FROM app.tbl_markings WHERE subsidiary_id = @fundo1_marking_nov_id AND marking_date = '2025-11-03' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_markings (subsidiary_id, marking_date, created_by) VALUES (@fundo1_marking_nov_id, '2025-11-03', 'SYSTEM');
        DECLARE @mark_nov03_id BIGINT = SCOPE_IDENTITY();
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov03_id, @marking_reason_work_nov_id, '12345678', 1, '2025-11-03 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov03_id, @marking_reason_work_nov_id, '12345678', 0, '2025-11-03 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov03_id, @marking_reason_work_nov_id, '23456789', 1, '2025-11-03 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov03_id, @marking_reason_work_nov_id, '23456789', 0, '2025-11-03 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov03_id, @marking_reason_work_nov_id, '34567890', 1, '2025-11-03 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov03_id, @marking_reason_work_nov_id, '34567890', 0, '2025-11-03 14:00:00');
    END

    -- 15 Noviembre (día representativo de mitad de mes)
    IF NOT EXISTS (SELECT 1 FROM app.tbl_markings WHERE subsidiary_id = @fundo1_marking_nov_id AND marking_date = '2025-11-15' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_markings (subsidiary_id, marking_date, created_by) VALUES (@fundo1_marking_nov_id, '2025-11-15', 'SYSTEM');
        DECLARE @mark_nov15_id BIGINT = SCOPE_IDENTITY();
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov15_id, @marking_reason_work_nov_id, '12345678', 1, '2025-11-15 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov15_id, @marking_reason_work_nov_id, '12345678', 0, '2025-11-15 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov15_id, @marking_reason_work_nov_id, '23456789', 1, '2025-11-15 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov15_id, @marking_reason_work_nov_id, '23456789', 0, '2025-11-15 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov15_id, @marking_reason_work_nov_id, '34567890', 1, '2025-11-15 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov15_id, @marking_reason_work_nov_id, '34567890', 0, '2025-11-15 14:00:00');
    END

    -- 30 Noviembre (día representativo de fin de mes)
    IF NOT EXISTS (SELECT 1 FROM app.tbl_markings WHERE subsidiary_id = @fundo1_marking_nov_id AND marking_date = '2025-11-30' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_markings (subsidiary_id, marking_date, created_by) VALUES (@fundo1_marking_nov_id, '2025-11-30', 'SYSTEM');
        DECLARE @mark_nov30_id BIGINT = SCOPE_IDENTITY();
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov30_id, @marking_reason_work_nov_id, '12345678', 1, '2025-11-30 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov30_id, @marking_reason_work_nov_id, '12345678', 0, '2025-11-30 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov30_id, @marking_reason_work_nov_id, '23456789', 1, '2025-11-30 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov30_id, @marking_reason_work_nov_id, '23456789', 0, '2025-11-30 14:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov30_id, @marking_reason_work_nov_id, '34567890', 1, '2025-11-30 06:00:00');
        INSERT INTO app.tbl_marking_details (marking_id, marking_reason_id, person_document_number, is_entry, marked_at) VALUES (@mark_nov30_id, @marking_reason_work_nov_id, '34567890', 0, '2025-11-30 14:00:00');
    END
END
GO

-- =============================================
-- 9. CREAR TAREOS PARA OCTUBRE Y NOVIEMBRE
-- =============================================

-- Tareos para Octubre (algunos días representativos)
DECLARE @supervisor_doc_oct NVARCHAR(15) = '23456789'; -- María López es supervisora
DECLARE @labor_cosecha_oct_id SMALLINT = (SELECT TOP 1 id FROM app.tbl_labors WHERE name LIKE '%Cosecha%' AND deleted_at IS NULL);
DECLARE @lote_a_oct_id INT = (SELECT id FROM app.tbl_lotes WHERE name = 'LOTE A' AND deleted_at IS NULL);

IF @labor_cosecha_oct_id IS NOT NULL AND @lote_a_oct_id IS NOT NULL AND @supervisor_doc_oct IS NOT NULL
BEGIN
    -- Tareo 1: 2025-10-02
    IF NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-2025-10-02-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number)
        VALUES ('TAREO-2025-10-02-001', @supervisor_doc_oct, @labor_cosecha_oct_id, @lote_a_oct_id, @supervisor_doc_oct);
        
        DECLARE @tareo_oct1_id INT = SCOPE_IDENTITY();
        
        -- Agregar empleados al tareo (distribuir diferentes AFPs)
        -- Empleado con AFP_INTEGRA
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_oct1_id, '12345678', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con AFP_PROFUTURO
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_oct1_id, '34567890', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con AFP_HABITAT
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_oct1_id, '45678901', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con AFP_PRIMA
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_oct1_id, '78901234', '06:00:00', '14:00:00', 8.00, 8.00);
    END
    
    -- Tareo 2: 2025-10-05 (con horas extras)
    IF NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-2025-10-05-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number)
        VALUES ('TAREO-2025-10-05-001', @supervisor_doc_oct, @labor_cosecha_oct_id, @lote_a_oct_id, @supervisor_doc_oct);
        
        DECLARE @tareo_oct2_id INT = SCOPE_IDENTITY();
        
        -- Empleado con AFP_INTEGRA (con horas extras)
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_oct2_id, '12345678', '06:00:00', '16:00:00', 10.00, 10.00); -- 2 horas extras
        
        -- Empleado con AFP_PROFUTURO
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_oct2_id, '34567890', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con AFP_HABITAT
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_oct2_id, '45678901', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con AFP_PRIMA
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_oct2_id, '78901234', '06:00:00', '14:00:00', 8.00, 8.00);
    END
    
    -- Tareo 3: 2025-10-15
    IF NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-2025-10-15-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number)
        VALUES ('TAREO-2025-10-15-001', @supervisor_doc_oct, @labor_cosecha_oct_id, @lote_a_oct_id, @supervisor_doc_oct);
        
        DECLARE @tareo_oct3_id INT = SCOPE_IDENTITY();
        
        -- Empleado con AFP_INTEGRA
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_oct3_id, '12345678', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con AFP_PROFUTURO
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_oct3_id, '34567890', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con ONP
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_oct3_id, '56789012', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con AFP_PRIMA
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_oct3_id, '78901234', '06:00:00', '14:00:00', 8.00, 8.00);
    END
END
GO

-- Tareos para Noviembre
DECLARE @supervisor_doc_nov NVARCHAR(15) = '23456789'; -- María López es supervisora
DECLARE @labor_cosecha_nov_id SMALLINT = (SELECT TOP 1 id FROM app.tbl_labors WHERE name LIKE '%Cosecha%' AND deleted_at IS NULL);
DECLARE @lote_a_nov_id INT = (SELECT id FROM app.tbl_lotes WHERE name = 'LOTE A' AND deleted_at IS NULL);

IF @labor_cosecha_nov_id IS NOT NULL AND @lote_a_nov_id IS NOT NULL AND @supervisor_doc_nov IS NOT NULL
BEGIN
    -- Tareo 1: 2025-11-02 (con horas extras)
    IF NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-2025-11-02-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number)
        VALUES ('TAREO-2025-11-02-001', @supervisor_doc_nov, @labor_cosecha_nov_id, @lote_a_nov_id, @supervisor_doc_nov);
        
        DECLARE @tareo_nov1_id INT = SCOPE_IDENTITY();
        
        -- Empleado con AFP_INTEGRA (con horas extras)
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_nov1_id, '12345678', '06:00:00', '16:00:00', 10.00, 10.00); -- 2 horas extras
        
        -- Empleado con AFP_PROFUTURO
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_nov1_id, '34567890', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con AFP_HABITAT
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_nov1_id, '45678901', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con AFP_PRIMA
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_nov1_id, '78901234', '06:00:00', '14:00:00', 8.00, 8.00);
    END
    
    -- Tareo 2: 2025-11-15
    IF NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-2025-11-15-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number)
        VALUES ('TAREO-2025-11-15-001', @supervisor_doc_nov, @labor_cosecha_nov_id, @lote_a_nov_id, @supervisor_doc_nov);
        
        DECLARE @tareo_nov2_id INT = SCOPE_IDENTITY();
        
        -- Empleado con AFP_INTEGRA
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_nov2_id, '12345678', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con AFP_PROFUTURO
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_nov2_id, '34567890', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con ONP
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_nov2_id, '56789012', '06:00:00', '14:00:00', 8.00, 8.00);
        
        -- Empleado con AFP_PRIMA
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_nov2_id, '78901234', '06:00:00', '14:00:00', 8.00, 8.00);
    END
END
GO

-- =============================================
-- 10. CREAR TAREOS ADMINISTRATIVOS PARA OCTUBRE Y NOVIEMBRE
-- =============================================

-- Obtener IDs de labores administrativas
DECLARE @labor_rrhh_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Recursos Humanos' AND deleted_at IS NULL);
DECLARE @labor_contabilidad_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Contabilidad y Finanzas' AND deleted_at IS NULL);
DECLARE @labor_planillas_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Procesamiento de Planillas' AND deleted_at IS NULL);
DECLARE @labor_asistencias_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Control de Asistencias' AND deleted_at IS NULL);
DECLARE @labor_documentos_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Gestión de Documentos' AND deleted_at IS NULL);
DECLARE @labor_secretaria_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Secretaría' AND deleted_at IS NULL);
DECLARE @labor_soporte_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Soporte Administrativo' AND deleted_at IS NULL);
DECLARE @labor_mantenimiento_equipos_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Mantenimiento de Equipos' AND deleted_at IS NULL);

-- Supervisor para tareos administrativos (usar Gerente Administrativo)
DECLARE @supervisor_admin_doc NVARCHAR(15) = '94567890'; -- Jorge León - Gerente Administrativo

-- Tareos administrativos para Octubre 2025 2025
-- Los tareos administrativos NO requieren lote (lote_id = NULL)
IF @supervisor_admin_doc IS NOT NULL
BEGIN
    -- Tareo Administrativo 1: Recursos Humanos - 2025-10-03
    IF @labor_rrhh_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-ADMIN-2025-10-03-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-ADMIN-2025-10-03-001', @supervisor_admin_doc, @labor_rrhh_id, NULL, NULL, '2025-10-03');
        
        DECLARE @tareo_admin_oct1_id INT = SCOPE_IDENTITY();
        
        -- Empleados de Recursos Humanos
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct1_id, '90123456', '08:00:00', '17:00:00', 8.00, 8.00); -- Carmen Vega - Encargada RH (AFP_HABITAT)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct1_id, '91234567', '08:00:00', '17:00:00', 8.00, 8.00); -- Roberto Salazar - Asistente RH (ONP)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct1_id, '99012345', '08:00:00', '17:00:00', 8.00, 8.00); -- Mario Quiroz - Asistente RH (AFP_PROFUTURO)
    END

    -- Tareo Administrativo 2: Contabilidad - 2025-10-04
    IF @labor_contabilidad_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-ADMIN-2025-10-04-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-ADMIN-2025-10-04-001', @supervisor_admin_doc, @labor_contabilidad_id, NULL, NULL, '2025-10-04');
        
        DECLARE @tareo_admin_oct2_id INT = SCOPE_IDENTITY();
        
        -- Empleados de Contabilidad
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct2_id, '92345678', '08:00:00', '17:00:00', 8.00, 8.00); -- Fernando Aguilar - Contador (AFP_INTEGRA)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct2_id, '93456789', '08:00:00', '17:00:00', 8.00, 8.00); -- Patricia Cortez - Asistente Contable (AFP_PRIMA)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct2_id, '99123456', '08:00:00', '17:00:00', 8.00, 8.00); -- Victor ACOSTA - Contador (AFP_HABITAT)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct2_id, '99234567', '08:00:00', '17:00:00', 8.00, 8.00); -- Eduardo Figueroa - Asistente Contable (ONP)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct2_id, '99912345', '08:00:00', '17:00:00', 8.00, 8.00); -- Sergio Ramos - Contador (AFP_PROFUTURO)
    END

    -- Tareo Administrativo 3: Procesamiento de Planillas - 2025-10-10
    IF @labor_planillas_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-ADMIN-2025-10-10-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-ADMIN-2025-10-10-001', @supervisor_admin_doc, @labor_planillas_id, NULL, NULL, '2025-10-10');
        
        DECLARE @tareo_admin_oct3_id INT = SCOPE_IDENTITY();
        
        -- Empleados procesando planillas
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct3_id, '90123456', '08:00:00', '17:00:00', 8.00, 8.00); -- Carmen Vega - Encargada RH (AFP_HABITAT)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct3_id, '92345678', '08:00:00', '17:00:00', 8.00, 8.00); -- Fernando Aguilar - Contador (AFP_INTEGRA)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct3_id, '94567890', '08:00:00', '17:00:00', 8.00, 8.00); -- Jorge León - Gerente Administrativo (AFP_PROFUTURO)
    END

    -- Tareo Administrativo 4: Control de Asistencias - 2025-10-16
    IF @labor_asistencias_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-ADMIN-2025-10-16-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-ADMIN-2025-10-16-001', @supervisor_admin_doc, @labor_asistencias_id, NULL, NULL, '2025-10-16');
        
        DECLARE @tareo_admin_oct4_id INT = SCOPE_IDENTITY();
        
        -- Empleados controlando asistencias
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct4_id, '91234567', '08:00:00', '17:00:00', 8.00, 8.00); -- Roberto Salazar - Asistente RH (ONP)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct4_id, '97890123', '08:00:00', '17:00:00', 8.00, 8.00); -- Manuel Hernández - Asistente Administrativo (AFP_INTEGRA)
    END

    -- Tareo Administrativo 5: Secretaría - 2025-10-20
    IF @labor_secretaria_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-ADMIN-2025-10-20-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-ADMIN-2025-10-20-001', @supervisor_admin_doc, @labor_secretaria_id, NULL, NULL, '2025-10-20');
        
        DECLARE @tareo_admin_oct5_id INT = SCOPE_IDENTITY();
        
        -- Empleados de secretaría
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct5_id, '95678901', '08:00:00', '17:00:00', 8.00, 8.00); -- Gloria Cardenas - Secretaria (AFP_HABITAT)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct5_id, '99567890', '08:00:00', '17:00:00', 8.00, 8.00); -- Francisco Fuentes - Secretaria (AFP_PROFUTURO)
    END

    -- Tareo Administrativo 6: Gestión de Documentos - 2025-10-25
    IF @labor_documentos_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-ADMIN-2025-10-25-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-ADMIN-2025-10-25-001', @supervisor_admin_doc, @labor_documentos_id, NULL, NULL, '2025-10-25');
        
        DECLARE @tareo_admin_oct6_id INT = SCOPE_IDENTITY();
        
        -- Empleados gestionando documentos
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct6_id, '96789012', '08:00:00', '17:00:00', 8.00, 8.00); -- Ricardo Sandoval - Auxiliar Administrativo (ONP)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct6_id, '99345678', '08:00:00', '17:00:00', 8.00, 8.00); -- Julio Contreras - Asistente Administrativo (AFP_INTEGRA)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct6_id, '99678901', '08:00:00', '17:00:00', 8.00, 8.00); -- Alexander Guzman - Auxiliar Administrativo (AFP_HABITAT)
    END

    -- Tareo Administrativo 7: Mantenimiento de Equipos - 2025-10-28
    IF @labor_mantenimiento_equipos_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-ADMIN-2025-10-28-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-ADMIN-2025-10-28-001', @supervisor_admin_doc, @labor_mantenimiento_equipos_id, NULL, NULL, '2025-10-28');
        
        DECLARE @tareo_admin_oct7_id INT = SCOPE_IDENTITY();
        
        -- Mecánicos
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct7_id, '98901234', '08:00:00', '17:00:00', 8.00, 8.00); -- Alberto Castillo - Mecánico (AFP_PRIMA)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_oct7_id, '99456789', '08:00:00', '17:00:00', 8.00, 8.00); -- Antonio Palacios - Mecánico (AFP_PRIMA)
    END
END
GO

-- Tareos administrativos para Noviembre 2025
-- Redeclarar todas las variables después del GO
DECLARE @labor_rrhh_nov_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Recursos Humanos' AND deleted_at IS NULL);
DECLARE @labor_contabilidad_nov_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Contabilidad y Finanzas' AND deleted_at IS NULL);
DECLARE @labor_planillas_nov_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Procesamiento de Planillas' AND deleted_at IS NULL);
DECLARE @labor_soporte_nov_id SMALLINT = (SELECT id FROM app.tbl_labors WHERE name = 'Soporte Administrativo' AND deleted_at IS NULL);
DECLARE @supervisor_admin_doc_nov NVARCHAR(15) = '94567890'; -- Jorge León - Gerente Administrativo

IF @supervisor_admin_doc_nov IS NOT NULL
BEGIN
    -- Tareo Administrativo 1: Recursos Humanos - 2025-11-05
    IF @labor_rrhh_nov_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-ADMIN-2025-11-05-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-ADMIN-2025-11-05-001', @supervisor_admin_doc_nov, @labor_rrhh_nov_id, NULL, NULL, '2025-11-05');
        
        DECLARE @tareo_admin_nov1_id INT = SCOPE_IDENTITY();
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_nov1_id, '90123456', '08:00:00', '17:00:00', 8.00, 8.00); -- Carmen Vega - Encargada RH (AFP_HABITAT)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_nov1_id, '99789012', '08:00:00', '17:00:00', 8.00, 8.00); -- Diego Romero - Encargado RH (ONP)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_nov1_id, '99901234', '08:00:00', '17:00:00', 8.00, 8.00); -- Raul Delgado - Asistente RH (AFP_PRIMA)
    END

    -- Tareo Administrativo 2: Contabilidad - 2025-11-08
    IF @labor_contabilidad_nov_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-ADMIN-2025-11-08-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-ADMIN-2025-11-08-001', @supervisor_admin_doc_nov, @labor_contabilidad_nov_id, NULL, NULL, '2025-11-08');
        
        DECLARE @tareo_admin_nov2_id INT = SCOPE_IDENTITY();
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_nov2_id, '92345678', '08:00:00', '17:00:00', 8.00, 8.00); -- Fernando Aguilar - Contador (AFP_INTEGRA)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_nov2_id, '93456789', '08:00:00', '17:00:00', 8.00, 8.00); -- Patricia Cortez - Asistente Contable (AFP_PRIMA)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_nov2_id, '99912345', '08:00:00', '17:00:00', 8.00, 8.00); -- Sergio Ramos - Contador (AFP_PROFUTURO)
    END

    -- Tareo Administrativo 3: Procesamiento de Planillas - 2025-11-12
    IF @labor_planillas_nov_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-ADMIN-2025-11-12-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-ADMIN-2025-11-12-001', @supervisor_admin_doc_nov, @labor_planillas_nov_id, NULL, NULL, '2025-11-12');
        
        DECLARE @tareo_admin_nov3_id INT = SCOPE_IDENTITY();
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_nov3_id, '90123456', '08:00:00', '17:00:00', 8.00, 8.00); -- Carmen Vega - Encargada RH (AFP_HABITAT)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_nov3_id, '92345678', '08:00:00', '17:00:00', 8.00, 8.00); -- Fernando Aguilar - Contador (AFP_INTEGRA)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_nov3_id, '99890123', '08:00:00', '17:00:00', 8.00, 8.00); -- Jesus Alvarez - Gerente Administrativo (AFP_INTEGRA)
    END

    -- Tareo Administrativo 4: Soporte Administrativo - 2025-11-18
    IF @labor_soporte_nov_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM app.tbl_tareos WHERE temporal_id = 'TAREO-ADMIN-2025-11-18-001' AND deleted_at IS NULL)
    BEGIN
        INSERT INTO app.tbl_tareos (temporal_id, supervisor_employee_document_number, labor_id, lote_id, scanner_employee_document_number, created_at)
        VALUES ('TAREO-ADMIN-2025-11-18-001', @supervisor_admin_doc_nov, @labor_soporte_nov_id, NULL, NULL, '2025-11-18');
        
        DECLARE @tareo_admin_nov4_id INT = SCOPE_IDENTITY();
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_nov4_id, '97890123', '08:00:00', '17:00:00', 8.00, 8.00); -- Manuel Hernández - Asistente Administrativo (AFP_INTEGRA)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_nov4_id, '99345678', '08:00:00', '17:00:00', 8.00, 8.00); -- Julio Contreras - Asistente Administrativo (AFP_INTEGRA)
        
        INSERT INTO app.tbl_tareo_employees (tareo_id, employee_document_number, start_time, end_time, actual_hours, paid_hours)
        VALUES (@tareo_admin_nov4_id, '96789012', '08:00:00', '17:00:00', 8.00, 8.00); -- Ricardo Sandoval - Auxiliar Administrativo (ONP)
    END
END
GO

PRINT 'Migración V118 completada: Datos completos insertados (áreas, cargos, fundos, lotes, personas, empleados, asistencias, tareos de campo y administrativos)';
GO

