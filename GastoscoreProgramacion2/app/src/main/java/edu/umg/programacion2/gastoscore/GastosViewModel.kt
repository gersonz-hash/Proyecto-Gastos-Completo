package edu.umg.programacion2.gastoscore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.umg.programacion2.gastos.modelo.Gasto
import edu.umg.programacion2.gastos.servicio.ControlGastos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import edu.umg.programacion2.gastoscore.datos.GastoEntity
data class GastosUiState(
    val transacciones: List<Gasto> = emptyList(),
    val egresos: Double = 0.0
)

class GastosViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val control: ControlGastos =
        Dependencias.obtenerControl(application)

    private val dao = GestorBaseDatos.obtenerDao(application)

    private val _estado = MutableStateFlow(GastosUiState())
    val estado = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            actualizarEstado()
        }
    }

    // Registrar un gasto
    fun registrar(gasto: Gasto, alTerminar: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val error = control.registrar(gasto)

                if (error == null) {
                    actualizarEstado()
                }

                alTerminar(error)
            } catch (e: Exception) {
                alTerminar(e.message ?: "No se pudo registrar el gasto")
            }
        }
    }

    // Eliminar un gasto
    fun eliminar(id: Int, alTerminar: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val eliminado = dao.eliminar(id)

                if (eliminado > 0) {
                    actualizarEstado()
                    alTerminar(null)
                } else {
                    alTerminar("No se encontró el gasto")
                }
            } catch (e: Exception) {
                alTerminar(e.message ?: "No se pudo eliminar el gasto")
            }
        }
    }

    // Editar un gasto
    fun editar(gasto: Gasto, alTerminar: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val actualizado = dao.actualizar(
                    GastoEntity.desde(gasto)
                ) > 0

                if (actualizado) {
                    actualizarEstado()
                    alTerminar(null)
                } else {
                    alTerminar("No se pudo actualizar el gasto")
                }
            } catch (e: Exception) {
                alTerminar(e.message ?: "Error al editar el gasto")
            }
        }
    }

    // Actualizar los datos que aparecen en pantalla
    private suspend fun actualizarEstado() {
        _estado.value = GastosUiState(
            transacciones = control.listar(),
            egresos = control.total()
        )
    }
}