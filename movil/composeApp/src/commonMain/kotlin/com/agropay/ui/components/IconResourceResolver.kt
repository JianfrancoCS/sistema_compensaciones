package com.agropay.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import movil.composeapp.generated.resources.Res

/**
 * Resuelve y muestra un icono desde un nombre simple (ej: "trash")
 * 
 * Prioridad:
 * 1. iconUrl (imagen desde URL del backend) - TODO: implementar con Coil
 * 2. iconName (nombre simple como "trash") -> mapea a IconType -> carga recurso vectorial
 * 3. icono por defecto
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun MenuIcon(
    iconUrl: String? = null,
    iconName: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    when {
        // Prioridad 1: Imagen desde URL del backend
        !iconUrl.isNullOrBlank() -> {
            // TODO: Implementar carga de imagen desde URL con Coil
            // Por ahora, usar icono por defecto
            DefaultIcon(modifier = modifier.size(size), tint = tint)
        }
        
        // Prioridad 2: Resolver icono desde nombre simple
        !iconName.isNullOrBlank() -> {
            val iconType = IconType.fromIconName(iconName)
            IconFromResource(
                iconType = iconType,
                modifier = modifier.size(size),
                tint = tint
            )
        }
        
        // Prioridad 3: Icono por defecto
        else -> {
            DefaultIcon(modifier = modifier.size(size), tint = tint)
        }
    }
}

/**
 * Carga un icono desde un recurso vectorial basado en IconType
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
private fun IconFromResource(
    iconType: IconType,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    // Intentar cargar el recurso vectorial
    // Por ahora, usar un icono de Material como fallback hasta que tengamos los recursos
    // Cuando tengas los archivos icon_*.xml, descomenta esto:
    /*
    Image(
        painter = painterResource(
            when (iconType) {
                IconType.HOME -> Res.drawable.icon_home
                IconType.USER -> Res.drawable.icon_user
                IconType.TRASH -> Res.drawable.icon_trash
                // ... agregar más casos según los iconos que tengas
                else -> Res.drawable.icon_default
            }
        ),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(tint)
    )
    */
    
    // Fallback temporal: usar icono de Material
    DefaultIcon(modifier = modifier, tint = tint)
}

/**
 * Icono por defecto cuando no hay icono específico disponible
 */
@Composable
private fun DefaultIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    // Usar un icono de Material como fallback
    Icon(
        imageVector = Icons.Filled.Circle,
        contentDescription = "Icono",
        modifier = modifier,
        tint = tint
    )
}

