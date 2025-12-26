IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_companies' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_companies(
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        legal_name NVARCHAR(150) NOT NULL,
        trade_name NVARCHAR(150) NOT NULL,
        ruc NVARCHAR(20) NOT NULL,
        company_type NVARCHAR(50) NOT NULL,

        -- Payroll Configuration
        payroll_payment_interval INT NOT NULL DEFAULT 30, -- Interval in days for the payroll period
        payroll_declaration_day TINYINT NOT NULL DEFAULT 28, -- Day of the month the payroll is declared
        payroll_anticipation_days TINYINT NOT NULL DEFAULT 3, -- Days before declaration the payroll period should close

        max_monthly_working_hours INT NULL,
        overtime_rate DECIMAL(5,2) NULL,
        daily_normal_hours DECIMAL(4,2) NULL DEFAULT 8.00,
        month_calculation_days INT NULL DEFAULT 30,
        rmv DECIMAL(10,2) NULL DEFAULT 1025.00, -- Remuneración Mínima Vital (RMV) de Perú 2025

        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL
    );

    -- Índices únicos solo para registros activos
    CREATE UNIQUE NONCLUSTERED INDEX UQ_tbl_companies_ruc_active
    ON app.tbl_companies(ruc)
    WHERE deleted_at IS NULL;

    CREATE UNIQUE NONCLUSTERED INDEX UQ_tbl_companies_trade_name_active
    ON app.tbl_companies(trade_name)
    WHERE deleted_at IS NULL;

    CREATE NONCLUSTERED INDEX IX_tbl_companies_public_id ON app.tbl_companies(public_id);
END
GO

INSERT INTO app.tbl_companies (
    legal_name,
    trade_name,
    ruc,
    company_type,
    payroll_payment_interval,       -- Intervalo de pago en días (Ej: 30 días)
    payroll_declaration_day,        -- Día de declaración de la nómina (Ej: día 28)
    payroll_anticipation_days,      -- Días antes de la declaración para el cierre de la nómina
    daily_normal_hours,             -- Horas normales de trabajo diarias (DEFAULT 8.00)
    overtime_rate,                  -- Tasa de sobretiempo (Ej: 0.25 para el 25%)
    rmv                             -- Remuneración Mínima Vital (RMV) de Perú 2025
)
VALUES (
           'INKA''S BERRIES S.A.C.',
           'INKAS BERRIES SAC',
           '20520866630',
           'SOCIEDAD ANONIMA CERRADA',
           30,                             -- Intervalo de pago: 30 días
           28,                             -- Día de declaración: 28
           3,                              -- Días de anticipación al cierre: 3
           8.00,                           -- Horas normales diarias: 8.00
           0.25,                           -- Tasa de horas extras (25% para las primeras 2 horas)
           1025.00                         -- RMV 2025 (S/. 1,025.00)
       );
GO

