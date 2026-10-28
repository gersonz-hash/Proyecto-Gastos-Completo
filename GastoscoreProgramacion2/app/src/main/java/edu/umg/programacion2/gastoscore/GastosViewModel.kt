
package edu.umg.programacion2.gastoscore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.umg.programacion2.gastos.modelo.Gasto
import edu.umg.programacion2.gastos.servicio.ControlGastos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun registrar(gasto: Gasto, alTerminar: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val error = control.registrar(gasto)

                if (error == null) {
                    actualizarEstado()
                }

                alTerminar(error)
            } catch (e: Exception) {
                alTerminar(
                    e.message ?: "No se pudo registrar el gasto"
                )
            }
        }
    }

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
                alTerminar(
                    e.message ?: "No se pudo eliminar el gasto"
                )
            }
        }
    }

    private suspend fun actualizarEstado() {
        _estado.value = GastosUiState(
            transacciones = control.listar(),
            egresos = control.total()
        )
    }
}
