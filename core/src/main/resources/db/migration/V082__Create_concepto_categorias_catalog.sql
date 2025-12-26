-- =============================================
-- Table: Concept Categories
-- =============================================
CREATE TABLE app.tbl_concept_categories (
    id SMALLINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    code NVARCHAR(30) NOT NULL,
    name NVARCHAR(50) NOT NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL,
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL
);
GO

CREATE UNIQUE NONCLUSTERED INDEX UQ_concept_categories_public_id_active
    ON app.tbl_concept_categories(public_id)
    WHERE deleted_at IS NULL;
GO

CREATE UNIQUE NONCLUSTERED INDEX UQ_concept_categories_code_active
    ON app.tbl_concept_categories(code)
    WHERE deleted_at IS NULL;
GO

-- =============================================
-- Insert base categories
-- =============================================
INSERT INTO app.tbl_concept_categories (code, name)
VALUES
    ('INCOME', 'Income'),
    ('DEDUCTION', 'Deductions'),
    ('RETIREMENT', 'Retirement'),
    ('EMPLOYEE_CONTRIBUTION', 'Employee Contributions'),
    ('EMPLOYER_CONTRIBUTION', 'Employer Contributions');
GO