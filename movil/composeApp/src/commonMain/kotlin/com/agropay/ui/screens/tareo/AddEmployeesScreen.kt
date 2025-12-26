package com.agropay.ui.screens.tareo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agropay.presentation.tareo.AddEmployeesViewModel
import com.agropay.ui.components.platform.QRCodeScannerView
import com.agropay.ui.theme.InfoPastel
import com.agropay.ui.theme.InfoText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeesScreen(
    viewModel: AddEmployeesViewModel,
    loteId: String?, // CORREGIDO: Puede ser null para tareos administrativos
    laborId: String,
    tareoId: String? = null,
    onNavigateBack: () -> Unit,
    onCreateTareo: (String) -> Unit
) {
    LaunchedEffect(laborId) { viewModel.loadLaborDetails(laborId) }
    val motives by viewModel.motives.collectAsState()
    val selectedLabor by viewModel.selectedLabor.collectAsState()

    var motivoExpanded by remember { mutableStateOf(false) }
    var motivoSeleccionado by remember { mutableStateOf("") }
    var empleadosAgregados by remember { mutableStateOf<List<EmpleadoTemporal>>(emptyList()) }
    var pedeteadorAgregado by remember { mutableStateOf<EmpleadoTemporal?>(null) }
    var showScanner by remember { mutableStateOf(false) }
    var showScannerPedeteador by remember { mutableStateOf(false) }

    val requierePedeteador = selectedLabor?.is_piecework ?: false
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Log para debugging
    LaunchedEffect(showScanner, showScannerPedeteador) {
        println("📱 Estado del scanner - showScanner: $showScanner, showScannerPedeteador: $showScannerPedeteador")
    }

    // CORREGIDO: Actualizar motivoId cuando cambia el motivo seleccionado
    LaunchedEffect(motivoSeleccionado) {
        if (motivoSeleccionado.isNotBlank() && empleadosAgregados.isNotEmpty()) {
            val motivoSeleccionadoObj = motives.find { it.name == motivoSeleccionado }
            val motivoId = motivoSeleccionadoObj?.id ?: ""
            empleadosAgregados = empleadosAgregados.map { empleado -> 
                empleado.copy(motivoId = motivoId) 
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedLabor?.name ?: "Agregar Empleados") },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Volver") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // SECCIÓN: MOTIVO GLOBAL
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Motivo de Entrada Global", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        ExposedDropdownMenuBox(expanded = motivoExpanded, onExpandedChange = { motivoExpanded = it }) {
                            OutlinedTextField(
                                value = motivoSeleccionado,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Selecciona motivo") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Expandir") },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface)
                            )
                            ExposedDropdownMenu(expanded = motivoExpanded, onDismissRequest = { motivoExpanded = false }) {
                                Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                                    motives.forEach { motive ->
                                        DropdownMenuItem(
                                            text = { Text(motive.name) },
                                            onClick = {
                                                motivoSeleccionado = motive.name
                                                motivoExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // SECCIÓN: PEDETEADOR
                if (requierePedeteador) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Pedeteador ${if (pedeteadorAgregado != null) "(1)" else ""}", style = MaterialTheme.typography.titleMedium)
                            FilledTonalButton(
                                onClick = { showScannerPedeteador = true },
                                enabled = pedeteadorAgregado == null,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = InfoPastel,
                                    contentColor = InfoText
                                )
                            ) {
                                Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Escanear")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        pedeteadorAgregado?.let { EmpleadoTemporalCard(empleado = it, onRemove = { pedeteadorAgregado = null }) }
                    }
                }

                // SECCIÓN: EMPLEADOS Y LISTA
                Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Empleados (${empleadosAgregados.size})", style = MaterialTheme.typography.titleMedium)
                        FilledTonalButton(
                            onClick = { showScanner = true },
                            enabled = motivoSeleccionado.isNotBlank(),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = InfoPastel,
                                contentColor = InfoText
                            )
                        ) {
                            Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Escanear")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(empleadosAgregados) { empleado ->
                            EmpleadoTemporalCard(empleado = empleado, onRemove = { empleadosAgregados = empleadosAgregados.filter { it.dni != empleado.dni } })
                        }
                    }
                }

                // BOTÓN FINAL
                Button(
                    onClick = {
                        if (tareoId == null) {
                            // CORREGIDO: Pasar scannerDocumentNumber (pedeteador) al crear tareo
                            val scannerDni = pedeteadorAgregado?.dni
                            viewModel.createNewTareo(
                                laborId = laborId, 
                                loteId = loteId,
                                scannerDocumentNumber = scannerDni, // CORREGIDO: Pasar pedeteador
                                onTareoCreated = { newTareoId ->
                                    // Guardar empleados escaneados en la BBDD
                                    scope.launch {
                                        // CORREGIDO: Pasar motivoId (UUID) en lugar de nombre
                                        val employeesWithMotives = empleadosAgregados.map { empleado ->
                                            Pair(empleado.dni, empleado.motivoId)
                                        }
                                        viewModel.addEmployeesToTareo(newTareoId, employeesWithMotives)
                                        onCreateTareo(newTareoId)
                                    }
                                }
                            )
                        } else {
                            // Agregar empleados a tareo existente
                            scope.launch {
                                // CORREGIDO: Pasar motivoId (UUID) en lugar de nombre
                                val employeesWithMotives = empleadosAgregados.map { empleado ->
                                    Pair(empleado.dni, empleado.motivoId)
                                }
                                viewModel.addEmployeesToTareo(tareoId ?: "", employeesWithMotives)
                                onNavigateBack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    enabled = empleadosAgregados.isNotEmpty() && motivoSeleccionado.isNotBlank()
                ) {
                    Text(if (tareoId != null) "Agregar Empleados" else "Crear Tareo")
                }
            }
        }

        // VISTA DEL ESCÁNER
        if (showScanner || showScannerPedeteador) {
            QRCodeScannerView(
                modifier = Modifier.fillMaxSize(),
                onCodeScanned = { dni ->
                    println("🔍 Código QR escaneado: $dni")
                    
                    // Cerrar el scanner INMEDIATAMENTE para que se pueda ver el Toast
                    val wasShowingScanner = showScanner
                    val wasShowingPedeteador = showScannerPedeteador
                    showScanner = false
                    showScannerPedeteador = false
                    
                    println("🔍 Scanner cerrado. wasShowingScanner: $wasShowingScanner, wasShowingPedeteador: $wasShowingPedeteador")

                    if (wasShowingScanner) {
                        println("🔍 Buscando empleado con DNI: $dni")
                        // Buscar empleado en tiempo real
                        scope.launch {
                            try {
                                val result = viewModel.searchEmployeeByDNI(dni, loteId ?: "")
                                println("🔍 Resultado de búsqueda recibido")

                                result.fold(
                                    onSuccess = { empleadoReal ->
                                        println("✅ Empleado encontrado: ${empleadoReal.documentNumber}")
                                        
                                        // CORREGIDO: Validar duplicados antes de agregar
                                        if (empleadosAgregados.any { it.dni == empleadoReal.documentNumber }) {
                                            snackbarHostState.showSnackbar(
                                                message = "El empleado ${empleadoReal.documentNumber} ya está en la lista",
                                                duration = SnackbarDuration.Short
                                            )
                                            return@launch
                                        }
                                        
                                        // CORREGIDO: Buscar motivo seleccionado por nombre y obtener su UUID
                                        val motivoSeleccionadoObj = motives.find { it.name == motivoSeleccionado }
                                        if (motivoSeleccionadoObj == null) {
                                            snackbarHostState.showSnackbar(
                                                message = "Error: Motivo no encontrado. Por favor, seleccione un motivo primero.",
                                                duration = SnackbarDuration.Short
                                            )
                                            return@launch
                                        }
                                        val motivoId = motivoSeleccionadoObj.id
                                        
                                        val fullName = "${empleadoReal.names} ${empleadoReal.paternalLastname} ${empleadoReal.maternalLastname}"
                                        val nuevoEmpleado = EmpleadoTemporal(
                                            dni = empleadoReal.documentNumber,
                                            nombre = fullName,
                                            posicion = "",
                                            motivoId = motivoId // CORREGIDO: Guardar UUID del motivo
                                        )
                                        empleadosAgregados = empleadosAgregados + nuevoEmpleado
                                        println("✅ Empleado agregado a la lista")
                                    },
                                    onFailure = { error ->
                                        println("❌ Error al buscar empleado: ${error.message}")
                                        // ❌ Error del servidor (success: false) - Mostrar Toast, NO agregar card
                                        snackbarHostState.showSnackbar(
                                            message = error.message ?: "Error desconocido",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                )
                            } catch (e: Exception) {
                                println("❌ Excepción al buscar empleado: ${e.message}")
                                e.printStackTrace()
                                snackbarHostState.showSnackbar(
                                    message = "Error inesperado: ${e.message}",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    } else if (wasShowingPedeteador) {
                        println("🔍 Buscando pedeteador con DNI: $dni")
                        // Buscar pedeteador en tiempo real
                        scope.launch {
                            try {
                                val result = viewModel.searchEmployeeByDNI(dni, loteId ?: "")
                                println("🔍 Resultado de búsqueda de pedeteador recibido")

                                result.fold(
                                    onSuccess = { empleadoReal ->
                                        println("✅ Pedeteador encontrado: ${empleadoReal.documentNumber}")
                                        val fullName = "${empleadoReal.names} ${empleadoReal.paternalLastname} ${empleadoReal.maternalLastname}"
                                        pedeteadorAgregado = EmpleadoTemporal(
                                            dni = empleadoReal.documentNumber,
                                            nombre = fullName,
                                            posicion = "",
                                            esPedeteador = true
                                        )
                                        println("✅ Pedeteador agregado")
                                    },
                                    onFailure = { error ->
                                        println("❌ Error al buscar pedeteador: ${error.message}")
                                        // ❌ Error del servidor (success: false) - Mostrar Toast, NO agregar card
                                        snackbarHostState.showSnackbar(
                                            message = error.message ?: "Error desconocido",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                )
                            } catch (e: Exception) {
                                println("❌ Excepción al buscar pedeteador: ${e.message}")
                                e.printStackTrace()
                                snackbarHostState.showSnackbar(
                                    message = "Error inesperado: ${e.message}",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                },
                onPermissionDenied = {
                    println("❌ Permiso de cámara denegado")
                    showScanner = false
                    showScannerPedeteador = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Se necesita permiso de cámara para escanear códigos QR",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }
    }
}

// CORREGIDO: motivoId ahora es UUID en lugar de nombre
data class EmpleadoTemporal(
    val dni: String, 
    val nombre: String, 
    val posicion: String, 
    val motivoId: String = "", // CORREGIDO: UUID del motivo
    val esPedeteador: Boolean = false
)

@Composable
private fun EmpleadoTemporalCard(empleado: EmpleadoTemporal, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(empleado.nombre, style = MaterialTheme.typography.titleSmall)
                Text("DNI: ${empleado.dni}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
        }
    }
}
