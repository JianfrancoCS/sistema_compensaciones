ALTER TABLE app.tbl_positions
ADD required_manager_position_id SMALLINT NULL;
GO

ALTER TABLE app.tbl_positions
ADD CONSTRAINT FK_positions_required_manager_position
FOREIGN KEY (required_manager_position_id) REFERENCES app.tbl_positions(id);
GO
