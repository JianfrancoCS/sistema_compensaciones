CREATE TABLE app.tbl_markings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    subsidiary_id SMALLINT NOT NULL,
    marking_date DATE NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2,
    created_by NVARCHAR(255),
    updated_by NVARCHAR(255),
    deleted_at DATETIME2,
    deleted_by NVARCHAR(255),

    CONSTRAINT FK_markings_subsidiary FOREIGN KEY (subsidiary_id)
        REFERENCES app.tbl_subsidiaries(id)
);
GO

CREATE UNIQUE INDEX UQ_markings_public_id ON app.tbl_markings(public_id) WHERE deleted_at IS NULL;
CREATE NONCLUSTERED INDEX IX_markings_subsidiary_date ON app.tbl_markings(subsidiary_id, marking_date) WHERE deleted_at IS NULL;
CREATE NONCLUSTERED INDEX IX_markings_date ON app.tbl_markings(marking_date) WHERE deleted_at IS NULL;
GO