-- =============================================
-- V108: CREAR TABLA DE HISTORIAL DE BOLETAS DE PAGO
-- =============================================
-- Tabla para mantener historial de versiones de boletas de pago
-- Permite auditoría y recuperación de versiones anteriores
-- La última versión (version_number más alto) es la que vale

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_payroll_details_history' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_payroll_details_history (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),

        -- Referencia al detalle original
        payroll_detail_id BIGINT NOT NULL,
        payroll_detail_public_id UNIQUEIDENTIFIER NOT NULL,

        -- Versión
        version_number INT NOT NULL DEFAULT 1,

        -- Datos históricos (snapshot completo)
        calculated_concepts NVARCHAR(MAX) NOT NULL,
        daily_detail NVARCHAR(MAX) NULL,

        total_income DECIMAL(12,2) NOT NULL DEFAULT 0,
        total_deductions DECIMAL(12,2) NOT NULL DEFAULT 0,
        total_employer_contributions DECIMAL(12,2) NOT NULL DEFAULT 0,
        net_to_pay DECIMAL(12,2) NOT NULL DEFAULT 0,

        days_worked TINYINT NULL,
        normal_hours DECIMAL(8,2) NULL,
        overtime_hours_25 DECIMAL(8,2) NULL,
        overtime_hours_35 DECIMAL(8,2) NULL,
        overtime_hours_100 DECIMAL(8,2) NULL,
        night_hours DECIMAL(8,2) NULL,
        total_hours DECIMAL(8,2) NULL,

        -- Metadatos de la versión
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        change_reason NVARCHAR(500) NULL -- Razón del cambio (opcional)
    );

    -- Índices
    CREATE NONCLUSTERED INDEX IX_payroll_details_history_payroll_detail
        ON app.tbl_payroll_details_history(payroll_detail_id, version_number DESC);

    CREATE NONCLUSTERED INDEX IX_payroll_details_history_public_id
        ON app.tbl_payroll_details_history(public_id);

    -- Foreign key (sin CASCADE para preservar historial)
    ALTER TABLE app.tbl_payroll_details_history
    ADD CONSTRAINT FK_payroll_details_history_payroll_detail
        FOREIGN KEY (payroll_detail_id)
        REFERENCES app.tbl_payroll_details(id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;
END
GO

