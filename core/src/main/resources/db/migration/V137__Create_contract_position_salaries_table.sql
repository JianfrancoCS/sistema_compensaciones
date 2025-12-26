-- =============================================
-- V137: CREAR TABLA N a N PARA SALARIOS ESPECIALES ENTRE CONTRATOS Y POSICIONES
-- =============================================
-- Tabla que permite asignar salarios especiales a empleados por contrato y posición
-- Si un empleado tiene un salario especial en esta tabla, se usa ese; si no, se usa el de la posición

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_contract_position_salaries' AND schema_id = SCHEMA_ID('app'))
BEGIN
    CREATE TABLE app.tbl_contract_position_salaries (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        public_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        
        contract_id BIGINT NOT NULL,
        position_id SMALLINT NOT NULL,
        salary DECIMAL(10,2) NOT NULL, -- Salario especial para este contrato y posición
        
        created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        created_by NVARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
        updated_at DATETIME2 NULL DEFAULT GETUTCDATE(),
        updated_by NVARCHAR(100) NULL,
        deleted_at DATETIME2 NULL,
        deleted_by NVARCHAR(100) NULL,
        
        CONSTRAINT FK_contract_position_salaries_contract
            FOREIGN KEY (contract_id) REFERENCES app.tbl_contracts(id)
            ON DELETE NO ACTION ON UPDATE NO ACTION,
        
        CONSTRAINT FK_contract_position_salaries_position
            FOREIGN KEY (position_id) REFERENCES app.tbl_positions(id)
            ON DELETE NO ACTION ON UPDATE NO ACTION
    );
    
    -- Índice único para public_id activo
    CREATE UNIQUE NONCLUSTERED INDEX UQ_contract_position_salaries_public_id_active
        ON app.tbl_contract_position_salaries(public_id)
        WHERE deleted_at IS NULL;
    
    -- Índice único para contract_id + position_id activo (un empleado solo puede tener un salario activo por contrato/posición)
    CREATE UNIQUE NONCLUSTERED INDEX UQ_contract_position_salaries_contract_position_active
        ON app.tbl_contract_position_salaries(contract_id, position_id)
        WHERE deleted_at IS NULL;
    
    -- Índice para búsquedas por contract_id
    CREATE NONCLUSTERED INDEX IX_contract_position_salaries_contract
        ON app.tbl_contract_position_salaries(contract_id, deleted_at);
    
    -- Índice para búsquedas por position_id
    CREATE NONCLUSTERED INDEX IX_contract_position_salaries_position
        ON app.tbl_contract_position_salaries(position_id, deleted_at);
END
GO

PRINT 'Migración V137 completada: Tabla tbl_contract_position_salaries creada para salarios especiales';
GO

