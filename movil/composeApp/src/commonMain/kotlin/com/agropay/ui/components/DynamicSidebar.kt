package com.agropay.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.agropay.domain.model.AuthState
import com.agropay.domain.model.NavigationItemDTO
import com.agropay.presentation.login.AuthViewModel
import com.agropay.ui.navigation.RouteMapper
import com.agropay.ui.navigation.Screen

/**
 * Sidebar dinámico construido desde el menú del backend
 */
@Composable
fun DynamicSidebar(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onSyncDownload: () -> Unit = {},
    onSyncUpload: () -> Unit = {},
    authViewModel: AuthViewModel? = null
) {
    val authState by (authViewModel?.authState ?: return AnimatedVisibility(visible = false) {}).collectAsState()
    
    val session = when (val state = authState) {
        is AuthState.Authenticated -> state.session
        else -> null
    }
    
    val menuItems = session?.menu ?: emptyList()
    val userInfo = session?.userInfo

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header con información del usuario
                    UserHeader(
                        username = userInfo?.username ?: "Usuario",
                        userId = session?.userId
                    )

                    HorizontalDivider()

                    // Menú dinámico desde el backend
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (menuItems.isEmpty()) {
                            Text(
                                text = "No hay elementos de menú disponibles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            Text(
                                text = "Navegación",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )

                            menuItems.forEach { item ->
                                MenuItem(
                                    item = item,
                                    onNavigate = { screen ->
                                        onNavigate(screen)
                                        onDismiss()
                                    },
                                    level = 0
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Botones de sincronización
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Sincronización",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )

                        // Botón Descargar datos
                        OutlinedButton(
                            onClick = {
                                onSyncDownload()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Descargar",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Descargar datos")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón Cargar datos
                        FilledTonalButton(
                            onClick = {
                                onSyncUpload()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Cargar",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cargar datos")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón Logout
                        OutlinedButton(
                            onClick = {
                                authViewModel?.logout()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Cerrar sesión",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrar sesión")
                        }
                    }
                }
            }

            // Overlay para cerrar el sidebar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                    .clickable { onDismiss() }
            )
        }
    }
}

/**
 * Header con información del usuario
 */
@Composable
private fun UserHeader(
    username: String,
    userId: String? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (userId != null) {
                    Text(
                        text = "ID: ${userId.take(8)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Item de menú (puede tener hijos)
 */
@Composable
private fun MenuItem(
    item: NavigationItemDTO,
    onNavigate: (Screen) -> Unit,
    level: Int = 0
) {
    var isExpanded by remember { mutableStateOf(false) }
    val hasChildren = !item.children.isNullOrEmpty()
    val screen = RouteMapper.mapRouteToScreen(item.path)

    if (hasChildren) {
        // Item con hijos (acordeón)
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                color = if (isExpanded) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                } else {
                    Color.Transparent
                },
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = (16 + level * 16).dp,
                            vertical = 12.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MenuIcon(
                        iconUrl = item.iconUrl,
                        iconName = item.icon,
                        size = 20.dp,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Default.ExpandLess
                        } else {
                            Icons.Default.ExpandMore
                        },
                        contentDescription = if (isExpanded) "Contraer" else "Expandir",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Hijos expandibles
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = androidx.compose.animation.shrinkVertically()
            ) {
                Column {
                    item.children?.forEach { child ->
                        MenuItem(
                            item = child,
                            onNavigate = onNavigate,
                            level = level + 1
                        )
                    }
                }
            }
        }
    } else {
        // Item sin hijos (clickeable)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = screen != null) {
                    screen?.let { onNavigate(it) }
                },
            color = Color.Transparent,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = (16 + level * 16).dp,
                        vertical = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuIcon(
                    iconUrl = item.iconUrl,
                    iconName = item.icon,
                    size = 20.dp,
                    tint = if (screen != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }
                )
                
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (screen != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

