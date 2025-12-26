CREATE TABLE [app].[tbl_images] (
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [imageable_id] [bigint] NOT NULL,
    [imageable_type] [varchar](255) NOT NULL,
    [url] [varchar](255) NOT NULL,
    [created_at] [datetime2](7) NULL,
    [updated_at] [datetime2](7) NULL,
    [created_by] [varchar](255) NULL,
    [updated_by] [varchar](255) NULL,
    [deleted_at] [datetime2](7) NULL,
    [deleted_by] [varchar](255) NULL,
    PRIMARY KEY ([id])
);
GO

CREATE INDEX IX_images_imageable ON [app].[tbl_images] ([imageable_id], [imageable_type]);
GO
