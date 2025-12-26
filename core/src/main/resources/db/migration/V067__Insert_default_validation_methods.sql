-- Insert default validation methods
INSERT INTO app.tbl_validation_methods (public_id, code, name, regex_pattern, method_type, requires_value, description) VALUES
(NEWID(), 'ONLY_NUMBERS', 'solo_numeros', '^\d+$', 'REGEX', 0, 'Solo acepta números'),
(NEWID(), 'ONLY_LETTERS', 'solo_letras', '^[a-zA-Z\s]+$', 'REGEX', 0, 'Solo acepta letras y espacios'),
(NEWID(), 'ALPHANUMERIC', 'alfanumerico', '^[a-zA-Z0-9]+$', 'REGEX', 0, 'Letras y números'),
(NEWID(), 'EMAIL_FORMAT', 'email_formato', '^[\w\-\.]+@([\w\-]+\.)+[\w\-]{2,4}$', 'REGEX', 0, 'Formato de email'),
(NEWID(), 'EXACT_LENGTH', 'longitud_exacta', '^.{{{VALUE}}}$', 'LENGTH', 1, 'Longitud exacta de caracteres'),
(NEWID(), 'MIN_LENGTH', 'longitud_minima', '^.{{{VALUE}},}$', 'LENGTH', 1, 'Longitud mínima de caracteres'),
(NEWID(), 'LENGTH_RANGE', 'longitud_rango', '^.{{{VALUE_MIN}},{{VALUE_MAX}}}$', 'LENGTH', 1, 'Longitud entre min y max'),
(NEWID(), 'GREATER_THAN', 'mayor_que', NULL, 'COMPARISON', 1, 'Valor numérico mayor que X'),
(NEWID(), 'LESS_THAN', 'menor_que', NULL, 'COMPARISON', 1, 'Valor numérico menor que X'),
(NEWID(), 'CONTAINS_UPPERCASE', 'contiene_mayuscula', '(?=.*[A-Z])', 'REGEX', 0, 'Debe contener al menos una mayúscula');
GO