package com.agropay.ui.navigation

import androidx.compose.runtime.*
import com.agropay.core.security.HAS_AUTHORIZE
import com.agropay.domain.model.AuthState
import com.agropay.presentation.login.AuthViewModel
import com.agropay.presentation.tareo.AddEmployeesViewModel
import com.agropay.presentation.tareo.CreateTareoViewModel
import com.agropay.ui.screens.login.LoginScreen
import com.agropay.ui.screens.splash.SplashScreen
import com.agropay.ui.screens.tareo.*
import com.agropay.ui.screens.produccion.*
import com.agropay.ui.screens.unauthorized.UnauthorizedScreen
import org.koin.compose.koinInject
import org.koin.compose.getKoin

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Unauthorized : Screen()
    object TareoList : Screen()
    data class CreateTareo(val loteId: String = "", val laborId: String = "") : Screen()
    data class AddEmployees(val loteId: String?, val laborId: String, val tareoId: String? = null) : Screen() // CORREGIDO: loteId puede ser null para tareos administrativos
    data class TareoDetail(val tareoId: String) : Screen()
    object ProduccionList : Screen()
    data class ProduccionDetail(val tareoId: String) : Screen()
}

@Composable
fun Navigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    val authViewModel: AuthViewModel = koinInject()
    val authState by authViewModel.authState.collectAsState()

    // Helper para verificar si el usuario tiene permisos válidos
    // Verifica si el usuario tiene un menú con elementos (lo que indica que tiene permisos)
    // o si tiene el perfil correcto asignado
    fun hasValidPermissions(): Boolean {
        val session = (authState as? AuthState.Authenticated)?.session ?: return false
        
        // Si el usuario tiene elementos en el menú, significa que tiene permisos
        val hasMenuItems = session.menu != null && session.menu.isNotEmpty()
        
        if (hasMenuItems) {
            println("✅ Usuario tiene ${session.menu.size} elementos en el menú - acceso permitido")
            return true
        }
        
        // Si no tiene menú, verificar por grupos (fallback)
        val userInfo = session.userInfo
        val hasGroup = HAS_AUTHORIZE(userInfo, "SUPERVISOR") || HAS_AUTHORIZE(userInfo, "ACOPIADOR")
        
        if (hasGroup) {
            println("✅ Usuario tiene grupo válido - acceso permitido")
            return true
        }
        
        println("❌ Usuario NO tiene permisos - menú vacío y sin grupos válidos")
        return false
    }

    // Helper para navegar después del login
    fun navigateAfterLogin() {
        currentScreen = if (hasValidPermissions()) {
            Screen.TareoList
        } else {
            Screen.Unauthorized
        }
    }

    // Escuchar cambios en authState para logout automático
    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated &&
            currentScreen !is Screen.Splash &&
            currentScreen !is Screen.Login) {
            currentScreen = Screen.Login
        }
    }

    when (val screen = currentScreen) {
        is Screen.Splash -> {
            SplashScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { currentScreen = Screen.Login },
                onNavigateToHome = { navigateAfterLogin() }
            )
        }

        is Screen.Login -> {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { navigateAfterLogin() }
            )
        }

        is Screen.Unauthorized -> {
            UnauthorizedScreen(
                onLogout = {
                    authViewModel.logout()
                    currentScreen = Screen.Login
                }
            )
        }

        is Screen.TareoList -> {
            TareoListScreen(
                onNavigateToCreate = { currentScreen = Screen.CreateTareo() },
                onNavigateToDetail = { tareoId -> currentScreen = Screen.TareoDetail(tareoId) },
                onNavigateToProduccion = { currentScreen = Screen.ProduccionList }
            )
        }

        is Screen.CreateTareo -> {
            val createTareoViewModel: CreateTareoViewModel = koinInject()

            CreateTareoScreen(
                viewModel = createTareoViewModel,
                onNavigateBack = { currentScreen = Screen.TareoList },
                onNavigateToAddEmployees = { loteId, laborId ->
                    currentScreen = Screen.AddEmployees(loteId, laborId)
                }
            )
        }

        is Screen.AddEmployees -> {
            val addEmployeesViewModel: AddEmployeesViewModel = koinInject()

            AddEmployeesScreen(
                viewModel = addEmployeesViewModel,
                loteId = screen.loteId,
                laborId = screen.laborId,
                tareoId = screen.tareoId,
                onNavigateBack = {
                    currentScreen = if (screen.tareoId != null) {
                        Screen.TareoDetail(screen.tareoId!!)
                    } else {
                        Screen.CreateTareo()
                    }
                },
                onCreateTareo = { tareoId ->
                    currentScreen = Screen.TareoDetail(tareoId)
                }
            )
        }

        is Screen.TareoDetail -> {
            val koin = getKoin()
            val tareoDetailViewModel = remember(screen.tareoId) {
                koin.get<com.agropay.presentation.tareo.TareoDetailViewModel>(
                    parameters = { org.koin.core.parameter.parametersOf(screen.tareoId) }
                )
            }
            
            TareoDetailScreen(
                viewModel = tareoDetailViewModel,
                tareoId = screen.tareoId,
                onNavigateBack = { currentScreen = Screen.TareoList },
                onNavigateToAddEmployees = { loteId, laborId, tareoId ->
                    currentScreen = Screen.AddEmployees(loteId, laborId, tareoId)
                }
            )
        }

        is Screen.ProduccionList -> {
            ProduccionListScreen(
                onNavigateToDetail = { tareoId ->
                    currentScreen = Screen.ProduccionDetail(tareoId)
                },
                onNavigateToTareo = { currentScreen = Screen.TareoList }
            )
        }

        is Screen.ProduccionDetail -> {
            ProduccionDetailScreen(
                tareoId = screen.tareoId,
                onNavigateBack = { currentScreen = Screen.ProduccionList }
            )
        }
    }
}
