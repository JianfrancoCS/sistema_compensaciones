package com.agropay.ui.screens.produccion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agropay.ui.state.TareoState

// Modelo para el conteo de producción por empleado
data class ProduccionEmpleado(
    val dni: String,
    val nombre: String,
    val cantidad: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduccionDetailScreen(
    tareoId: String,
    onNavigateBack: () -> Unit
) {
    val tareo = remember(tareoId) { TareoState.obtenerTareo(tareoId) }

    var produccionList by remember {
        mutableStateOf<List<ProduccionEmpleado>>(
            // Inicializar con los empleados del tareo
            tareo?.empleados?.map {
                ProduccionEmpleado(it.dni, it.nombre, 0)
            } ?: emptyList()
        )
    }
    var showScanner by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    if (tareo == null) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Producción: ${tareo.titulo}") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Información del tareo
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
                        text = "Fundo: ${tareo.fundo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Supervisor: ${tareo.supervisor}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Botón escanear
            Button(
                onClick = { showScanner = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escanear")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resumen total
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total producción:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "${produccionList.sumOf { it.cantidad }} unidades",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de empleados con su conteo
            Text(
                text = "Conteo por empleado",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por nombre o DNI") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filtrar empleados según búsqueda
            val filteredList = remember(produccionList, searchQuery) {
                if (searchQuery.isBlank()) {
                    produccionList.sortedByDescending { it.cantidad }
                } else {
                    produccionList.filter {
                        it.nombre.contains(searchQuery, ignoreCase = true) ||
                        it.dni.contains(searchQuery, ignoreCase = true)
                    }.sortedByDescending { it.cantidad }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList) { empleado ->
                    ProduccionCard(empleado)
                }
            }
        }
    }

    // Diálogo scanner
    if (showScanner) {
        ScannerProduccionDialog(
            onDismiss = { showScanner = false },
            onScan = { dni ->
                // Buscar al empleado y sumar 1 a su producción
                val index = produccionList.indexOfFirst { it.dni == dni }
                if (index != -1) {
                    produccionList = produccionList.toMutableList().apply {
                        this[index] = this[index].copy(cantidad = this[index].cantidad + 1)
                    }
                }
                showScanner = false
            }
        )
    }
}

@Composable
private fun ProduccionCard(empleado: ProduccionEmpleado) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (empleado.cantidad > 0)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
            }

            Surface(
                color = if (empleado.cantidad > 0)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "${empleado.cantidad}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (empleado.cantidad > 0)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ScannerProduccionDialog(
    onDismiss: () -> Unit,
    onScan: (String) -> Unit
) {
    var dniManual by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escanear DNI") },
        text = {
            Column {
                Text("Simula el escaneo ingresando el número de DNI:")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = dniManual,
                    onValueChange = { dniManual = it },
                    label = { Text("Número de DNI") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onScan(dniManual) },
                enabled = dniManual.isNotBlank()
            ) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}