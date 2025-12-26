-- Add dynamic_variable_id column to tbl_variables to link with validation system
IF NOT EXISTS (SELECT * FROM sys.columns
               WHERE object_id = OBJECT_ID('app.tbl_variables')
               AND name = 'dynamic_variable_id')
BEGIN
    ALTER TABLE app.tbl_variables
    ADD dynamic_variable_id SMALLINT NULL;

    -- Add foreign key constraint
    ALTER TABLE app.tbl_variables
    ADD CONSTRAINT FK_variables_dynamic_variable
        FOREIGN KEY (dynamic_variable_id)
        REFERENCES app.tbl_dynamic_variables(id);

    -- Add index for performance
    CREATE NONCLUSTERED INDEX IX_variables_dynamic_variable_id
    ON app.tbl_variables(dynamic_variable_id);
END
GO