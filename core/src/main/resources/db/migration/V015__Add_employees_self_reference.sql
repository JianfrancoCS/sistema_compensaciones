-- V015: Add self-reference for manager in employees table (Corrected)
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_employees_reports_to')
BEGIN
    ALTER TABLE app.tbl_employees
    ADD CONSTRAINT FK_employees_reports_to
    FOREIGN KEY (reports_to_employee_document_number) REFERENCES app.tbl_employees(person_document_number); -- Corrected referenced column name
END
GO
