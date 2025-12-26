-- V063: Agregar tipo de documento a la tabla de personas
ALTER TABLE app.tbl_persons
ADD document_type_id SMALLINT NULL;
GO

-- Crear la foreign key
ALTER TABLE app.tbl_persons
ADD CONSTRAINT FK_persons_document_type FOREIGN KEY (document_type_id) REFERENCES app.tbl_document_types(id);
GO

-- Actualizar personas existentes con DNI por defecto (asumiendo que las existentes tienen DNI)
UPDATE app.tbl_persons
SET document_type_id = (SELECT id FROM app.tbl_document_types WHERE code = 'DNI')
WHERE document_type_id IS NULL;
GO

-- Hacer el campo obligatorio después de la actualización
ALTER TABLE app.tbl_persons
ALTER COLUMN document_type_id SMALLINT NOT NULL;
GO