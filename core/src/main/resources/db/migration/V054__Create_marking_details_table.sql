-- V054: Create marking details table (individual person markings)
CREATE TABLE app.tbl_marking_details (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    marking_id BIGINT NOT NULL,
    marking_reason_id SMALLINT NOT NULL,
    person_document_number NVARCHAR(15) NOT NULL,
    is_entry BIT NOT NULL,
    marked_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2,
    created_by NVARCHAR(255),
    updated_by NVARCHAR(255),
    deleted_at DATETIME2,
    deleted_by NVARCHAR(255),

    CONSTRAINT FK_marking_details_marking FOREIGN KEY (marking_id)
        REFERENCES app.tbl_markings(id),
    CONSTRAINT FK_marking_details_reason FOREIGN KEY (marking_reason_id)
        REFERENCES app.tbl_marking_reasons(id),
    CONSTRAINT FK_marking_details_person FOREIGN KEY (person_document_number)
        REFERENCES app.tbl_persons(document_number)
);
GO

CREATE UNIQUE INDEX UQ_marking_details_public_id ON app.tbl_marking_details(public_id) WHERE deleted_at IS NULL;
CREATE NONCLUSTERED INDEX IX_marking_details_marking ON app.tbl_marking_details(marking_id) WHERE deleted_at IS NULL;
CREATE NONCLUSTERED INDEX IX_marking_details_person_date ON app.tbl_marking_details(person_document_number, marked_at) WHERE deleted_at IS NULL;
CREATE NONCLUSTERED INDEX IX_marking_details_entry_date ON app.tbl_marking_details(is_entry, marked_at) WHERE deleted_at IS NULL;
GO