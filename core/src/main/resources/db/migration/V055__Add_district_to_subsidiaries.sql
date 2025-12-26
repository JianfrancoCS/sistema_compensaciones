-- V055: Agregar relación de distrito a la tabla de subsidiarias
ALTER TABLE app.tbl_subsidiaries
ADD district_id INT NULL;
GO

-- Agregar foreign key constraint
ALTER TABLE app.tbl_subsidiaries
ADD CONSTRAINT FK_tbl_subsidiaries_tbl_districts
FOREIGN KEY (district_id)
REFERENCES app.tbl_districts(id);
GO

-- Agregar índice para mejorar rendimiento en consultas por distrito
CREATE NONCLUSTERED INDEX IX_tbl_subsidiaries_district_id
ON app.tbl_subsidiaries(district_id);
GO