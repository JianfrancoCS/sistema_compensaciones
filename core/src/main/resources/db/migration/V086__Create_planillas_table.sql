-- =============================================
-- Table: Payrolls (Header)
-- =============================================
CREATE TABLE app.tbl_payrolls (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),

    code NVARCHAR(30) NOT NULL,
    subsidiary_id SMALLINT NOT NULL,
    payroll_configuration_id BIGINT NOT NULL, -- New column

    year SMALLINT NOT NULL,
    month TINYINT NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    payment_date DATE NULL,

    state_id SMALLINT NOT NULL,
    base_payroll_id BIGINT NULL,
    corrected_payroll_id BIGINT NULL,

    total_employees INT DEFAULT 0,
    total_income DECIMAL(15,2) DEFAULT 0,
    total_deductions DECIMAL(15,2) DEFAULT 0,
    total_net DECIMAL(15,2) DEFAULT 0,

    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME2 NULL,
    updated_by NVARCHAR(100) NULL,
    approved_at DATETIME2 NULL,
    approved_by NVARCHAR(100) NULL,
    deleted_at DATETIME2 NULL,
    deleted_by NVARCHAR(100) NULL,

    CONSTRAINT FK_payrolls_subsidiary
        FOREIGN KEY (subsidiary_id)
        REFERENCES app.tbl_subsidiaries(id)
        ON DELETE NO ACTION
        ON UPDATE CASCADE,

    CONSTRAINT FK_payrolls_state
        FOREIGN KEY (state_id)
        REFERENCES app.tbl_states(id)
        ON DELETE NO ACTION
        ON UPDATE CASCADE,

    CONSTRAINT FK_payrolls_base
        FOREIGN KEY (base_payroll_id)
        REFERENCES app.tbl_payrolls(id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,

    CONSTRAINT FK_payrolls_corrected
        FOREIGN KEY (corrected_payroll_id)
        REFERENCES app.tbl_payrolls(id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION

    -- FK to payroll_configuration will be added in V098 after tbl_payroll_configuration exists
);
GO

-- Indexes
CREATE UNIQUE NONCLUSTERED INDEX UQ_payrolls_code_active
    ON app.tbl_payrolls(code)
    WHERE deleted_at IS NULL;

CREATE UNIQUE NONCLUSTERED INDEX UQ_payrolls_public_id_active
    ON app.tbl_payrolls(public_id)
    WHERE deleted_at IS NULL;

CREATE NONCLUSTERED INDEX IX_payrolls_subsidiary_period
    ON app.tbl_payrolls(subsidiary_id, year, month, deleted_at);

CREATE NONCLUSTERED INDEX IX_payrolls_state
    ON app.tbl_payrolls(state_id, deleted_at);

CREATE NONCLUSTERED INDEX IX_payrolls_base
    ON app.tbl_payrolls(base_payroll_id)
    WHERE base_payroll_id IS NOT NULL;

CREATE NONCLUSTERED INDEX IX_payrolls_corrected
    ON app.tbl_payrolls(corrected_payroll_id)
    WHERE corrected_payroll_id IS NOT NULL;

CREATE NONCLUSTERED INDEX IX_payrolls_payroll_configuration -- New Index
    ON app.tbl_payrolls(payroll_configuration_id);
GO
