
package edu.umg.programacion2.gastoscore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PantallaInicio() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Control de Gastos",
            fontSize = 26.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Bienvenido a tu aplicación.")
        Text(text = "Usa el menú inferior para navegar.")
    }
}

@Composable
fun PantallaMovimientos() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Movimientos",
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Aquí podrás consultar tus movimientos.")
    }
}

@Composable
fun PantallaReportes() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Reportes",
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Aquí podrás consultar tus reportes.")
    }
}
