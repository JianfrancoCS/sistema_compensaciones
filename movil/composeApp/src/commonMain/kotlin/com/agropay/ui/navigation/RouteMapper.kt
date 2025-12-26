package com.agropay.ui.navigation

import com.agropay.domain.model.NavigationItemDTO

/**
 * Mapea rutas del backend a pantallas del móvil
 * Las rutas del backend vienen como "/system/employees", "/system/contracts", etc.
 */
object RouteMapper {
    /**
     * Convierte una ruta del backend a una Screen del móvil
     * Si la ruta no está mapeada, retorna null (no navegar)
     */
    fun mapRouteToScreen(route: String?): Screen? {
        if (route.isNullOrBlank()) return null
        
        // Normalizar la ruta (remover leading/trailing slashes)
        val normalizedRoute = route.trim().removePrefix("/").removeSuffix("/")
        
        return when {
            // Rutas de tareos
            normalizedRoute.contains("tareo", ignoreCase = true) -> Screen.TareoList
            
            // Rutas de producción
            normalizedRoute.contains("produccion", ignoreCase = true) ||
            normalizedRoute.contains("harvest", ignoreCase = true) -> Screen.ProduccionList
            
            // Rutas de empleados
            normalizedRoute.contains("employees", ignoreCase = true) ||
            normalizedRoute.contains("empleados", ignoreCase = true) -> Screen.TareoList // Por ahora, redirigir a tareos
            
            // Rutas de contratos
            normalizedRoute.contains("contracts", ignoreCase = true) ||
            normalizedRoute.contains("contratos", ignoreCase = true) -> Screen.TareoList // Por ahora, redirigir a tareos
            
            // Rutas de planillas
            normalizedRoute.contains("payroll", ignoreCase = true) ||
            normalizedRoute.contains("planilla", ignoreCase = true) -> Screen.TareoList // Por ahora, redirigir a tareos
            
            // Rutas de asistencia
            normalizedRoute.contains("attendance", ignoreCase = true) ||
            normalizedRoute.contains("asistencia", ignoreCase = true) -> Screen.TareoList // Por ahora, redirigir a tareos
            
            // Por defecto, no navegar (o redirigir a home)
            else -> null
        }
    }
    
    /**
     * Verifica si una ruta es válida para el móvil
     */
    fun isRouteSupported(route: String?): Boolean {
        return mapRouteToScreen(route) != null
    }
}

