package edu.umg.programacion2.gastoscore

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.umg.programacion2.gastos.modelo.Categoria
import edu.umg.programacion2.gastos.modelo.Gasto
import edu.umg.programacion2.gastoscore.ui.theme.GastoscoreProgramacion2Theme
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GastoscoreProgramacion2Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    FormularioGasto(
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
fun FormularioGasto(modifier: Modifier = Modifier) {

    var descripcion by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var categoria by remember {
        mutableStateOf(Categoria.ALIMENTACION)
    }
    var menuAbierto by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val modelo: GastosViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )

    val estado by modelo.estado.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text("Control de gastos")

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = monto,
            onValueChange = { monto = it },
            label = { Text("Monto en quetzales") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { menuAbierto = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Categoría: $categoria")
        }

        DropdownMenu(
            expanded = menuAbierto,
            onDismissRequest = { menuAbierto = false }
        ) {
            Categoria.entries.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion.name) },
                    onClick = {
                        categoria = opcion
                        menuAbierto = false
                    }
                )
            }
        }

        Button(
            onClick = {
                val cantidad = monto.toDoubleOrNull()

                if (
                    descripcion.isBlank() ||
                    cantidad == null ||
                    cantidad <= 0
                ) {
                    Toast.makeText(
                        context,
                        "Ingresa una descripción y un monto válido",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val gasto = Gasto(
                        descripcion = descripcion.trim(),
                        monto = cantidad,
                        categoria = categoria,
                        fecha = LocalDate.now().toString()
                    )

                    modelo.registrar(gasto) { error ->
                        if (error == null) {
                            Toast.makeText(
                                context,
                                "Gasto registrado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            descripcion = ""
                            monto = ""
                        } else {
                            Toast.makeText(
                                context,
                                error,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar gasto")
        }

        Text("Total de gastos: Q %.2f".format(estado.egresos))
        Text("Gastos registrados:")

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(estado.transacciones) { gasto ->
                TarjetaGasto(
                    gasto = gasto,
                    alEliminar = {
                        modelo.eliminar(gasto.id) { error ->
                            if (error == null) {
                                Toast.makeText(
                                    context,
                                    "Gasto eliminado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    error,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TarjetaGasto(
    gasto: Gasto,
    alEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Descripción: ${gasto.descripcion}")
            Text("Monto: Q %.2f".format(gasto.monto))
            Text("Categoría: ${gasto.categoria}")
            Text("Fecha: ${gasto.fecha}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = alEliminar) {
                    Text("Eliminar")
                }
            }
        }
    }
}