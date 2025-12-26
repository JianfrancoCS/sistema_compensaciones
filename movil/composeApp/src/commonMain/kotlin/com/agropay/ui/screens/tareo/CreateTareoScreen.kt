package com.agropay.ui.screens.tareo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.agropay.presentation.tareo.CreateTareoViewModel
import com.agropay.ui.theme.BlackButton
import com.agropay.ui.theme.TextBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTareoScreen(
    viewModel: CreateTareoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddEmployees: (loteId: String?, laborId: String) -> Unit // CORREGIDO: loteId puede ser null para tareos administrativos
) {
    // Observar datos del ViewModel
    val fundos by viewModel.fundos.collectAsState()
    val lotes by viewModel.lotes.collectAsState()
    val labors by viewModel.labors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Estado para Fundo
    var fundoExpanded by remember { mutableStateOf(false) }
    var selectedFundoId by remember { mutableStateOf("") }
    var selectedFundoName by remember { mutableStateOf("") }

    // Estado para Lote
    var loteExpanded by remember { mutableStateOf(false) }
    var selectedLoteId by remember { mutableStateOf("") }
    var selectedLoteName by remember { mutableStateOf("") }

    // Estado para Labor
    var laborExpanded by remember { mutableStateOf(false) }
    var selectedLaborId by remember { mutableStateOf("") }
    var selectedLaborName by remember { mutableStateOf("") }
    
    // CORREGIDO: Estado para tareo administrativo
    var isAdministrativeTareo by remember { mutableStateOf(false) }

    // Cuando se selecciona un fundo, cargar sus lotes
    LaunchedEffect(selectedFundoId) {
        if (selectedFundoId.isNotBlank()) {
            viewModel.loadLotesByFundo(selectedFundoId)
            // Limpiar lote seleccionado al cambiar de fundo
            selectedLoteId = ""
            selectedLoteName = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Nuevo Tareo") },
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
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(paddingValues)
                )
            } else if (fundos.isEmpty() || labors.isEmpty()) {
                // Mostrar mensaje si no hay datos
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No hay datos disponibles",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Por favor, descarga los datos usando el botón en el menú lateral",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Título en negro
                    Text(
                        text = "Selecciona el Fundo, Lote y Labor",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextBlack
                    )

                    // 1. Dropdown: Fundo (antes Sucursal)
                    ExposedDropdownMenuBox(
                        expanded = fundoExpanded,
                        onExpandedChange = { fundoExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedFundoName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fundo") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, "Expandir")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        )

                        ExposedDropdownMenu(
                            expanded = fundoExpanded,
                            onDismissRequest = { fundoExpanded = false }
                        ) {
                            fundos.forEach { fundo ->
                                DropdownMenuItem(
                                    text = { Text(fundo.name) },
                                    onClick = {
                                        selectedFundoId = fundo.id
                                        selectedFundoName = fundo.name
                                        fundoExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // CORREGIDO: Checkbox para tareo administrativo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAdministrativeTareo,
                            onCheckedChange = { 
                                isAdministrativeTareo = it
                                // Si se marca como administrativo, limpiar lote seleccionado
                                if (it) {
                                    selectedLoteId = ""
                                    selectedLoteName = ""
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tareo administrativo (sin lote)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    // 2. Dropdown: Lote (deshabilitado si es administrativo o si no hay fundo)
                    ExposedDropdownMenuBox(
                        expanded = loteExpanded,
                        onExpandedChange = {
                            if (!isAdministrativeTareo && selectedFundoId.isNotBlank()) {
                                loteExpanded = it
                            }
                        }
                    ) {
                        OutlinedTextField(
                            value = selectedLoteName,
                            onValueChange = {},
                            readOnly = true,
                            enabled = !isAdministrativeTareo && selectedFundoId.isNotBlank(),
                            label = { Text("Lote${if (isAdministrativeTareo) " (no requerido)" else ""}") },
                            placeholder = { Text(if (isAdministrativeTareo) "No requerido para tareos administrativos" else "Selecciona un lote") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, "Expandir")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, !isAdministrativeTareo && selectedFundoId.isNotBlank())
                        )

                        ExposedDropdownMenu(
                            expanded = loteExpanded,
                            onDismissRequest = { loteExpanded = false }
                        ) {
                            if (lotes.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No hay lotes para este fundo") },
                                    onClick = { },
                                    enabled = false
                                )
                            } else {
                                lotes.forEach { lote ->
                                    DropdownMenuItem(
                                        text = { Text(lote.name) },
                                        onClick = {
                                            selectedLoteId = lote.id
                                            selectedLoteName = lote.name
                                            loteExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 3. Dropdown: Labor
                    ExposedDropdownMenuBox(
                        expanded = laborExpanded,
                        onExpandedChange = { laborExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedLaborName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Labor") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, "Expandir")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        )

                        ExposedDropdownMenu(
                            expanded = laborExpanded,
                            onDismissRequest = { laborExpanded = false }
                        ) {
                            labors.forEach { labor ->
                                DropdownMenuItem(
                                    text = { Text(labor.name) },
                                    onClick = {
                                        selectedLaborId = labor.id
                                        selectedLaborName = labor.name
                                        laborExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // CORREGIDO: Botón Continuar - Requiere labor + (lote O tareo administrativo)
                    Button(
                        onClick = {
                            // CORREGIDO: Pasar null para lote si es tareo administrativo
                            val loteId = if (isAdministrativeTareo) null else selectedLoteId.takeIf { it.isNotBlank() }
                            onNavigateToAddEmployees(loteId, selectedLaborId)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedLaborId.isNotBlank() && (isAdministrativeTareo || selectedLoteId.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlackButton
                        )
                    ) {
                        Text("Continuar")
                    }
                }
            }
        }
    }
}
