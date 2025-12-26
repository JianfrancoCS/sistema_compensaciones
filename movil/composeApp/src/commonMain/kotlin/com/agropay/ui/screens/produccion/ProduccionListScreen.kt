package com.agropay.ui.screens.produccion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agropay.ui.components.MainLayout
import com.agropay.ui.state.TareoState
import com.agropay.ui.state.TareoData

@Composable
fun ProduccionListScreen(
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToTareo: () -> Unit = {}
) {
    // Simular DNI del pedeteador logueado (en el futuro vendrá del login)
    val pedeteadorDni = "99887766"

    // Obtener solo tareos con producción (DESTAJO) asignados a este pedeteador
    val tareosAsignados = remember {
        TareoState.obtenerTareosConProduccion().filter { tareo ->
            tareo.pedeteadores.any { it.dni == pedeteadorDni }
        }
    }

    MainLayout(
        title = "Producción",
        onNavigate = { onNavigateToTareo() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (tareosAsignados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No tienes tareos asignados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tareosAsignados, key = { it.id }) { tareo ->
                        TareoProduccionCard(
                            tareo = tareo,
                            onClick = { onNavigateToDetail(tareo.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TareoProduccionCard(
    tareo: TareoData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = tareo.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fecha: ${tareo.fecha}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = tareo.estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = "${tareo.empleados.size} empleados registrados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Text(
                text = "Supervisor: ${tareo.supervisor}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}