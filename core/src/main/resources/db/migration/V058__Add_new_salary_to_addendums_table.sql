ALTER TABLE app.tbl_addendums
ADD new_salary DECIMAL(10, 2) NULL
CONSTRAINT CHK_addendums_new_salary_positive CHECK (new_salary IS NULL OR new_salary > 0);