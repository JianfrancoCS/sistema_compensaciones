-- =============================================
-- V121: AGREGAR SOPORTE DE PLATAFORMA A ELEMENTOS Y CONTENEDORES
-- =============================================
-- Agrega campos booleanos para indicar en qué plataformas está disponible cada elemento/contenedor
-- - is_web: Disponible en aplicación web
-- - is_mobile: Disponible en aplicación móvil
-- - is_desktop: Disponible en aplicación de escritorio (futuro)

-- =============================================
-- AGREGAR CAMPOS DE PLATAFORMA A ELEMENTOS
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_elements') AND name = 'is_web')
BEGIN
    ALTER TABLE app.tbl_elements
    ADD is_web BIT NOT NULL DEFAULT 1; -- Por defecto disponible en web (compatibilidad)
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_elements') AND name = 'is_mobile')
BEGIN
    ALTER TABLE app.tbl_elements
    ADD is_mobile BIT NOT NULL DEFAULT 1; -- Por defecto disponible en móvil (compatibilidad)
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_elements') AND name = 'is_desktop')
BEGIN
    ALTER TABLE app.tbl_elements
    ADD is_desktop BIT NOT NULL DEFAULT 0; -- Por defecto NO disponible en desktop
END
GO

-- Índice para búsquedas por plataforma móvil
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_elements_mobile_active' AND object_id = OBJECT_ID('app.tbl_elements'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_elements_mobile_active
    ON app.tbl_elements(is_mobile, is_active, deleted_at)
    WHERE is_mobile = 1 AND is_active = 1 AND deleted_at IS NULL;
END
GO

-- Índice para búsquedas por plataforma web
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_elements_web_active' AND object_id = OBJECT_ID('app.tbl_elements'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_elements_web_active
    ON app.tbl_elements(is_web, is_active, deleted_at)
    WHERE is_web = 1 AND is_active = 1 AND deleted_at IS NULL;
END
GO

-- =============================================
-- AGREGAR CAMPOS DE PLATAFORMA A CONTENEDORES
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_containers') AND name = 'is_web')
BEGIN
    ALTER TABLE app.tbl_containers
    ADD is_web BIT NOT NULL DEFAULT 1; -- Por defecto disponible en web (compatibilidad)
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_containers') AND name = 'is_mobile')
BEGIN
    ALTER TABLE app.tbl_containers
    ADD is_mobile BIT NOT NULL DEFAULT 1; -- Por defecto disponible en móvil (compatibilidad)
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('app.tbl_containers') AND name = 'is_desktop')
BEGIN
    ALTER TABLE app.tbl_containers
    ADD is_desktop BIT NOT NULL DEFAULT 0; -- Por defecto NO disponible en desktop
END
GO

-- Índices para búsquedas por plataforma
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_containers_mobile_active' AND object_id = OBJECT_ID('app.tbl_containers'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_containers_mobile_active
    ON app.tbl_containers(is_mobile, is_active, deleted_at)
    WHERE is_mobile = 1 AND is_active = 1 AND deleted_at IS NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_containers_web_active' AND object_id = OBJECT_ID('app.tbl_containers'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_containers_web_active
    ON app.tbl_containers(is_web, is_active, deleted_at)
    WHERE is_web = 1 AND is_active = 1 AND deleted_at IS NULL;
END
GO

PRINT 'Migración V121 completada: Campos de plataforma agregados a elementos y contenedores';
GO

