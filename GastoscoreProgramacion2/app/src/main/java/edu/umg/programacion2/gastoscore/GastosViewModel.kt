package edu.umg.programacion2.gastoscore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.umg.programacion2.gastos.modelo.Gasto
import edu.umg.programacion2.gastoscore.datos.GastoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GastosUiState(
    val transacciones: List<Gasto> = emptyList(),
    val tipos: Map<Int, String> = emptyMap(),
    val ingresos: Double = 0.0,
    val egresos: Double = 0.0
) {
    val saldo: Double
        get() = ingresos - egresos
}

class GastosViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dao = GestorBaseDatos.obtenerDao(application)

    private val _estado = MutableStateFlow(GastosUiState())
    val estado = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            actualizarEstado()
        }
    }

    fun registrar(
        gasto: Gasto,
        tipo: String,
        alTerminar: (String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                dao.insertar(
                    GastoEntity.desde(
                        gasto.copy(id = 0),
                        tipo.uppercase()
                    )
                )

                actualizarEstado()
                alTerminar(null)

            } catch (e: Exception) {
                alTerminar(
                    e.message ?: "No se pudo registrar el movimiento"
                )
            }
        }
    }

    fun eliminar(
        id: Int,
        alTerminar: (String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val movimiento = dao.buscarPorId(id)

                if (movimiento == null) {
                    alTerminar("No se encontró el movimiento")
                } else {
                    dao.eliminar(movimiento)
                    actualizarEstado()
                    alTerminar(null)
                }
            } catch (e: Exception) {
                alTerminar(e.message ?: "No se pudo eliminar")
            }
        }
    }

    fun editar(
        gasto: Gasto,
        tipo: String,
        alTerminar: (String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val anterior = dao.buscarPorId(gasto.id)

                if (anterior == null) {
                    alTerminar("No se encontró el movimiento")
                } else {
                    dao.actualizar(
                        GastoEntity.desde(
                            gasto,
                            tipo.uppercase()
                        )
                    )

                    actualizarEstado()
                    alTerminar(null)
                }
            } catch (e: Exception) {
                alTerminar(e.message ?: "No se pudo editar")
            }
        }
    }

    private suspend fun actualizarEstado() {
        val filas = dao.listar()

        val movimientos = filas.map { it.aGasto() }

        _estado.value = GastosUiState(
            transacciones = movimientos,
            tipos = filas.associate { it.id to it.tipo },
            ingresos = dao.totalPorTipo("INGRESO"),
            egresos = dao.totalPorTipo("EGRESO")
        )
    }
}
