-- =============================================
-- Tabla: Asignación de Rollos QR a Empleados por Día
-- =============================================
CREATE TABLE app.tbl_qr_roll_employees (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),

    qr_roll_id INT NOT NULL,
    employee_document_number NVARCHAR(15) NOT NULL,
    assigned_date DATE NOT NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
    updated_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL,

    CONSTRAINT FK_qr_roll_employees_qr_roll
        FOREIGN KEY (qr_roll_id) REFERENCES app.tbl_qr_rolls(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION,

    CONSTRAINT FK_qr_roll_employees_employee
        FOREIGN KEY (employee_document_number)
        REFERENCES app.tbl_employees(person_document_number)
        ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- Índices
CREATE UNIQUE NONCLUSTERED INDEX UQ_qr_roll_employees_public_id_active
    ON app.tbl_qr_roll_employees(public_id) WHERE deleted_at IS NULL;

-- Un empleado solo puede tener un rollo asignado por día
CREATE UNIQUE NONCLUSTERED INDEX UQ_qr_roll_employees_employee_date_active
    ON app.tbl_qr_roll_employees(employee_document_number, assigned_date)
    WHERE deleted_at IS NULL;

CREATE NONCLUSTERED INDEX IX_qr_roll_employees_roll
    ON app.tbl_qr_roll_employees(qr_roll_id, deleted_at);

CREATE NONCLUSTERED INDEX IX_qr_roll_employees_date
    ON app.tbl_qr_roll_employees(assigned_date, deleted_at);
GO
