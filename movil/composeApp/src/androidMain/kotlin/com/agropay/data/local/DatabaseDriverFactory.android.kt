package com.agropay.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.agropay.db.AgropayDatabase

/**
 * Implementación Android del DatabaseDriverFactory
 * 
 * Maneja la creación del driver SQLDelight y ejecuta migraciones manuales
 * cuando es necesario (por ejemplo, cuando se agregan nuevas columnas).
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        // Primero, ejecutar migraciones manuales si es necesario
        // Esto debe hacerse ANTES de crear el driver con el schema nuevo
        runManualMigrations()
        
        // Crear el driver con el schema actualizado
        // SQLDelight manejará automáticamente las migraciones del schema
        return AndroidSqliteDriver(
            schema = AgropayDatabase.Schema,
            context = context,
            name = "agropay.db"
        )
    }
    
    /**
     * Ejecuta migraciones manuales necesarias antes de que SQLDelight valide el schema.
     * Estas migraciones son para bases de datos que ya existen y necesitan actualizarse.
     */
    private fun runManualMigrations() {
        try {
            // Abrir la base de datos directamente para ejecutar migraciones
            val dbPath = context.getDatabasePath("agropay.db")
            if (dbPath.exists()) {
                val db = SQLiteDatabase.openDatabase(
                    dbPath.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READWRITE
                )
                
                try {
                    // Verificar si la columna closing_motive_id existe
                    val cursor = db.rawQuery("PRAGMA table_info(tareos)", null)
                    var columnExists = false
                    
                    while (cursor.moveToNext()) {
                        val columnName = cursor.getString(1) // índice 1 es el nombre de la columna
                        if (columnName == "closing_motive_id") {
                            columnExists = true
                            break
                        }
                    }
                    cursor.close()
                    
                    // Si la columna no existe, agregarla
                    if (!columnExists) {
                        db.execSQL("ALTER TABLE tareos ADD COLUMN closing_motive_id TEXT")
                    }
                } finally {
                    db.close()
                }
            }
        } catch (e: Exception) {
            // Si hay algún error en la migración manual, continuar de todas formas
            // SQLDelight intentará manejar el schema automáticamente
            println("⚠️ Error en migración manual: ${e.message}")
        }
    }
}
