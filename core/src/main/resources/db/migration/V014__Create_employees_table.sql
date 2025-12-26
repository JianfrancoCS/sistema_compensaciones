IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_employees' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_employees(
        person_document_number NVARCHAR(15) NOT NULL PRIMARY KEY, -- Renamed from documentNumber
        code UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        subsidiary_id SMALLINT NOT NULL,
        position_id SMALLINT NOT NULL,
        reports_to_employee_document_number NVARCHAR(15) NULL, -- Renamed from reports_to_employee_documentNumber
        retirement_concept_id SMALLINT NULL,
        health_insurance_concept_id SMALLINT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        CONSTRAINT FK_employees_person
        FOREIGN KEY (person_document_number) REFERENCES app.tbl_persons(document_number), -- Corrected foreign key reference

        CONSTRAINT FK_employees_subsidiary
        FOREIGN KEY (subsidiary_id) REFERENCES app.tbl_subsidiaries(id),

        CONSTRAINT FK_employees_position
        FOREIGN KEY (position_id) REFERENCES app.tbl_positions(id)
    );

    -- Crear índices
    CREATE UNIQUE NONCLUSTERED INDEX UQ_employees_code_active
    ON app.tbl_employees(code) WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_employees_subsidiary_active
    ON app.tbl_employees(subsidiary_id, deleted_at);

    CREATE NONCLUSTERED INDEX IX_employees_position_active
    ON app.tbl_employees(position_id, deleted_at);

    CREATE NONCLUSTERED INDEX IX_employees_reports_to_active
    ON app.tbl_employees(reports_to_employee_document_number, deleted_at);
END
GO