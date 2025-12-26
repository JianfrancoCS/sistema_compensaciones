-- =============================================
-- Table: Work Calendar
-- Defines the working and non-working days for the organization.
-- =============================================
CREATE TABLE app.tbl_work_calendar (
    id INT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    date DATE NOT NULL,
    is_working_day BIT NOT NULL DEFAULT 1,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL,
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL
);
GO

-- Unique index for public_id, considering soft delete
CREATE UNIQUE NONCLUSTERED INDEX UQ_work_calendar_public_id_active
    ON app.tbl_work_calendar(public_id)
    WHERE deleted_at IS NULL;
GO

-- Unique index per date, considering soft delete
CREATE UNIQUE NONCLUSTERED INDEX UQ_work_calendar_date_active
    ON app.tbl_work_calendar(date)
    WHERE deleted_at IS NULL;
GO

-- Index for quick lookup of working/non-working days
CREATE NONCLUSTERED INDEX IX_work_calendar_is_working_day
    ON app.tbl_work_calendar(is_working_day, date)
    WHERE deleted_at IS NULL;
GO
