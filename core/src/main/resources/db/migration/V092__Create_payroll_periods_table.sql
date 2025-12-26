-- =============================================
-- Table: Payroll Periods
-- Stores the calculated payroll periods with their respective dates.
-- =============================================
CREATE TABLE app.tbl_payroll_periods (
    id INT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),

    year SMALLINT NOT NULL,
    month TINYINT NOT NULL,

    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    declaration_date DATE NOT NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL,
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL
);
GO

-- Unique index for public_id, considering soft delete
CREATE UNIQUE NONCLUSTERED INDEX UQ_payroll_periods_public_id_active
    ON app.tbl_payroll_periods(public_id)
    WHERE deleted_at IS NULL;
GO

-- Unique index for year and month, to prevent duplicate periods, considering soft delete
CREATE UNIQUE NONCLUSTERED INDEX UQ_payroll_periods_year_month_active
    ON app.tbl_payroll_periods(year, month)
    WHERE deleted_at IS NULL;
GO
