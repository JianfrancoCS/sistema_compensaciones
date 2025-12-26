-- V060: Add is_internal field to marking reasons table to differentiate internal vs external marking types
ALTER TABLE app.tbl_marking_reasons
ADD is_internal BIT NOT NULL DEFAULT 0;
GO

-- Update existing records: WORK is internal, others are external
UPDATE app.tbl_marking_reasons
SET is_internal = 1
WHERE code = 'WORK';

UPDATE app.tbl_marking_reasons
SET is_internal = 0
WHERE code IN ('CONTRACT_SIGNING', 'VISIT', 'SUPERVISION');
GO