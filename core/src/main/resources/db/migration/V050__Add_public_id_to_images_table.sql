ALTER TABLE [app].[tbl_images]
    ADD public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID();

ALTER TABLE [app].[tbl_images]
    ADD CONSTRAINT UQ_tbl_images_public_id UNIQUE (public_id);
