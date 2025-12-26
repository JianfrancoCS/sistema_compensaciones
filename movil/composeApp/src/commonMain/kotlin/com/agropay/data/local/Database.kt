package com.agropay.data.local

import com.agropay.db.AgropayDatabase

/**
 * Singleton para acceso global a la base de datos SQLDelight.
 *
 * Uso:
 * ```
 * // Inicializar (en MainActivity para Android, en MainViewController para iOS)
 * Database.initialize(DatabaseDriverFactory(context))
 *
 * // Usar queries
 * Database.instance.tareosQueries.insertTareo(...)
 * Database.instance.tareosQueries.getPendingSync().asFlow()
 * ```
 */
object Database {
    private var database: AgropayDatabase? = null

    /**
     * Inicializa la base de datos con el driver específico de plataforma.
     * Debe llamarse UNA vez al iniciar la app.
     */
    fun initialize(driverFactory: DatabaseDriverFactory) {
        if (database == null) {
            database = AgropayDatabase(driverFactory.createDriver())
        }
    }

    /**
     * Instancia de la base de datos.
     * Lanza excepción si no se ha inicializado.
     */
    val instance: AgropayDatabase
        get() = database ?: error(
            "Database no inicializada. Llama a Database.initialize() primero."
        )
}