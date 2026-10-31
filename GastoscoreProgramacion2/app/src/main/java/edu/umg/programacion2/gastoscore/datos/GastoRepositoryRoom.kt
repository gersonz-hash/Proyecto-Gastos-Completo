package edu.umg.programacion2.gastoscore.datos

import edu.umg.programacion2.gastos.datos.GastoRepository
import edu.umg.programacion2.gastos.modelo.Gasto

class GastoRepositoryRoom(
    private val dao: GastoDao
) : GastoRepository {

    override suspend fun agregar(gasto: Gasto): Gasto {
        val id = dao.insertar(GastoEntity.desde(gasto))
        return gasto.copy(id = id.toInt())
    }

    override suspend fun listar(): List<Gasto> {
        return dao.listar().map { it.aGasto() }
    }

    override suspend fun buscarPorId(id: Int): Gasto? {
        return dao.buscarPorId(id)?.aGasto()
    }

    override suspend fun actualizar(gasto: Gasto): Boolean {
        return dao.actualizar(GastoEntity.desde(gasto)) > 0
    }

    override suspend fun eliminar(id: Int): Boolean {
        val gasto = dao.buscarPorId(id) ?: return false
        return dao.eliminar(gasto) > 0
    }
}
