-- =============================================
-- Tabla: Tareos (Cabecera)
-- =============================================
CREATE TABLE app.tbl_tareos (
    id INT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    temporal_id NVARCHAR(255) NOT NULL,

    supervisor_employee_document_number NVARCHAR(15) NOT NULL,
    labor_id SMALLINT NOT NULL,
    lote_id INT NULL, -- NULL para tareos administrativos que no requieren lote
    scanner_employee_document_number NVARCHAR(15) NULL,
    closed_at DATETIME2 NULL, -- Fecha y hora en que se cerró el tareo (cuando se envía el cierre final)

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL,

    CONSTRAINT FK_tareos_supervisor
        FOREIGN KEY (supervisor_employee_document_number)
        REFERENCES app.tbl_employees(person_document_number)
        ON DELETE NO ACTION ON UPDATE NO ACTION,

    CONSTRAINT FK_tareos_labor
        FOREIGN KEY (labor_id) REFERENCES app.tbl_labors(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION,

    CONSTRAINT FK_tareos_lote
        FOREIGN KEY (lote_id) REFERENCES app.tbl_lotes(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION, -- Permite NULL para tareos administrativos

    CONSTRAINT FK_tareos_scanner
        FOREIGN KEY (scanner_employee_document_number)
        REFERENCES app.tbl_employees(person_document_number)
        ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- Índices
CREATE UNIQUE NONCLUSTERED INDEX UQ_tareos_public_id_active
    ON app.tbl_tareos(public_id) WHERE deleted_at IS NULL;

CREATE UNIQUE NONCLUSTERED INDEX UQ_tareos_temporal_id_active
    ON app.tbl_tareos(temporal_id) WHERE deleted_at IS NULL;

CREATE NONCLUSTERED INDEX IX_tareos_supervisor_active
    ON app.tbl_tareos(supervisor_employee_document_number, deleted_at);

CREATE NONCLUSTERED INDEX IX_tareos_labor_active
    ON app.tbl_tareos(labor_id, deleted_at);

CREATE NONCLUSTERED INDEX IX_tareos_lote_active
    ON app.tbl_tareos(lote_id, deleted_at);

CREATE NONCLUSTERED INDEX IX_tareos_scanner_active
    ON app.tbl_tareos(scanner_employee_document_number, deleted_at);

CREATE NONCLUSTERED INDEX IX_tareos_created_date
    ON app.tbl_tareos(created_at) WHERE deleted_at IS NULL;
GO
