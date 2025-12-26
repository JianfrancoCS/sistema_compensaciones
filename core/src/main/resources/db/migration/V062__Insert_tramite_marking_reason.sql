-- V062: Insert TRAMITE marking reason for internal employees
-- Adding TRAMITE as an additional internal marking reason for employees

INSERT INTO app.tbl_marking_reasons (public_id, code, name, is_internal, created_at, created_by)
VALUES (
    NEWID(),
    'TRAMITE',
    'Trámite',
    1, -- is_internal = true (para empleados)
    GETDATE(),
    'SYSTEM'
);
GO