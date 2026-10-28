package edu.umg.programacion2.gastoscore

import android.content.Context
import edu.umg.programacion2.gastos.servicio.ControlGastos

object Dependencias {

    fun obtenerControl(context: Context): ControlGastos {
        return ControlGastosApp.obtener(context)
    }
}