-- V046: Inserción de tipos de adenda por defecto
INSERT INTO app.tbl_addendum_types (code, name, description) VALUES
('ADDEN_PLAZO', 'PLAZO', 'Adenda para la modificación del plazo del contrato'),
('ADDEN_ECONOMICO', 'ECONÓMICO', 'Adenda para la modificación de las condiciones económicas');
GO
