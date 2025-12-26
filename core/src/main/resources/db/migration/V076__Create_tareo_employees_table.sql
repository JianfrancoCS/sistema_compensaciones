-- =============================================
-- Tabla: Detalle de Empleados en Tareo
-- =============================================
CREATE TABLE app.tbl_tareo_employees (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),

    tareo_id INT NOT NULL,
    employee_document_number NVARCHAR(15) NOT NULL,

    start_time TIME NULL,
    end_time TIME NULL,

    actual_hours DECIMAL(5, 2) NULL,
    paid_hours DECIMAL(5, 2) NULL,
    productivity INT NULL, -- Total de unidades productivas registradas al finalizar el tareo (solo para labores de destajo)

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL,

    CONSTRAINT FK_tareo_employees_tareo
        FOREIGN KEY (tareo_id) REFERENCES app.tbl_tareos(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION,

    CONSTRAINT FK_tareo_employees_employee
        FOREIGN KEY (employee_document_number)
        REFERENCES app.tbl_employees(person_document_number)
        ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- Índices
CREATE UNIQUE NONCLUSTERED INDEX UQ_tareo_employees_public_id_active
    ON app.tbl_tareo_employees(public_id) WHERE deleted_at IS NULL;

CREATE UNIQUE NONCLUSTERED INDEX UQ_tareo_employees_tareo_employee_active
    ON app.tbl_tareo_employees(tareo_id, employee_document_number)
    WHERE deleted_at IS NULL;

CREATE NONCLUSTERED INDEX IX_tareo_employees_employee_active
    ON app.tbl_tareo_employees(employee_document_number, deleted_at);
GO