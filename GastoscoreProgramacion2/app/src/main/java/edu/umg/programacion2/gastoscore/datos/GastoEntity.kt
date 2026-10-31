
package edu.umg.programacion2.gastoscore.datos

import androidx.room.Entity
import androidx.room.PrimaryKey
import edu.umg.programacion2.gastos.modelo.Gasto
import edu.umg.programacion2.gastos.modelo.Categoria

@Entity(tableName = "gastos")
data class GastoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    val categoria: String,
    val fecha: String,
    val tipo: String = "EGRESO"
) {
    fun aGasto(): Gasto {
        return Gasto(
            id = id,
            descripcion = descripcion,
            monto = monto,
            categoria = Categoria.valueOf(categoria),
            fecha = fecha
        )
    }

    companion object {
        fun desde(
            gasto: Gasto,
            tipo: String = "EGRESO"
        ): GastoEntity {
            return GastoEntity(
                id = gasto.id,
                descripcion = gasto.descripcion,
                monto = gasto.monto,
                categoria = gasto.categoria.name,
                fecha = gasto.fecha,
                tipo = tipo
            )
        }
    }
}
