ALTER TABLE [app].[tbl_contracts] ADD [template_id] [smallint] NULL;

ALTER TABLE [app].[tbl_contracts] DROP COLUMN [variables];

-- Foreign key
ALTER TABLE [app].[tbl_contracts] ADD FOREIGN KEY([template_id]) 
REFERENCES [app].[tbl_contract_templates] ([id]);
