-- V033: Creación de la tabla de adendas de contratos en el esquema 'app'
CREATE TABLE app.tbl_addendums (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    addendum_number NVARCHAR(30) NOT NULL,
    contract_id BIGINT NOT NULL,
    addendum_type_id SMALLINT NOT NULL,
    content NVARCHAR(MAX) NOT NULL,
    effective_date DATE NOT NULL,
    new_end_date DATE,
    state_id SMALLINT NOT NULL,
    template_id SMALLINT,
    parent_addendum_id BIGINT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2,
    created_by NVARCHAR(255),
    updated_by NVARCHAR(255),
    deleted_at DATETIME2,
    deleted_by NVARCHAR(255),
    CONSTRAINT FK_addendums_contract FOREIGN KEY (contract_id) REFERENCES app.tbl_contracts(id),
    CONSTRAINT FK_addendums_addendum_type FOREIGN KEY (addendum_type_id) REFERENCES app.tbl_addendum_types(id),
    CONSTRAINT FK_addendums_state FOREIGN KEY (state_id) REFERENCES app.tbl_states(id),
    CONSTRAINT FK_addendums_template FOREIGN KEY (template_id) REFERENCES app.tbl_addendum_templates(id)
);
GO

-- Add self-referencing foreign key after table creation
ALTER TABLE app.tbl_addendums
ADD CONSTRAINT FK_addendums_parent FOREIGN KEY (parent_addendum_id) REFERENCES app.tbl_addendums(id);
GO

CREATE UNIQUE INDEX UQ_addendums_public_id ON app.tbl_addendums(public_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX UQ_addendums_addendum_number ON app.tbl_addendums(addendum_number) WHERE deleted_at IS NULL;
GO
