-- =============================================
-- Table: Calendar Event Types
-- Catalog for different types of calendar events (e.g., Holiday, Company Anniversary).
-- =============================================
CREATE TABLE app.tbl_calendar_event_types (
    id SMALLINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    code NVARCHAR(30) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255) NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL,
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL
);
GO

-- Unique index for public_id, considering soft delete
CREATE UNIQUE NONCLUSTERED INDEX UQ_calendar_event_types_public_id_active
    ON app.tbl_calendar_event_types(public_id)
    WHERE deleted_at IS NULL;
GO

-- Unique index for code, considering soft delete
CREATE UNIQUE NONCLUSTERED INDEX UQ_calendar_event_types_code_active
    ON app.tbl_calendar_event_types(code)
    WHERE deleted_at IS NULL;
GO
