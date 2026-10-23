
package edu.umg.programacion2.gastos.servicio

import edu.umg.programacion2.gastos.datos.GastoRepository
import edu.umg.programacion2.gastos.modelo.Categoria
import edu.umg.programacion2.gastos.modelo.Gasto

class ControlGastos(private val repositorio: GastoRepository) {

    fun validar(gasto: Gasto): String? {
        if (gasto.descripcion.isBlank()) {
            return "La descripción no puede estar vacía"
        }

        if (gasto.monto <= 0) {
            return "El monto debe ser mayor que 0"
        }

        if (!FORMATO_FECHA.matches(gasto.fecha)) {
            return "La fecha debe tener el formato yyyy-MM-dd"
        }

        return null
    }

    suspend fun registrar(gasto: Gasto): String? {
        val error = validar(gasto)
        if (error != null) {
            return error
        }

        repositorio.agregar(gasto)
        return null
    }

    suspend fun buscarPorId(id: Int): Gasto? {
        return repositorio.buscarPorId(id)
    }

    suspend fun actualizar(gasto: Gasto): String? {
        val error = validar(gasto)
        if (error != null) {
            return error
        }

        val actualizado = repositorio.actualizar(gasto)

        if (!actualizado) {
            return "No existe el gasto #${gasto.id}"
        }

        return null
    }

    suspend fun listar(): List<Gasto> {
        return repositorio.listar()
            .sortedByDescending { it.fecha }
    }

    suspend fun eliminar(id: Int): Boolean {
        return repositorio.eliminar(id)
    }

    suspend fun total(): Double {
        var suma = 0.0

        for (gasto in repositorio.listar()) {
            suma += gasto.monto
        }

        return suma
    }

    suspend fun totalPorCategoria(): Map<Categoria, Double> {
        val totales = mutableMapOf<Categoria, Double>()

        for (gasto in repositorio.listar()) {
            val acumulado = totales[gasto.categoria] ?: 0.0
            totales[gasto.categoria] = acumulado + gasto.monto
        }

        return totales
    }

    suspend fun resumen(): String {
        val cantidad = repositorio.listar().size
        val palabra = if (cantidad == 1) "gasto" else "gastos"

        return "$cantidad $palabra · Total: ${formatearQuetzales(total())}"
    }

    private companion object {
        val FORMATO_FECHA = Regex("""\d{4}-\d{2}-\d{2}""")
    }
}

fun formatearQuetzales(monto: Double): String = "Q %.2f".format(monto)
