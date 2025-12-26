-- =============================================
-- Table: Payroll Detail (per Employee)
-- =============================================
CREATE TABLE app.tbl_payroll_details (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),

    payroll_id BIGINT NOT NULL,
    employee_document_number NVARCHAR(15) NOT NULL,

    calculated_concepts NVARCHAR(MAX) NOT NULL,
    daily_detail NVARCHAR(MAX) NULL,

    total_income DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_deductions DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_employer_contributions DECIMAL(12,2) NOT NULL DEFAULT 0,
    net_to_pay DECIMAL(12,2) NOT NULL DEFAULT 0,

    days_worked TINYINT NULL,
    normal_hours DECIMAL(8,2) NULL,
    overtime_hours_25 DECIMAL(8,2) NULL,
    overtime_hours_100 DECIMAL(8,2) NULL,
    total_hours DECIMAL(8,2) NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL,
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL,

    CONSTRAINT FK_payroll_details_payroll
        FOREIGN KEY (payroll_id)
        REFERENCES app.tbl_payrolls(id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,

    CONSTRAINT FK_payroll_details_employee
        FOREIGN KEY (employee_document_number)
        REFERENCES app.tbl_employees(person_document_number)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION
);
GO

-- Indexes
CREATE UNIQUE NONCLUSTERED INDEX UQ_payroll_details_payroll_employee
    ON app.tbl_payroll_details(payroll_id, employee_document_number);

CREATE UNIQUE NONCLUSTERED INDEX UQ_payroll_details_public_id
    ON app.tbl_payroll_details(public_id);

CREATE NONCLUSTERED INDEX IX_payroll_details_employee
    ON app.tbl_payroll_details(employee_document_number);
GO
