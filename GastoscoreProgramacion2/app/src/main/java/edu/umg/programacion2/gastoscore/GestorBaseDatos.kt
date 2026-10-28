
package edu.umg.programacion2.gastoscore

import android.content.Context
import androidx.room.Room
import edu.umg.programacion2.gastoscore.datos.AppDatabase
import edu.umg.programacion2.gastoscore.datos.GastoRepositoryRoom
import edu.umg.programacion2.gastos.servicio.ControlGastos

object GestorBaseDatos {

    @Volatile
    private var instancia: ControlGastos? = null

    @Volatile
    private var baseDatos: AppDatabase? = null

    fun obtener(context: Context): ControlGastos {
        return instancia ?: synchronized(this) {
            instancia ?: crearControlGastos(context).also {
                instancia = it
            }
        }
    }

    fun obtenerDao(context: Context) =
        obtenerBaseDatos(context).gastoDao()

    private fun obtenerBaseDatos(context: Context): AppDatabase {
        return baseDatos ?: synchronized(this) {
            baseDatos ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "control_gastos_db"
            ).build().also {
                baseDatos = it
            }
        }
    }

    private fun crearControlGastos(context: Context): ControlGastos {
        val repositorio = GastoRepositoryRoom(
            obtenerDao(context)
        )
        return ControlGastos(repositorio)
    }
}
