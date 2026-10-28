package edu.umg.programacion2.gastoscore

import android.content.Context
import androidx.room.Room
import edu.umg.programacion2.gastoscore.datos.AppDatabase
import edu.umg.programacion2.gastoscore.datos.GastoRepositoryRoom
import edu.umg.programacion2.gastos.servicio.ControlGastos

object ControlGastosApp {

    @Volatile
    private var instancia: ControlGastos? = null

    fun obtener(context: Context): ControlGastos {
        return instancia ?: synchronized(this) {
            instancia ?: crearControlGastos(context).also {
                instancia = it
            }
        }
    }

    private fun crearControlGastos(context: Context): ControlGastos {
        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "control_gastos_db"
        ).build()

        val repositorio = GastoRepositoryRoom(db.gastoDao())

        return ControlGastos(repositorio)
    }
}