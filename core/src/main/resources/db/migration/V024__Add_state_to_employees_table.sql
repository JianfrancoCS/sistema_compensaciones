ALTER TABLE app.tbl_employees
ADD state_id SMALLINT;
GO

ALTER TABLE app.tbl_employees
ADD CONSTRAINT FK_tbl_employees_tbl_states
FOREIGN KEY (state_id)
REFERENCES app.tbl_states(id)
GO

CREATE NONCLUSTERED INDEX IX_tbl_employees_state_id ON app.tbl_employees(state_id);
GO
