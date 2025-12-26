
IF NOT EXISTS (SELECT 1 FROM app.tbl_domains WHERE name = 'tbl_employees')
    BEGIN
        INSERT INTO app.tbl_domains (name) VALUES ('tbl_employees');
    END
GO

DECLARE @domainId SMALLINT;

SELECT @domainId = id FROM app.tbl_domains WHERE name = 'tbl_employees';

IF @domainId IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND name = 'CREADO')
            BEGIN
                INSERT INTO app.tbl_states (domain_id, name, code, is_default) VALUES (@domainId, 'CREADO', 'EMPLOYEE_CREATE', 1);
            END
        IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND name = 'ACTIVO')
            BEGIN
                INSERT INTO app.tbl_states (domain_id, name, code, is_default) VALUES (@domainId, 'ACTIVO', 'EMPLOYEE_ACTIVE', 0);
            END
        IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND name = 'INACTIVO')
            BEGIN
                INSERT INTO app.tbl_states (domain_id, name, code, is_default) VALUES (@domainId, 'INACTIVO', 'EMPLOYEE_INACTIVE', 0);
            END
    END
GO
