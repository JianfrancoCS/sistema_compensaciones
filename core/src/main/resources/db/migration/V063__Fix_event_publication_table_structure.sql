-- Fix Spring Modulith event_publication table structure
-- Add missing completion_date column and rename identifier to id

-- Add the missing completion_date column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.event_publication') AND name = 'completion_date')
BEGIN
    ALTER TABLE app.event_publication ADD completion_date DATETIME2 NULL;
END
GO

-- Rename identifier column to id (Spring Modulith expects 'id')
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.event_publication') AND name = 'identifier')
AND NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.event_publication') AND name = 'id')
BEGIN
    EXEC sp_rename 'app.event_publication.identifier', 'id', 'COLUMN';
END
GO

-- Create indexes for better performance
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('app.event_publication') AND name = 'IX_event_publication_completion_date')
BEGIN
    CREATE INDEX IX_event_publication_completion_date ON app.event_publication (completion_date);
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('app.event_publication') AND name = 'IX_event_publication_publication_date')
BEGIN
    CREATE INDEX IX_event_publication_publication_date ON app.event_publication (publication_date);
END
GO