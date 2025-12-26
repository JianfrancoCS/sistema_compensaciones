-- V061: Insertar estados para adendas
IF NOT EXISTS (SELECT 1 FROM app.tbl_domains WHERE name = 'tbl_addendums')
BEGIN
    INSERT INTO app.tbl_domains (name) VALUES ('tbl_addendums');
END
GO

DECLARE @domainId SMALLINT;

SELECT @domainId = id FROM app.tbl_domains WHERE name = 'tbl_addendums';

IF @domainId IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND name = 'BORRADOR')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default) VALUES (@domainId, 'BORRADOR', 'ADDENDUM_DRAFT', 1);
    END
    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND name = 'FIRMADO')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default) VALUES (@domainId, 'FIRMADO', 'ADDENDUM_SIGNED', 0);
    END
    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND name = 'ANULADO')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default) VALUES (@domainId, 'ANULADO', 'ADDENDUM_CANCELLED', 0);
    END
    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND name = 'VENCIDO')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default) VALUES (@domainId, 'VENCIDO', 'ADDENDUM_EXPIRED', 0);
    END
END
GO