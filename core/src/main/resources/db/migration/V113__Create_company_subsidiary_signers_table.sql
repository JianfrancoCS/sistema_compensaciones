-- =============================================
-- V113: CREAR TABLA HISTÓRICA DE RESPONSABLES DE FIRMA
-- =============================================
-- Tabla histórica para responsables de firma de boletas de pago
-- Permite tener diferentes responsables por subsidiaria
-- El último registro (más reciente) será el que se use para la boleta
-- Si subsidiary_id es NULL, el responsable es a nivel de empresa

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_company_subsidiary_signers' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_company_subsidiary_signers (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),

        -- Relación con empresa (obligatorio)
        company_id BIGINT NOT NULL,

        -- Relación con subsidiaria (opcional: NULL = responsable a nivel de empresa)
        subsidiary_id SMALLINT NULL,

        -- Responsable de firma (empleado)
        responsible_employee_document_number NVARCHAR(15) NOT NULL,

        -- Cargo del responsable (para mostrar en la boleta)
        responsible_position NVARCHAR(100) NOT NULL DEFAULT 'JEFE DE RECURSOS HUMANOS',

        -- Metadatos históricos
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        
        -- Notas opcionales
        notes NVARCHAR(500) NULL
    );

    -- Índices
    CREATE NONCLUSTERED INDEX IX_company_subsidiary_signers_company_subsidiary
        ON app.tbl_company_subsidiary_signers(company_id, subsidiary_id, created_at DESC);

    CREATE NONCLUSTERED INDEX IX_company_subsidiary_signers_public_id
        ON app.tbl_company_subsidiary_signers(public_id);

    CREATE NONCLUSTERED INDEX IX_company_subsidiary_signers_employee
        ON app.tbl_company_subsidiary_signers(responsible_employee_document_number);

    -- Foreign keys
    -- Nota: Usamos ON UPDATE NO ACTION para evitar ciclos de cascada con tbl_subsidiaries -> tbl_companies
    ALTER TABLE app.tbl_company_subsidiary_signers
    ADD CONSTRAINT FK_company_subsidiary_signers_company
        FOREIGN KEY (company_id)
        REFERENCES app.tbl_companies(id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

    ALTER TABLE app.tbl_company_subsidiary_signers
    ADD CONSTRAINT FK_company_subsidiary_signers_subsidiary
        FOREIGN KEY (subsidiary_id)
        REFERENCES app.tbl_subsidiaries(id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

    ALTER TABLE app.tbl_company_subsidiary_signers
    ADD CONSTRAINT FK_company_subsidiary_signers_employee
        FOREIGN KEY (responsible_employee_document_number)
        REFERENCES app.tbl_employees(person_document_number)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;
END
GO

