INSERT INTO app.tbl_variables (code, name, default_value, created_by)
VALUES
-- Variables de empresa
('NOMBRE_EMPRESA', 'Nombre de la Empresa', 'INKA''S BERRIES S.A.C', 'SYSTEM'),
('RUC_EMPRESA', 'RUC de la Empresa', '20520866630', 'SYSTEM'),
('DOMICILIO_EMPRESA', 'Domicilio de la Empresa', NULL, 'SYSTEM'),
('REPRESENTANTE_LEGAL', 'Representante Legal', NULL, 'SYSTEM'),

-- Variables de trabajador
('NOMBRE_COMPLETO_TRABAJADOR', 'Nombre Completo del Trabajador', NULL, 'SYSTEM'),
('DNI_TRABAJADOR', 'DNI del Trabajador', NULL, 'SYSTEM'),
('DOMICILIO_TRABAJADOR', 'Domicilio del Trabajador', NULL, 'SYSTEM'),
('NACIONALIDAD_TRABAJADOR', 'Nacionalidad del Trabajador', 'PERUANA', 'SYSTEM'),
('FECHA_NACIMIENTO_TRABAJADOR', 'Fecha de Nacimiento del Trabajador', NULL, 'SYSTEM'),
('CARGO_TRABAJADOR', 'Cargo del Trabajador', NULL, 'SYSTEM'),

-- Variables de fechas del contrato
('FECHA_FIRMA_CONTRATO', 'Fecha de Firma del Contrato', NULL, 'SYSTEM'),
('FECHA_INICIO_CONTRATO', 'Fecha de Inicio del Contrato', NULL, 'SYSTEM'),
('FECHA_FIN_CONTRATO', 'Fecha de Fin del Contrato', NULL, 'SYSTEM'),
('DURACION_MESES', 'Duración del Contrato (meses)', NULL, 'SYSTEM'),

-- Variables laborales
('HORAS_DIARIAS', 'Horas Laborales Diarias', '8', 'SYSTEM'),
('DIAS_SEMANALES', 'Días Laborales Semanales', '6', 'SYSTEM'),
('SALARIO_BASE', 'Salario Base Mensual', '1130', 'SYSTEM'),
('TARIFA_HORA', 'Tarifa por Hora', '4.71', 'SYSTEM'),

-- Variables de bonificaciones
('RECARGO_HORAS_EXTRAS', 'Recargo Horas Extras (%)', '25', 'SYSTEM'),
('RECARGO_FERIADOS', 'Recargo Días Feriados (%)', '100', 'SYSTEM'),
('BONO_NOCTURNO', 'Bono Turno Nocturno (%)', '35', 'SYSTEM'),

-- Variables de descuentos legales
('DESCUENTO_RENTA', 'Descuento Impuesto a la Renta (%)', '8', 'SYSTEM'),
('DESCUENTO_ESSALUD', 'Descuento EsSalud (%)', '4', 'SYSTEM'),
('DESCUENTO_PENSIONES', 'Descuento Sistema Pensiones (%)', '10', 'SYSTEM'),

-- Variables de beneficios
('DIAS_VACACIONES', 'Días de Vacaciones Anuales', '30', 'SYSTEM'),
('LIMITE_ANTICIPOS', 'Límite de Anticipos (S/)', '500', 'SYSTEM'),

-- Variables agrícolas
('TIPO_CULTIVO', 'Tipo de Cultivo', 'arándanos', 'SYSTEM'),
('UBICACION_PREDIO', 'Ubicación del Predio', 'Chiclayo, Lambayeque', 'SYSTEM');
