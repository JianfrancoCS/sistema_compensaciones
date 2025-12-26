-- Add extended_end_date column to contracts table for duration addendums
ALTER TABLE app.tbl_contracts
ADD extended_end_date DATE NULL;

-- Add employee_document_number column to addendums table for direct employee relation
ALTER TABLE app.tbl_addendums
ADD employee_document_number NVARCHAR(15) NULL;

-- Add foreign key constraint separately
ALTER TABLE app.tbl_addendums
ADD CONSTRAINT FK_addendums_employee FOREIGN KEY (employee_document_number)
    REFERENCES app.tbl_employees(person_document_number);