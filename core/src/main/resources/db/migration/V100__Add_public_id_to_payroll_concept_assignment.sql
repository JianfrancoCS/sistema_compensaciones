-- =============================================
-- Add public_id column to tbl_payroll_concept_assignment for consistency
-- =============================================
ALTER TABLE app.tbl_payroll_concept_assignment
ADD public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID();
GO

-- Create unique index for public_id, considering soft delete
CREATE UNIQUE NONCLUSTERED INDEX UQ_payroll_concept_assignment_public_id_active
    ON app.tbl_payroll_concept_assignment(public_id)
    WHERE deleted_at IS NULL;
GO