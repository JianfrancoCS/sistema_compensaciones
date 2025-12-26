# Guía de Uso: SQLDelight Database + Repository

## Arquitectura Implementada

```
UI (Compose) → ViewModel → Repository → SQLDelight (SQLite local)
                                 ↓
                            API REST → Backend (SQL Server)
```

## 1. Base de Datos Local (SQLite)

La base de datos se inicializa automáticamente al abrir la app:
- **Android**: `MainActivity.onCreate()`
- **iOS**: `MainViewController()`

```kotlin
// Ya está configurado, NO necesitas hacer nada más
Database.initialize(DatabaseDriverFactory(context))
```

## 2. Usar Repository en un ViewModel

### Ejemplo: Crear Tareo

```kotlin
class TareoViewModel : ViewModel() {
    private val repository = TareoRepository()

    suspend fun crearTareo(
        supervisorDni: String,
        laborId: Long,
        subsidiaryId: Long,
        pedeteadorDni: String? = null
    ) {
        val localId = UUID.randomUUID().toString()

        try {
            val tareoId = repository.createTareo(
                localId = localId,
                supervisorDocumentNumber = supervisorDni,
                laborId = laborId,
                subsidiaryId = subsidiaryId,
                pedeteadorDocumentNumber = pedeteadorDni
            )

            println("Tareo creado con ID: $tareoId")
            // El tareo queda en estado PENDING para sincronización
        } catch (e: Exception) {
            println("Error al crear tareo: ${e.message}")
        }
    }
}
```

### Ejemplo: Observar Tareos Activos (Flow Reactivo)

```kotlin
class TareoViewModel : ViewModel() {
    private val repository = TareoRepository()

    val tareosActivos: Flow<List<Tareos>> = repository.getActiveTareos()
}

// En tu Composable
@Composable
fun TareosScreen(viewModel: TareoViewModel) {
    val tareos by viewModel.tareosActivos.collectAsState(initial = emptyList())

    LazyColumn {
        items(tareos) { tareo ->
            TareoCard(tareo)
        }
    }
}
```

### Ejemplo: Agregar Empleados a Tareo

```kotlin
suspend fun agregarEmpleado(
    tareoLocalId: String,
    empleadoDni: String
) {
    // 1. Obtener el tareo
    val tareo = repository.getTareoByLocalId(tareoLocalId) ?: return

    // 2. Verificar si ya está agregado
    val yaExiste = repository.isEmployeeInTareo(tareo.id, empleadoDni)
    if (yaExiste) {
        println("El empleado ya está en el tareo")
        return
    }

    // 3. Agregar empleado
    val employeeLocalId = UUID.randomUUID().toString()
    repository.addEmployeeToTareo(
        localId = employeeLocalId,
        tareoId = tareo.id,
        employeeDocumentNumber = empleadoDni,
        startTime = "08:00:00"
    )

    println("Empleado agregado exitosamente")
}
```

### Ejemplo: Observar Empleados de un Tareo

```kotlin
class TareoDetailViewModel(tareoLocalId: String) : ViewModel() {
    private val repository = TareoRepository()

    val empleados: Flow<List<Tareo_employees>> =
        repository.getEmployeesByTareoLocalId(tareoLocalId)
}

@Composable
fun TareoDetailScreen(tareoLocalId: String) {
    val viewModel = remember { TareoDetailViewModel(tareoLocalId) }
    val empleados by viewModel.empleados.collectAsState(initial = emptyList())

    Column {
        Text("Empleados: ${empleados.size}")
        empleados.forEach { empleado ->
            Text("${empleado.employee_document_number} - ${empleado.start_time}")
        }
    }
}
```

## 3. Sincronización con Backend

### WorkManager (aún por implementar)

```kotlin
class SyncWorker : CoroutineWorker() {
    override suspend fun doWork(): Result {
        val repository = TareoRepository()

        // 1. Obtener tareos pendientes
        val pending = repository.getPendingSyncTareos()

        if (pending.isEmpty()) {
            return Result.success()
        }

        // 2. Marcar como sincronizando
        repository.markTareosAsSyncing(pending.map { it.local_id })

        // 3. Enviar al backend por API
        pending.forEach { tareo ->
            try {
                val response = apiClient.createTareo(tareo.toDTO())

                // 4. Actualizar con public_id del servidor
                repository.updateTareoSyncSuccess(
                    localId = tareo.local_id,
                    publicId = response.publicId
                )
            } catch (e: Exception) {
                // 5. Marcar error
                repository.updateTareoSyncError(
                    localId = tareo.local_id,
                    errorMessage = e.message ?: "Error desconocido"
                )
            }
        }

        return Result.success()
    }
}
```

## 4. Queries Disponibles

### Tareos
- ✅ `createTareo()` - Crea tareo local
- ✅ `getTareoByLocalId()` - Obtiene tareo por local_id
- ✅ `getActiveTareos()` - Flow de tareos activos
- ✅ `getPendingSyncTareos()` - Lista tareos pendientes de sincronización
- ✅ `updateTareoSyncSuccess()` - Marca tareo como sincronizado
- ✅ `updateTareoSyncError()` - Marca tareo con error

### Empleados en Tareo
- ✅ `addEmployeeToTareo()` - Agrega empleado a tareo
- ✅ `getEmployeesByTareoLocalId()` - Flow de empleados de un tareo
- ✅ `isEmployeeInTareo()` - Verifica si empleado ya está agregado
- ✅ `updateEmployeeExit()` - Marca salida de empleado
- ✅ `getPendingSyncEmployees()` - Lista empleados pendientes de sincronización
- ✅ `countEmployeesInTareo()` - Cuenta empleados en un tareo

## 5. Estados de Sincronización

```kotlin
enum class SyncStatus {
    PENDING,   // Creado localmente, no sincronizado
    SYNCING,   // En proceso de sincronización
    SYNCED,    // Sincronizado exitosamente con backend
    ERROR      // Error al sincronizar
}
```

## 6. Patrón local_id + public_id

```kotlin
// CREACIÓN LOCAL
val localId = UUID.randomUUID().toString() // "abc-123-local"
repository.createTareo(localId = localId, ...)

// SINCRONIZACIÓN
val response = apiClient.createTareo(...)
repository.updateTareoSyncSuccess(
    localId = "abc-123-local",
    publicId = response.publicId // "xyz-789-server"
)

// AHORA EL TAREO TIENE AMBOS IDs:
// - local_id: "abc-123-local" (nunca cambia)
// - public_id: "xyz-789-server" (viene del backend)
```

## 7. Próximos Pasos

1. ✅ SQLDelight configurado
2. ✅ Database factory creado
3. ✅ Repository básico implementado
4. ⏳ Implementar API client con Retrofit
5. ⏳ Implementar WorkManager para sincronización
6. ⏳ Conectar UI con Repository
7. ⏳ Implementar cache de empleados (employees_cache)
8. ⏳ Implementar registros de cosecha (harvest_records)

## 8. Ejemplo Completo: Flujo de Trabajo

```kotlin
// 1. Usuario crea tareo (offline)
val localId = UUID.randomUUID().toString()
repository.createTareo(
    localId = localId,
    supervisorDocumentNumber = "12345678",
    laborId = 1,
    subsidiaryId = 1
)
// Estado: PENDING

// 2. Usuario agrega empleados
repository.addEmployeeToTareo(
    localId = UUID.randomUUID().toString(),
    tareoId = tareoId,
    employeeDocumentNumber = "87654321"
)
// Estado empleado: PENDING

// 3. WorkManager sincroniza automáticamente
syncWorker.doWork()
// Estado: SYNCING → SYNCED

// 4. Ahora tiene public_id del backend
val tareo = repository.getTareoByLocalId(localId)
println(tareo?.public_id) // "xyz-789-server"
```