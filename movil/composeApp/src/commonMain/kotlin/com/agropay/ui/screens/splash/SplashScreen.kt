package com.agropay.ui.screens.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agropay.domain.model.AuthState
import com.agropay.presentation.login.AuthViewModel
import kotlinx.coroutines.delay

/**
 * Pantalla inicial que verifica la sesión del usuario
 */
@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                delay(500) // Pequeña pausa para mostrar el splash
                onNavigateToHome()
            }
            is AuthState.Unauthenticated -> {
                delay(500)
                onNavigateToLogin()
            }
            is AuthState.Error -> {
                delay(1000)
                onNavigateToLogin()
            }
            is AuthState.Loading -> {
                // Esperando verificación
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "AgroPay",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}