IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'event_publication' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.event_publication (
        identifier UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        publication_date DATETIME2 NOT NULL,
        listener_id NVARCHAR(255) NOT NULL,
        event_type NVARCHAR(255) NOT NULL,
        serialized_event NVARCHAR(MAX) NOT NULL
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'completed_event_publication' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.completed_event_publication (
        identifier UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        publication_date DATETIME2 NOT NULL,
        listener_id NVARCHAR(255) NOT NULL,
        completion_date DATETIME2 NOT NULL
    );
END
GO
