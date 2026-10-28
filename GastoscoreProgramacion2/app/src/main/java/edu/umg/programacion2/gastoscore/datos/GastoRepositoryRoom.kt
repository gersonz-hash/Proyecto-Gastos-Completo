package edu.umg.programacion2.gastoscore.datos

import edu.umg.programacion2.gastos.datos.GastoRepository
import edu.umg.programacion2.gastos.modelo.Gasto

class GastoRepositoryRoom(
    private val dao: GastoDao
) : GastoRepository {

    override suspend fun agregar(gasto: Gasto): Gasto {
        val idNuevo = dao.insertar(
            GastoEntity.desde(gasto.copy(id = 0))
        )
        return gasto.copy(id = idNuevo.toInt())
    }

    override suspend fun listar(): List<Gasto> {
        val gastos = mutableListOf<Gasto>()

        for (fila in dao.listar()) {
            gastos.add(fila.aGasto())
        }

        return gastos
    }

    override suspend fun buscarPorId(id: Int): Gasto? {
        return dao.buscarPorId(id)?.aGasto()
    }

    override suspend fun actualizar(gasto: Gasto): Boolean {
        return dao.actualizar(GastoEntity.desde(gasto)) > 0
    }

    override suspend fun eliminar(id: Int): Boolean {
        return dao.eliminar(id) > 0
    }
}