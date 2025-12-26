ALTER TABLE app.tbl_contract_templates ADD code NVARCHAR(20);
GO

CREATE UNIQUE INDEX UQ_contract_templates_code ON app.tbl_contract_templates(code) WHERE deleted_at IS NULL;
GO