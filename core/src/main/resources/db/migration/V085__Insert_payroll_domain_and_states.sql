-- =============================================
-- Domain and States for Payrolls
-- =============================================

-- Create domain for payrolls
IF NOT EXISTS (SELECT 1 FROM app.tbl_domains WHERE name = 'tbl_payrolls')
BEGIN
    INSERT INTO app.tbl_domains (name) VALUES ('tbl_payrolls');
END
GO

DECLARE @domainId SMALLINT;
SELECT @domainId = id FROM app.tbl_domains WHERE name = 'tbl_payrolls';

IF @domainId IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND code = 'PAYROLL_DRAFT')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default)
        VALUES (@domainId, 'DRAFT', 'PAYROLL_DRAFT', 1);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND code = 'PAYROLL_IN_PROGRESS')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default)
        VALUES (@domainId, 'IN PROGRESS', 'PAYROLL_IN_PROGRESS', 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND code = 'PAYROLL_CALCULATED')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default)
        VALUES (@domainId, 'CALCULATED', 'PAYROLL_CALCULATED', 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND code = 'PAYROLL_APPROVED')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default)
        VALUES (@domainId, 'APPROVED', 'PAYROLL_APPROVED', 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND code = 'PAYROLL_PAID')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default)
        VALUES (@domainId, 'PAID', 'PAYROLL_PAID', 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND code = 'PAYROLL_CANCELLED')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default)
        VALUES (@domainId, 'CANCELLED', 'PAYROLL_CANCELLED', 0);
    END

    IF NOT EXISTS (SELECT 1 FROM app.tbl_states WHERE domain_id = @domainId AND code = 'PAYROLL_CANCELLED_CORRECTION')
    BEGIN
        INSERT INTO app.tbl_states (domain_id, name, code, is_default)
        VALUES (@domainId, 'CANCELLED FOR CORRECTION', 'PAYROLL_CANCELLED_CORRECTION', 0);
    END
END
GO