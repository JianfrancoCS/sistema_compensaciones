-- =============================================
-- Tabla: Motivos aplicados a empleados en tareo
-- =============================================
CREATE TABLE app.tbl_tareo_employee_motives (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    tareo_employee_id BIGINT NOT NULL,
    motive_id SMALLINT NOT NULL,

    applied_at TIME NULL,
    observations NVARCHAR(500) NULL,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',

    CONSTRAINT FK_tareo_employee_motives_tareo_employee
        FOREIGN KEY (tareo_employee_id) REFERENCES app.tbl_tareo_employees(id)
        ON DELETE CASCADE ON UPDATE NO ACTION,

    CONSTRAINT FK_tareo_employee_motives_motive
        FOREIGN KEY (motive_id) REFERENCES app.tbl_tareo_motives(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- Índices
CREATE NONCLUSTERED INDEX IX_tareo_employee_motives_tareo_employee
    ON app.tbl_tareo_employee_motives(tareo_employee_id);

CREATE NONCLUSTERED INDEX IX_tareo_employee_motives_motive
    ON app.tbl_tareo_employee_motives(motive_id);
GO
