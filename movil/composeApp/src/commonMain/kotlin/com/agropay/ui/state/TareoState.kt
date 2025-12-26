package com.agropay.ui.state

import androidx.compose.runtime.mutableStateListOf
import com.agropay.ui.screens.tareo.EmpleadoTareo

// Data class para Tareo completo
data class TareoData(
    val id: String,
    val titulo: String,
    val fundo: String,          // Nombre del fundo (para mostrar)
    val loteId: String,         // ID del lote (para operaciones)
    val labor: String,
    val laborId: String,        // ID de la labor (para operaciones)
    val fecha: String,
    val supervisor: String = "José Altamirano",
    val estado: String = "Activo",
    val empleados: List<EmpleadoTareo> = emptyList(),
    val tipoLabor: TipoLabor = TipoLabor.JORNAL,
    val pedeteadores: List<EmpleadoTareo> = emptyList()
)

enum class TipoLabor {
    JORNAL,    // Sin producción
    DESTAJO    // Con producción, requiere pedeteadores
}

// ATENCIÓN: Este objeto es una simulación y será reemplazado por un sistema de BBDD real.
object TareoState {
    // Se elimina la lista harcodeada.
    val tareos = mutableStateListOf<TareoData>()

    fun agregarTareo(tareo: TareoData) {
        tareos.add(tareo)
    }

    fun eliminarTareo(tareoId: String) {
        tareos.removeIf { it.id == tareoId }
    }

    fun actualizarEmpleados(tareoId: String, empleados: List<EmpleadoTareo>) {
        val index = tareos.indexOfFirst { it.id == tareoId }
        if (index != -1) {
            tareos[index] = tareos[index].copy(empleados = empleados)
        }
    }

    fun actualizarTareo(tareo: TareoData) {
        val index = tareos.indexOfFirst { it.id == tareo.id }
        if (index != -1) {
            tareos[index] = tareo
        }
    }

    fun obtenerTareo(tareoId: String): TareoData? {
        return tareos.find { it.id == tareoId }
    }

    fun obtenerTareosConProduccion(): List<TareoData> {
        return tareos.filter { it.tipoLabor == TipoLabor.DESTAJO }
    }

    fun obtenerTareosDePedeteador(dniPedeteador: String): List<TareoData> {
        return tareos.filter { tareo ->
            tareo.pedeteadores.any { it.dni == dniPedeteador }
        }
    }
}
