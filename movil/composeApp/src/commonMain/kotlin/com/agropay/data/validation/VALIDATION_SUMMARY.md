# Resumen de Validaciones - Móvil

## Validaciones Implementadas

### 1. Validación de Tareos

#### Antes de Crear Tareo
- ✅ Labor ID no vacío
- ✅ Lote ID no vacío
- ✅ Supervisor DNI no vacío
- ✅ Supervisor DNI con formato correcto (8 dígitos)

#### Antes de Sincronizar Tareo
- ✅ Tareo debe tener supervisor asignado
- ✅ Tareo debe tener labor asignada
- ✅ Tareo debe tener lote asignado
- ✅ Tareo debe tener al menos un empleado

### 2. Validación de Empleados en Tareo

#### Al Agregar Empleado
- ✅ DNI no vacío
- ✅ DNI con formato correcto (8 dígitos)
- ✅ Motivo de entrada debe existir
- ✅ Si hay hora de entrada, debe haber motivo de entrada
- ✅ Si hay hora de salida, debe haber motivo de salida
- ✅ Formato de hora válido (HH:mm o ISO)

### 3. Validación en Sincronización

#### Antes de Enviar al Backend
- ✅ Todos los tareos pendientes se validan
- ✅ Solo tareos válidos se incluyen en el batch
- ✅ Errores se reportan sin bloquear tareos válidos
- ✅ Mensajes de error descriptivos

## Flujo de Trabajo

### 1. Crear Tareo
```
Usuario selecciona Fundo → Lote → Labor
↓
Validación: laborId, loteId, supervisorDocumentNumber
↓
Si válido → Guardar en BD local
Si inválido → Mostrar error
```

### 2. Agregar Empleados
```
Usuario escanea QR o ingresa DNI
↓
Buscar empleado en backend (tiempo real)
↓
Si encontrado → Validar DNI y motivo
↓
Si válido → Guardar en BD local con hora actual
Si inválido → Mostrar error
```

### 3. Sincronizar Datos
```
Usuario presiona "Cargar datos"
↓
Obtener tareos no sincronizados
↓
Para cada tareo:
  - Validar tareo completo
  - Validar que tenga empleados
  - Validar cada empleado
↓
Solo enviar tareos válidos
↓
Marcar como sincronizados los exitosos
↓
Reportar errores de los fallidos
```

## Manejo de Errores

### Errores de Validación
- Se muestran mensajes descriptivos
- No bloquean la operación de otros tareos
- Se acumulan para reporte final

### Errores de Red
- Se capturan y reportan
- Los datos permanecen en BD local
- Se pueden reintentar más tarde

### Errores del Backend
- Se parsean y muestran mensajes del servidor
- Se mantienen los tareos fallidos para revisión
- Solo se marcan como sincronizados los exitosos

## Estado de Datos

### Datos Locales
- ✅ Se guardan inmediatamente en BD local
- ✅ Se marcan como `synced = 0` (no sincronizado)
- ✅ Disponibles offline

### Datos Sincronizados
- ✅ Se marcan como `synced = 1` (sincronizado)
- ✅ No se vuelven a enviar
- ✅ Se pueden consultar offline

## Próximos Pasos Recomendados

1. **Obtener Supervisor del Usuario Logueado**
   - Actualmente hardcodeado a "77777777"
   - Debe obtenerse de `AuthRepository.currentSession`

2. **Validar Datos de Cache**
   - Verificar que existan fundos, lotes, labores antes de crear tareo
   - Mostrar mensaje si no hay datos sincronizados

3. **Validar Conexión**
   - Verificar conexión antes de sincronizar
   - Mostrar mensaje si no hay conexión

4. **Retry Logic**
   - Reintentar tareos fallidos automáticamente
   - Configurar número máximo de reintentos

