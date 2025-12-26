package com.agropay.ui.screens.tareo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agropay.ui.state.TareoState
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Modelo de empleado agregado al tareo
data class EmpleadoTareo(
    val dni: String,
    val nombre: String,
    val posicion: String,
    val motivoEntrada: String,
    val horaEntrada: String,
    val horaSalida: String? = null,
    val motivoSalida: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareoDetailScreen(
    viewModel: com.agropay.presentation.tareo.TareoDetailViewModel,
    tareoId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddEmployees: (String, String, String) -> Unit = { _, _, _ -> }
) {
    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    println("📋 TAREODETAILSCREEN - INICIANDO")
    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    println("🔍 Tareo ID recibido: $tareoId")
    
    val tareoInfo by viewModel.tareoInfo.collectAsState()
    val cacheRepository: com.agropay.data.repository.CacheRepository = org.koin.compose.koinInject()
    
    println("📊 Estado del tareoInfo: ${if (tareoInfo == null) "null" else "cargado"}")
    tareoInfo?.let {
        println("✅ TareoInfo cargado:")
        println("   - Tareo ID: ${it.tareo.id}")
        println("   - Labor: ${it.laborName}")
        println("   - Lote: ${it.loteName ?: "null (administrativo)"}")
        println("   - Supervisor: ${it.supervisorName}")
        println("   - Scanner: ${it.scannerName ?: "null"}")
        println("   - Fecha: ${it.fecha}")
        println("   - Total empleados: ${it.empleados.size}")
    }

    if (tareoInfo == null) {
        println("⚠️ TareoInfo es null, mostrando loading...")
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    val info = tareoInfo!!
    println("✅ TareoInfo válido, renderizando pantalla...")

    // Convertir empleados de BD a EmpleadoTareo para la UI
    val empleados = remember(info.empleados) {
        info.empleados.map { emp ->
            val employee = cacheRepository.getEmployeeByDocumentNumber(emp.employee_document_number)
            val entryMotive = emp.entry_motive_id?.let { cacheRepository.getTareoMotiveById(it) }
            val exitMotive = emp.exit_motive_id?.let { cacheRepository.getTareoMotiveById(it) }
            
            EmpleadoTareo(
                dni = emp.employee_document_number,
                nombre = employee?.let { "${it.names} ${it.paternal_lastname}" } ?: emp.employee_document_number,
                posicion = "Sin posición", // TODO: Obtener nombre de posición desde positions_cache
                motivoEntrada = entryMotive?.name ?: "Sin motivo",
                horaEntrada = emp.start_time ?: "N/A",
                horaSalida = emp.end_time,
                motivoSalida = exitMotive?.name
            )
        }
    }
    
    var empleadosState by remember { mutableStateOf(empleados) }

    var showBulkExitDialog by remember { mutableStateOf(false) }
    var empleadoParaSalida by remember { mutableStateOf<EmpleadoTareo?>(null) }
    
    val tareoRepository: com.agropay.data.repository.TareoRepository = org.koin.compose.koinInject()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Tareo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sección 1: Información General
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${info.laborName} - ${info.loteName ?: "Administrativo"}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoRow(label = "Labor:", value = info.laborName)
                    InfoRow(label = "Lote:", value = info.loteName ?: "Administrativo")
                    InfoRow(label = "Supervisor:", value = info.supervisorName)
                    InfoRow(label = "Fecha:", value = info.fecha)
                    InfoRow(label = "Estado:", value = if (info.tareo.is_synced) "Sincronizado" else "Pendiente")
                    info.scannerName?.let {
                        InfoRow(label = "Scanner:", value = it)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToAddEmployees(info.tareo.lote_id ?: "", info.tareo.labor_id, tareoId) },
                            modifier = Modifier.weight(1f),
                            enabled = !info.tareo.is_synced && !info.tareo.is_closing
                        ) {
                            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agregar")
                        }

                        OutlinedButton(
                            onClick = { showBulkExitDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = !info.tareo.is_synced && !info.tareo.is_closing
                        ) {
                            Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salida Grupal")
                        }
                    }
                    
                    // Mostrar estado si el tareo está cerrado
                    if (info.tareo.is_closing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "✓ Tareo cerrado - Listo para sincronizar",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Sección 2: Pedeteador (si existe)
            info.scannerName?.let { scannerName ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Pedeteador",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Usar la información del scanner que ya viene procesada del ViewModel
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = scannerName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "DNI: ${info.tareo.scanner_employee_document_number}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Sección 3: Lista de Empleados
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Empleados (${empleadosState.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(empleadosState) { empleado ->
                        EmpleadoCard(
                            empleado = empleado,
                            onMarcarSalida = { empleadoParaSalida = empleado }
                        )
                    }
                }
            }
        }
    }

    // Diálogo: Salida grupal
    if (showBulkExitDialog) {
        BulkExitDialog(
            tareoId = tareoId,
            empleadosSinSalida = empleadosState.filter { it.horaSalida == null },
            onDismiss = { showBulkExitDialog = false },
            onConfirm = { motivoId ->
                scope.launch {
                    try {
                        // Obtener hora actual formateada
                        val currentTime = kotlinx.datetime.Clock.System.now()
                        val timeZone = kotlinx.datetime.TimeZone.currentSystemDefault()
                        val localDateTime = currentTime.toLocalDateTime(timeZone)
                        val hour = localDateTime.hour.toString().padStart(2, '0')
                        val minute = localDateTime.minute.toString().padStart(2, '0')
                        val formattedTime = "$hour:$minute"
                        
                        // Obtener DNIs de empleados sin salida
                        val empleadosSinSalida = empleadosState.filter { it.horaSalida == null }
                        val dnis = empleadosSinSalida.map { it.dni }
                        
                        // Guardar salidas en BD y cerrar tareo
                        tareoRepository.bulkExitEmployeesAndCloseTareo(
                            tareoId = tareoId,
                            employees = dnis,
                            exitTime = formattedTime,
                            exitMotiveId = motivoId,
                            closingMotiveId = motivoId // Usar el mismo motivo para cerrar el tareo
                        )
                        
                        showBulkExitDialog = false
                        snackbarHostState.showSnackbar("Tareo cerrado exitosamente. Listo para sincronizar.")
                        
                        // Navegar de vuelta a la lista
                        onNavigateBack()
                    } catch (e: Exception) {
                        println("❌ Error al cerrar tareo: ${e.message}")
                        snackbarHostState.showSnackbar("Error al cerrar tareo: ${e.message}")
                    }
                }
            }
        )
    }

    // Diálogo: Salida individual
    empleadoParaSalida?.let { empleado ->
        IndividualExitDialog(
            empleado = empleado,
            onDismiss = { empleadoParaSalida = null },
            onConfirm = { motivo ->
                empleadosState = empleadosState.map {
                    if (it.dni == empleado.dni) {
                        it.copy(horaSalida = "17:30", motivoSalida = motivo)
                    } else it
                }
                empleadoParaSalida = null
            }
        )
    }
    
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun PedeteadorCard(pedeteador: EmpleadoTareo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pedeteador.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "DNI: ${pedeteador.dni}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = pedeteador.posicion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EmpleadoCard(
    empleado: EmpleadoTareo,
    onMarcarSalida: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (empleado.horaSalida != null)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = empleado.nombre,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "DNI: ${empleado.dni}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = empleado.posicion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (empleado.horaSalida == null) {
                    IconButton(
                        onClick = onMarcarSalida,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            "Marcar salida",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Entrada
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "E: ${empleado.horaEntrada}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = empleado.motivoEntrada,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Salida (si existe)
            empleado.horaSalida?.let { horaSalida ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "S: $horaSalida",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    empleado.motivoSalida?.let { motivoSalida ->
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = motivoSalida,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

// Diálogos
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkExitDialog(
    tareoId: String,
    empleadosSinSalida: List<EmpleadoTareo>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit // Recibe el ID del motivo (UUID)
) {
    var motivoExpanded by remember { mutableStateOf(false) }
    var motivoSeleccionado by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair<id, name>
    
    val cacheRepository: com.agropay.data.repository.CacheRepository = org.koin.compose.koinInject()
    val motivosFlow = cacheRepository.getAllTareoMotives()
    val motivos by motivosFlow.collectAsState(initial = emptyList())
    
    val motivosList = remember(motivos) {
        motivos.map { it.id to it.name }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Salida Grupal") },
        text = {
            Column {
                Text("Selecciona el motivo de salida para ${empleadosSinSalida.size} empleado(s) sin salida registrada. El tareo se cerrará automáticamente:")

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = motivoExpanded,
                    onExpandedChange = { motivoExpanded = it }
                ) {
                    OutlinedTextField(
                        value = motivoSeleccionado?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Motivo de salida") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = motivoExpanded,
                        onDismissRequest = { motivoExpanded = false }
                    ) {
                        motivosList.forEach { (id, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    motivoSeleccionado = id to name
                                    motivoExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { motivoSeleccionado?.first?.let { onConfirm(it) } },
                enabled = motivoSeleccionado != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cerrar Tareo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualExitDialog(
    empleado: EmpleadoTareo,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var motivoExpanded by remember { mutableStateOf(false) }
    var motivoSeleccionado by remember { mutableStateOf("") }

    val motivos = listOf("Fin de jornada", "Término anticipado", "Emergencia", "Permiso")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Marcar Salida") },
        text = {
            Column {
                Text("Empleado: ${empleado.nombre}")
                Text("DNI: ${empleado.dni}")

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = motivoExpanded,
                    onExpandedChange = { motivoExpanded = it }
                ) {
                    OutlinedTextField(
                        value = motivoSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Motivo de salida") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = motivoExpanded,
                        onDismissRequest = { motivoExpanded = false }
                    ) {
                        motivos.forEach { motivo ->
                            DropdownMenuItem(
                                text = { Text(motivo) },
                                onClick = {
                                    motivoSeleccionado = motivo
                                    motivoExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(motivoSeleccionado) },
                enabled = motivoSeleccionado.isNotBlank()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}