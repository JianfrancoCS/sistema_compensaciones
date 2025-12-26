-- =============================================
-- Table: Payroll Concepts
-- =============================================
CREATE TABLE app.tbl_concepts (
    id SMALLINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),

    code NVARCHAR(20) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255) NULL,

    category_id SMALLINT NOT NULL,
    value DECIMAL(10,2) NULL,
    calculation_priority SMALLINT NOT NULL DEFAULT 100,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL,
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL,

    CONSTRAINT FK_concepts_category
        FOREIGN KEY (category_id)
        REFERENCES app.tbl_concept_categories(id)
        ON DELETE NO ACTION
        ON UPDATE CASCADE
);
GO

-- Indexes
CREATE UNIQUE NONCLUSTERED INDEX UQ_concepts_code_active
    ON app.tbl_concepts(code)
    WHERE deleted_at IS NULL;

CREATE UNIQUE NONCLUSTERED INDEX UQ_concepts_public_id_active
    ON app.tbl_concepts(public_id)
    WHERE deleted_at IS NULL;

CREATE NONCLUSTERED INDEX IX_concepts_category
    ON app.tbl_concepts(category_id, deleted_at);
GO
