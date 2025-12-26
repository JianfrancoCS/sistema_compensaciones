IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'app')
    BEGIN
        EXEC('CREATE SCHEMA app')
    END
GO