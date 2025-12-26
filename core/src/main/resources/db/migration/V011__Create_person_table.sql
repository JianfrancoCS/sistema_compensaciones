IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_persons' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_persons(
        document_number NVARCHAR(15) PRIMARY KEY,
        names NVARCHAR(100) NOT NULL,
        paternal_lastname NVARCHAR(50) NOT NULL,
        maternal_lastname NVARCHAR(50) NOT NULL,
        dob DATE NOT NULL,
        gender NCHAR(1) NULL,
        district_id INT  NULL,
        person_parent_document_number NVARCHAR(15) NULL,

        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,

        CONSTRAINT FK_persons_districts
        FOREIGN KEY (district_id)
        REFERENCES app.tbl_districts(id)
        ON DELETE NO ACTION
        ON UPDATE CASCADE,

        CONSTRAINT FK_persons_parent
        FOREIGN KEY (person_parent_document_number)
        REFERENCES app.tbl_persons(document_number)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION
    );

    CREATE UNIQUE NONCLUSTERED INDEX UQ_tbl_persons_document_number_active
    ON app.tbl_persons(document_number)
    WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_persons_parent
    ON app.tbl_persons(person_parent_document_number)
    WHERE person_parent_document_number IS NOT NULL;
END
GO