-- V061: Add is_employee field to marking details table to differentiate employee vs person markings
ALTER TABLE app.tbl_marking_details
ADD is_employee BIT NOT NULL DEFAULT 0;
GO

-- Create index for employee markings filtering
CREATE NONCLUSTERED INDEX IX_marking_details_employee_date
ON app.tbl_marking_details(is_employee, marked_at)
WHERE deleted_at IS NULL;
GO