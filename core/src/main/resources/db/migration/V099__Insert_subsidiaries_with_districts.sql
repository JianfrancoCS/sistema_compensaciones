-- V099: Insertar subsidiarias con relación a distrito ICA

-- Obtener el ID de la compañía
DECLARE @company_id BIGINT;
SELECT @company_id = id FROM app.tbl_companies WHERE ruc = '20520866630';

-- Obtener el ID del distrito ICA (usando ubigeo_inei '110101')
DECLARE @district_id INT;
SELECT @district_id = id FROM app.tbl_districts WHERE ubigeo_inei = '110101' AND deleted_at IS NULL;

-- Insertar subsidiarias con relación a la compañía y al distrito ICA
IF @company_id IS NOT NULL AND @district_id IS NOT NULL
BEGIN
    INSERT INTO app.tbl_subsidiaries (name, company_id, district_id)
    VALUES ('FUNDO 1', @company_id, @district_id);

    INSERT INTO app.tbl_subsidiaries (name, company_id, district_id)
    VALUES ('FUNDO 2', @company_id, @district_id);

    INSERT INTO app.tbl_subsidiaries (name, company_id, district_id)
    VALUES ('FUNDO 3', @company_id, @district_id);
END
ELSE
BEGIN
    RAISERROR('No se pudo insertar subsidiarias: company_id o district_id no encontrado', 16, 1);
END
GO