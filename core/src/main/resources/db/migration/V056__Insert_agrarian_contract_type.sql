INSERT INTO app.tbl_contract_types (public_id, code, name, description, created_at, created_by)
VALUES
    (NEWID(), 'CONT_INDEFINIDO', 'Contrato Indefinido', 'Contrato de trabajo de duración indeterminada.', GETDATE(), 'system'),
    (NEWID(), 'CONT_PLAZO', 'Contrato a Plazo Fijo', 'Contrato de trabajo sujeto a una duración determinada.', GETDATE(), 'system');
