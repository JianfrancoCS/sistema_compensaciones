-- =============================================
-- Table: Calendar Events
-- Associates specific dates from the work calendar with an event type.
-- =============================================
CREATE TABLE app.tbl_calendar_events (
    id INT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    work_calendar_id INT NOT NULL,
    event_type_id SMALLINT NOT NULL,
    description NVARCHAR(255) NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL,
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL,

    CONSTRAINT FK_calendar_events_work_calendar
        FOREIGN KEY (work_calendar_id)
        REFERENCES app.tbl_work_calendar(id)
        ON DELETE CASCADE,

    CONSTRAINT FK_calendar_events_event_type
        FOREIGN KEY (event_type_id)
        REFERENCES app.tbl_calendar_event_types(id)
        ON DELETE NO ACTION
);
GO

-- Unique index for public_id, considering soft delete
CREATE UNIQUE NONCLUSTERED INDEX UQ_calendar_events_public_id_active
    ON app.tbl_calendar_events(public_id)
    WHERE deleted_at IS NULL;
GO

-- Unique index to prevent the same event type on the same day, considering soft delete
CREATE UNIQUE NONCLUSTERED INDEX UQ_calendar_events_work_calendar_event_type_active
    ON app.tbl_calendar_events(work_calendar_id, event_type_id)
    WHERE deleted_at IS NULL;
GO

-- Index for quick lookup of events by type
CREATE NONCLUSTERED INDEX IX_calendar_events_event_type
    ON app.tbl_calendar_events(event_type_id)
    WHERE deleted_at IS NULL;
GO
