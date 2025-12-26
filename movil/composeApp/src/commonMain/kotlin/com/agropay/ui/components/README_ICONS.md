# Sistema de Iconos Dinámicos

## Concepto

En lugar de almacenar el icono completo de PrimeNG (ej: `"pi pi-trash"`) en el backend, solo almacenamos la parte única (ej: `"trash"`). Luego cada plataforma resuelve el icono según sus necesidades:

- **Backend**: Almacena solo `"trash"` en el campo `icon`
- **Web**: Convierte `"trash"` → `"pi pi-trash"` para PrimeNG
- **Móvil**: Mapea `"trash"` → `IconType.TRASH` → `icon_trash.xml` (recurso vectorial)

## Estructura en el Móvil

### 1. IconType.kt
Enum que define todos los iconos disponibles y mapea nombres a recursos:

```kotlin
enum class IconType(val resourceName: String) {
    TRASH("icon_trash"),
    HOME("icon_home"),
    // ...
}
```

### 2. IconResourceResolver.kt
Resuelve iconos dinámicamente:

```kotlin
MenuIcon(
    iconUrl = item.iconUrl,  // Prioridad 1: URL de imagen
    iconName = item.icon,     // Prioridad 2: Nombre simple (ej: "trash")
    size = 24.dp,
    tint = MaterialTheme.colorScheme.onSurface
)
```

### 3. Recursos Vectoriales
Cada icono tiene un archivo XML en `composeResources/drawable/`:

- `icon_trash.xml`
- `icon_home.xml`
- `icon_user.xml`
- etc.

## Agregar Nuevos Iconos

1. **Agregar al enum IconType.kt**:
```kotlin
enum class IconType(val resourceName: String) {
    // ...
    NEW_ICON("icon_new_icon"),
}
```

2. **Crear el archivo vectorial**:
Crear `composeResources/drawable/icon_new_icon.xml`

3. **Actualizar IconResourceResolver.kt**:
Agregar el caso en el `when` de `IconFromResource`:
```kotlin
IconType.NEW_ICON -> Res.drawable.icon_new_icon
```

## Normalización de Nombres

El sistema normaliza automáticamente:
- `"pi pi-trash"` → `"trash"`
- `"pi-trash"` → `"trash"`
- `"TRASH"` → `"trash"`
- `"trash"` → `"trash"`

## Prioridad de Resolución

1. **iconUrl**: Si hay URL de imagen, se usa (TODO: implementar con Coil)
2. **iconName**: Se resuelve a `IconType` y se carga el recurso vectorial
3. **Default**: Si no hay icono, se usa `IconType.DEFAULT` → `icon_default.xml`

