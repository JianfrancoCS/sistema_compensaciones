package com.agropay.ui.screens.tareo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AssignmentLate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agropay.presentation.login.AuthViewModel
import com.agropay.presentation.sync.SyncState
import com.agropay.presentation.sync.SyncViewModel
import com.agropay.presentation.tareo.TareoListViewModel
import com.agropay.ui.components.LoadingOverlay
import com.agropay.ui.components.Sidebar
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareoListScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToProduccion: () -> Unit
) {
    val viewModel: TareoListViewModel = koinInject()
    val syncViewModel: SyncViewModel = koinInject()
    val authViewModel: AuthViewModel = koinInject()

    val tareos by viewModel.tareos.collectAsState()
    val syncState by syncViewModel.syncState.collectAsState()

    var showSidebar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

        LaunchedEffect(syncState) {
        when (val state = syncState) {
            is SyncState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                syncViewModel.resetSyncState()
            }
            is SyncState.Error -> {
                snackbarHostState.showSnackbar("Error: ${state.message}")
                syncViewModel.resetSyncState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Lista de Tareos") },
                    navigationIcon = {
                        IconButton(onClick = { showSidebar = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Add, "Crear Tareo")
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (tareos.isEmpty()) {
                    EmptyTareosState(onNavigateToCreate)
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(tareos) { tareo ->
                            TareoCard(
                                tareo = tareo,
                                onNavigateToDetail = onNavigateToDetail,
                                onDelete = { tareoId ->
                                    scope.launch {
                                        viewModel.deleteTareo(tareoId)
                                    }
                                },
                                onEdit = { tareoId ->
                                    onNavigateToDetail(tareoId)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Sidebar con callbacks de sincronización
        Sidebar(
            isVisible = showSidebar,
            onDismiss = { showSidebar = false },
            onNavigateToTareo = { showSidebar = false },
            onNavigateToProduccion = {
                onNavigateToProduccion()
                showSidebar = false
            },
            onSyncDownload = {
                syncViewModel.downloadAllData()
                showSidebar = false
            },
            onSyncUpload = {
                syncViewModel.uploadAllPendingData()
                showSidebar = false
            },
            authViewModel = authViewModel
        )

        // Loading overlay durante sincronización
        LoadingOverlay(
            isVisible = syncState is SyncState.Loading,
            message = "Sincronizando datos..."
        )
    }
}

@Composable
private fun EmptyTareosState(
    onNavigateToCreate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono grande
        Icon(
            imageVector = Icons.Outlined.AssignmentLate,
            contentDescription = "Sin tareos",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Título
        Text(
            text = "No hay tareos registrados",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Descripción
        Text(
            text = "Comienza creando tu primer tareo\npara gestionar el trabajo del equipo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de acción
        Button(
            onClick = onNavigateToCreate,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Crear",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear Tareo")
        }
    }
}

@Composable
fun TareoCard(
    tareo: com.agropay.db.Tareos,
    onNavigateToDetail: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onNavigateToDetail(tareo.id) },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icono de tareo
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Tareo",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Información del tareo
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Labor ID: ${tareo.labor_id}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lote: ${tareo.lote_id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Creado: ${tareo.created_at}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    // Badge de estado
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (tareo.is_closing) {
                            Text(
                                text = "🔒 Cerrado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        if (tareo.is_synced) {
                            Text(
                                text = "✓ Sincronizado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (!tareo.is_closing) {
                            Text(
                                text = "⏳ Pendiente",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        } else {
                            Text(
                                text = "📤 Listo para sincronizar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Botones de acción (solo si NO está sincronizado)
            if (!tareo.is_synced) {
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onEdit(tareo.id) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onDelete(tareo.id) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
