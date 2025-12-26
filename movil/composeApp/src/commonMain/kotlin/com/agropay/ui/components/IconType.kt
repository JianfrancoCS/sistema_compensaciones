package com.agropay.ui.components

/**
 * Enum de iconos disponibles en el móvil
 * Cada icono corresponde a un archivo vectorial en composeResources/drawable/
 * El nombre del archivo debe ser: icon_{nombre}.xml
 * 
 * Ejemplo: IconType.TRASH -> icon_trash.xml
 */
enum class IconType(val resourceName: String) {
    // ============================================
    // ICONOS COMUNES
    // ============================================
    HOME("icon_home"),
    USER("icon_user"),
    USERS("icon_users"),
    BUILDING("icon_building"),
    COMPANY("icon_company"),
    FILE("icon_file"),
    FOLDER("icon_folder"),
    CALENDAR("icon_calendar"),
    CHART("icon_chart"),
    SETTINGS("icon_settings"),
    COG("icon_cog"),
    MONEY("icon_money"),
    WALLET("icon_wallet"),
    BRIEFCASE("icon_briefcase"),
    LIST("icon_list"),
    TABLE("icon_table"),
    SEARCH("icon_search"),
    PLUS("icon_plus"),
    ADD("icon_add"),
    EDIT("icon_edit"),
    PENCIL("icon_pencil"),
    TRASH("icon_trash"),
    DELETE("icon_delete"),
    SAVE("icon_save"),
    PRINT("icon_print"),
    DOWNLOAD("icon_download"),
    UPLOAD("icon_upload"),
    SYNC("icon_sync"),
    REFRESH("icon_refresh"),
    CHECK("icon_check"),
    CLOSE("icon_close"),
    TIMES("icon_times"),
    INFO("icon_info"),
    WARNING("icon_warning"),
    LOCK("icon_lock"),
    KEY("icon_key"),
    UNLOCK("icon_unlock"),
    EYE("icon_eye"),
    EYE_SLASH("icon_eye_slash"),
    ARROW_RIGHT("icon_arrow_right"),
    ARROW_LEFT("icon_arrow_left"),
    ARROW_UP("icon_arrow_up"),
    ARROW_DOWN("icon_arrow_down"),
    CHEVRON_RIGHT("icon_chevron_right"),
    CHEVRON_LEFT("icon_chevron_left"),
    
    // ============================================
    // ICONOS DEL BACKEND (PRIMENG)
    // ============================================
    // Contenedores
    CHART_LINE("icon_chart_line"),          // dashboard
    SITEMAP("icon_sitemap"),                // organizations, organization-chart
    FILE_EDIT("icon_file_edit"),            // hiring, contracts
    CLOCK("icon_clock"),                    // attendance
    SHIELD("icon_shield"),                  // security
    
    // Elementos - Organizaciones
    WAREHOUSE("icon_warehouse"),            // subsidiaries
    OBJECTS_COLUMN("icon_objects_column"),  // areas
    GLOBE("icon_globe"),                   // foreign-persons
    
    // Elementos - Contratación
    COPY("icon_copy"),                      // contract-templates, addendum-templates
    FILE_PLUS("icon_file_plus"),           // addendums
    
    // Elementos - Operaciones
    TH_LARGE("icon_th_large"),              // batches, labor-units
    QRCODE("icon_qrcode"),                 // qr
    
    // Elementos - Asistencia
    SIGN_IN("icon_sign_in"),                // attendance-entry
    SIGN_OUT("icon_sign_out"),              // attendance-exit
    
    // Elementos - Planillas
    FILE_EXCEL("icon_file_excel"),          // payrolls
    CALENDAR_PLUS("icon_calendar_plus"),    // calendar
    
    // Elementos - Configuración
    CODE("icon_code"),                      // variables
    
    // Elementos - Seguridad
    ID_CARD("icon_id_card"),                // profiles
    
    // ============================================
    // ICONOS ESPECÍFICOS DE AGROPAY
    // ============================================
    EMPLOYEES("icon_employees"),
    CONTRACTS("icon_contracts"),
    PAYROLL("icon_payroll"),
    ATTENDANCE("icon_attendance"),
    TAREO("icon_tareo"),
    HARVEST("icon_harvest"),
    PRODUCTION("icon_production"),
    SUBSIDIARY("icon_subsidiary"),
    AREA("icon_area"),
    POSITION("icon_position"),
    LABOR("icon_labor"),
    LOT("icon_lot"),
    QR("icon_qr"),
    
    // ============================================
    // ICONO POR DEFECTO (siempre debe existir)
    // ============================================
    DEFAULT("icon_default");

    companion object {
        /**
         * Resuelve un nombre de icono (ej: "trash", "pi-trash", "pi pi-trash") a un IconType
         * Normaliza el nombre removiendo prefijos comunes y mapea a los iconos del backend
         */
        fun fromIconName(iconName: String?): IconType {
            if (iconName.isNullOrBlank()) return DEFAULT

            // Normalizar: remover "pi pi-", "pi-", espacios, convertir a lowercase
            val normalized = iconName
                .lowercase()
                .trim()
                .removePrefix("pi pi-")
                .removePrefix("pi-")
                .replace(" ", "-")
                .replace("_", "-")

            // Mapeo específico de iconos del backend
            val iconMap = mapOf(
                // Contenedores
                "chart-line" to CHART_LINE,
                "sitemap" to SITEMAP,
                "file-edit" to FILE_EDIT,
                "clock" to CLOCK,
                "wallet" to WALLET,
                "cog" to COG,
                "shield" to SHIELD,
                
                // Elementos - Organizaciones
                "warehouse" to WAREHOUSE,
                "objects-column" to OBJECTS_COLUMN,
                "globe" to GLOBE,
                "briefcase" to BRIEFCASE,
                "users" to USERS,
                
                // Elementos - Contratación
                "copy" to COPY,
                "file-plus" to FILE_PLUS,
                
                // Elementos - Operaciones
                "th-large" to TH_LARGE,
                "qrcode" to QRCODE,
                
                // Elementos - Asistencia
                "sign-in" to SIGN_IN,
                "sign-out" to SIGN_OUT,
                
                // Elementos - Planillas
                "file-excel" to FILE_EXCEL,
                "calendar-plus" to CALENDAR_PLUS,
                "calendar" to CALENDAR,
                
                // Elementos - Configuración
                "code" to CODE,
                "building" to BUILDING,
                
                // Elementos - Seguridad
                "id-card" to ID_CARD,
                
                // Iconos comunes
                "home" to HOME,
                "house" to HOME,
                "user" to USER,
                "person" to USER,
                "file" to FILE,
                "folder" to FOLDER,
                "folders" to FOLDER,
                "chart" to CHART,
                "settings" to SETTINGS,
                "money" to MONEY,
                "dollar" to MONEY,
                "currency" to MONEY,
                "list" to LIST,
                "table" to TABLE,
                "search" to SEARCH,
                "plus" to PLUS,
                "add" to ADD,
                "edit" to EDIT,
                "pencil" to PENCIL,
                "trash" to TRASH,
                "delete" to DELETE,
                "save" to SAVE,
                "print" to PRINT,
                "download" to DOWNLOAD,
                "upload" to UPLOAD,
                "sync" to SYNC,
                "refresh" to REFRESH,
                "check" to CHECK,
                "check-circle" to CHECK,
                "close" to CLOSE,
                "times" to TIMES,
                "x" to TIMES,
                "info" to INFO,
                "info-circle" to INFO,
                "warning" to WARNING,
                "exclamation-triangle" to WARNING,
                "lock" to LOCK,
                "key" to KEY,
                "unlock" to UNLOCK,
                "eye" to EYE,
                "visibility" to EYE,
                "eye-slash" to EYE_SLASH,
                "visibility-off" to EYE_SLASH,
            )

            // Buscar en el mapa primero
            iconMap[normalized]?.let { return it }

            // Si no está en el mapa, buscar por nombre del enum o resourceName
            return entries.find { 
                it.name.lowercase().replace("_", "-") == normalized ||
                it.resourceName.removePrefix("icon_").replace("_", "-") == normalized
            } ?: DEFAULT
        }
    }
}

