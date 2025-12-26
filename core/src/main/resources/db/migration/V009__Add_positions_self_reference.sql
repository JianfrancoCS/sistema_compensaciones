IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_positions_parent')
    BEGIN
        ALTER TABLE app.tbl_positions
            ADD CONSTRAINT FK_positions_parent
                FOREIGN KEY (parent_position_id) REFERENCES app.tbl_positions(id);
    END
GO