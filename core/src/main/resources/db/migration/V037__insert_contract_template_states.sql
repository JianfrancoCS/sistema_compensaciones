INSERT INTO app.tbl_domains (name, created_by) VALUES ('tbl_contract_templates', 'SYSTEM');
GO

DECLARE @domain_id SMALLINT;
SELECT @domain_id = id FROM app.tbl_domains WHERE name = 'tbl_contract_templates';

INSERT INTO app.tbl_states (domain_id, name, code, is_default, created_by) VALUES
(@domain_id,  'BORRADOR', 'TEMPLATE_DRAFT', 1, 'SYSTEM'),
(@domain_id,  'PUBLICADO', 'TEMPLATE_PUBLISHED', 0, 'SYSTEM'),
(@domain_id,  'ARCHIVADO', 'TEMPLATE_ARCHIVED', 0, 'SYSTEM');
GO