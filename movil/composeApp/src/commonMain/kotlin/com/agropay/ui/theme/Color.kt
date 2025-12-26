package com.agropay.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// PALETA PRINCIPAL - SOLO ESTOS COLORES
// ==========================================

// Verde principal
val GreenPrimary = Color(0xFF4faf5a) // #4faf5a

// Negro para botones
val BlackButton = Color(0xFF131613) // #131613

// Sombras y fondos sutiles
val ShadowGray = Color(0xFFF7F7F7) // #f7f7f7

// Bordes
val BorderGray = Color(0xFFF2F2F2) // #f2f2f2

// Blanco
val White = Color(0xFFFFFFFF)

// Negro para texto
val TextBlack = Color(0xFF131613)

// Gris para texto secundario
val TextGray = Color(0xFF6B6B6B)

// ==========================================
// COLORES PASTELES PARA ESTADOS
// (Transparentes y suaves)
// ==========================================

// Error - Rojo pastel
val ErrorPastel = Color(0xFFFFE5E5) // Rojo muy suave
val ErrorText = Color(0xFFD84747) // Rojo para texto

// Success - Verde pastel (usando el verde principal más suave)
val SuccessPastel = Color(0xFFE8F5E9) // Verde muy claro
val SuccessText = Color(0xFF4faf5a) // Nuestro verde

// Warning - Amarillo pastel
val WarningPastel = Color(0xFFFFF9E6) // Amarillo muy suave
val WarningText = Color(0xFFF5A623) // Amarillo para texto

// Info - Azul pastel
val InfoPastel = Color(0xFFE3F2FD) // Azul muy claro
val InfoText = Color(0xFF42A5F5) // Azul para texto

// ==========================================
// TEMA CLARO - Material 3
// ==========================================

val md_theme_light_primary = GreenPrimary
val md_theme_light_onPrimary = White
val md_theme_light_primaryContainer = Color(0xFFE8F5E9) // Verde pastel claro
val md_theme_light_onPrimaryContainer = TextBlack

val md_theme_light_secondary = BlackButton
val md_theme_light_onSecondary = White
val md_theme_light_secondaryContainer = ShadowGray
val md_theme_light_onSecondaryContainer = TextBlack

val md_theme_light_tertiary = GreenPrimary
val md_theme_light_onTertiary = White
val md_theme_light_tertiaryContainer = SuccessPastel
val md_theme_light_onTertiaryContainer = TextBlack

val md_theme_light_error = ErrorText
val md_theme_light_onError = White
val md_theme_light_errorContainer = ErrorPastel
val md_theme_light_onErrorContainer = ErrorText

val md_theme_light_background = White
val md_theme_light_onBackground = TextBlack

val md_theme_light_surface = White
val md_theme_light_onSurface = TextBlack

val md_theme_light_surfaceVariant = ShadowGray
val md_theme_light_onSurfaceVariant = TextGray

val md_theme_light_outline = BorderGray
val md_theme_light_outlineVariant = BorderGray

val md_theme_light_inverseSurface = BlackButton
val md_theme_light_inverseOnSurface = White
val md_theme_light_inversePrimary = GreenPrimary

val md_theme_light_surfaceTint = GreenPrimary
val md_theme_light_scrim = Color(0x80000000) // Negro semi-transparente

// ==========================================
// TEMA OSCURO - Material 3
// ==========================================

val md_theme_dark_primary = GreenPrimary
val md_theme_dark_onPrimary = BlackButton
val md_theme_dark_primaryContainer = Color(0xFF2D5F32)
val md_theme_dark_onPrimaryContainer = Color(0xFFE8F5E9)

val md_theme_dark_secondary = Color(0xFFB8C7B8)
val md_theme_dark_onSecondary = BlackButton
val md_theme_dark_secondaryContainer = Color(0xFF3A4A3A)
val md_theme_dark_onSecondaryContainer = Color(0xFFE8F5E9)

val md_theme_dark_tertiary = GreenPrimary
val md_theme_dark_onTertiary = BlackButton
val md_theme_dark_tertiaryContainer = Color(0xFF2D5F32)
val md_theme_dark_onTertiaryContainer = Color(0xFFE8F5E9)

val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = ErrorPastel

val md_theme_dark_background = Color(0xFF1A1C1A)
val md_theme_dark_onBackground = Color(0xFFE2E3DE)

val md_theme_dark_surface = Color(0xFF1A1C1A)
val md_theme_dark_onSurface = Color(0xFFE2E3DE)

val md_theme_dark_surfaceVariant = Color(0xFF424941)
val md_theme_dark_onSurfaceVariant = Color(0xFFC1C9BE)

val md_theme_dark_outline = Color(0xFF8B938A)
val md_theme_dark_outlineVariant = Color(0xFF424941)

val md_theme_dark_inverseSurface = Color(0xFFE2E3DE)
val md_theme_dark_inverseOnSurface = Color(0xFF1A1C1A)
val md_theme_dark_inversePrimary = Color(0xFF006D39)

val md_theme_dark_surfaceTint = GreenPrimary
val md_theme_dark_scrim = Color(0xFF000000)

// ==========================================
// COLORES ADICIONALES
// ==========================================

// Fondo para pantallas de Login y Unauthorized
val BackgroundGreenLight = White // Blanco puro, super limpio
