package com.agropay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.agropay.presentation.login.AuthViewModel
import com.agropay.ui.navigation.Screen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    title: String,
    onNavigate: (Screen) -> Unit = {},
    onSyncDownload: () -> Unit = {},
    onSyncUpload: () -> Unit = {},
    onLogout: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    var isSidebarVisible by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val authViewModel: AuthViewModel = koinInject()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = { isSidebarVisible = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú"
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Más opciones",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Salir") },
                                    onClick = {
                                        showMenu = false
                                        showLogoutDialog = true
                                    }
                                )
                            }
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
            content(paddingValues)
        }

        // Sidebar dinámico desde el backend
        DynamicSidebar(
            isVisible = isSidebarVisible,
            onDismiss = { isSidebarVisible = false },
            onNavigate = { screen ->
                onNavigate(screen)
            },
            onSyncDownload = {
                isSidebarVisible = false
                onSyncDownload()
            },
            onSyncUpload = {
                isSidebarVisible = false
                onSyncUpload()
            },
            authViewModel = authViewModel
        )
    }

    // Diálogo de confirmación para salir
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Salir") },
            text = { Text("¿Estás seguro de que deseas salir?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}