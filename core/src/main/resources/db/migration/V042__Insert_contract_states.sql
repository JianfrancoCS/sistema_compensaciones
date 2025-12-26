IF NOT EXISTS (SELECT 1 FROM app.tbl_domains WHERE name = 'tbl_contracts')
BEGIN
    INSERT INTO app.tbl_domains (name) VALUES ('tbl_contracts');
END
GO

DECLARE @domainId SMALLINT;

SELECT @domainId = id FROM app.tbl_domains WHERE name = 'tbl_contracts';

IF @domainId IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND name = 'BORRADOR')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default) VALUES (@domainId, 'BORRADOR', 'CONTRACT_DRAFT', 1);
    END
    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND name = 'FIRMADO')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default) VALUES (@domainId, 'FIRMADO', 'CONTRACT_SIGNED', 0);
    END
    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND name = 'ANULADO')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default) VALUES (@domainId, 'ANULADO', 'CONTRACT_CANCELLED', 0);
    END
    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND name = 'VENCIDO')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default) VALUES (@domainId, 'VENCIDO', 'CONTRACT_EXPIRED', 0);
    END
END
GO
