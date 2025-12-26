IF NOT EXISTS (SELECT 1 FROM app.tbl_departments)
BEGIN
    INSERT INTO app.tbl_departments (name) VALUES
    ('AMAZONAS'),
    ('ANCASH'),
    ('APURIMAC'),
    ('AREQUIPA'),
    ('AYACUCHO'),
    ('CAJAMARCA'),
    ('CALLAO'),
    ('CUSCO'),
    ('HUANCAVELICA'),
    ('HUANUCO'),
    ('ICA'),
    ('JUNIN'),
    ('LA LIBERTAD'),
    ('LAMBAYEQUE'),
    ('LIMA'),
    ('LORETO'),
    ('MADRE DE DIOS'),
    ('MOQUEGUA'),
    ('PASCO'),
    ('PIURA'),
    ('PUNO'),
    ('SAN MARTIN'),
    ('TACNA'),
    ('TUMBES'),
    ('UCAYALI');
END
GO

