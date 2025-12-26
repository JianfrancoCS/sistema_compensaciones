-- =============================================
-- V154: CORREGIR USUARIOS DUPLICADOS CON MISMO PUBLIC_ID
-- =============================================
-- Identifica y corrige usuarios duplicados con el mismo public_id
-- Esto puede ocurrir si se crearon usuarios manualmente o por migraciones
-- que no verificaron correctamente la existencia del usuario

PRINT '========================================';
PRINT 'CORRIGIENDO USUARIOS DUPLICADOS';
PRINT '========================================';
PRINT '';

-- Paso 1: Identificar usuarios duplicados (mismo public_id, ambos activos)
SELECT 
    public_id,
    COUNT(*) AS cantidad_duplicados,
    STRING_AGG(CAST(id AS NVARCHAR) + ':' + username, ', ') AS usuarios_duplicados
INTO #usuarios_duplicados
FROM app.tbl_users
WHERE deleted_at IS NULL
GROUP BY public_id
HAVING COUNT(*) > 1;

-- Mostrar usuarios duplicados encontrados
DECLARE @cantidad_duplicados INT = (SELECT COUNT(*) FROM #usuarios_duplicados);

IF @cantidad_duplicados > 0
BEGIN
    PRINT 'Se encontraron ' + CAST(@cantidad_duplicados AS NVARCHAR(10)) + ' public_id(s) duplicado(s):';
    PRINT '';
    
    DECLARE @public_id_duplicado UNIQUEIDENTIFIER;
    DECLARE @usuarios_info NVARCHAR(MAX);
    
    DECLARE dup_cursor CURSOR FOR
    SELECT public_id, usuarios_duplicados
    FROM #usuarios_duplicados;
    
    OPEN dup_cursor;
    FETCH NEXT FROM dup_cursor INTO @public_id_duplicado, @usuarios_info;
    
    WHILE @@FETCH_STATUS = 0
    BEGIN
        PRINT 'Public ID: ' + CAST(@public_id_duplicado AS NVARCHAR(36));
        PRINT '  Usuarios: ' + @usuarios_info;
        
        -- Para cada public_id duplicado, mantener el usuario más antiguo (menor id)
        -- y eliminar (soft delete) los demás
        DECLARE @usuario_a_mantener SMALLINT;
        DECLARE @usuario_a_eliminar SMALLINT;
        
        -- Obtener el ID del usuario más antiguo (menor id) para mantenerlo
        SELECT TOP 1 @usuario_a_mantener = id
        FROM app.tbl_users
        WHERE public_id = @public_id_duplicado
          AND deleted_at IS NULL
        ORDER BY id ASC; -- Mantener el más antiguo
        
        -- Eliminar (soft delete) los demás usuarios con el mismo public_id
        UPDATE app.tbl_users
        SET deleted_at = GETUTCDATE(),
            deleted_by = 'SYSTEM',
            updated_at = GETUTCDATE(),
            updated_by = 'SYSTEM'
        WHERE public_id = @public_id_duplicado
          AND deleted_at IS NULL
          AND id != @usuario_a_mantener;
        
        DECLARE @usuarios_eliminados INT = @@ROWCOUNT;
        PRINT '  Usuario mantenido (ID: ' + CAST(@usuario_a_mantener AS NVARCHAR(10)) + ')';
        PRINT '  Usuarios eliminados: ' + CAST(@usuarios_eliminados AS NVARCHAR(10));
        PRINT '';
        
        FETCH NEXT FROM dup_cursor INTO @public_id_duplicado, @usuarios_info;
    END
    
    CLOSE dup_cursor;
    DEALLOCATE dup_cursor;
    
    PRINT 'Corrección completada.';
END
ELSE
BEGIN
    PRINT 'No se encontraron usuarios duplicados.';
END

-- Limpiar tabla temporal
DROP TABLE #usuarios_duplicados;

-- Verificar que no queden duplicados
PRINT '';
PRINT 'Verificando que no queden duplicados...';
DECLARE @duplicados_restantes INT = (
    SELECT COUNT(*)
    FROM (
        SELECT public_id
        FROM app.tbl_users
        WHERE deleted_at IS NULL
        GROUP BY public_id
        HAVING COUNT(*) > 1
    ) AS dup
);

IF @duplicados_restantes > 0
BEGIN
    PRINT 'ERROR: Aún quedan ' + CAST(@duplicados_restantes AS NVARCHAR(10)) + ' public_id(s) duplicado(s).';
    PRINT 'Revisar manualmente la base de datos.';
END
ELSE
BEGIN
    PRINT 'OK: No quedan duplicados.';
END

PRINT '';
PRINT '========================================';
GO

