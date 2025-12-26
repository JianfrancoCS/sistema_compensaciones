ALTER TABLE [app].[tbl_contracts] ADD [subsidiary_id] [SMALLINT] NULL;
ALTER TABLE [app].[tbl_contracts] ADD [position_id] [SMALLINT] NULL;
GO

ALTER TABLE [app].[tbl_contracts] ADD CONSTRAINT [FK_contracts_subsidiary] FOREIGN KEY([subsidiary_id]) REFERENCES [app].[tbl_subsidiaries] ([id]);
ALTER TABLE [app].[tbl_contracts] ADD CONSTRAINT [FK_contracts_position] FOREIGN KEY([position_id]) REFERENCES [app].[tbl_positions] ([id]);
GO
