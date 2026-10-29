
package edu.umg.programacion2.gastoscore

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.umg.programacion2.gastos.modelo.Gasto
import java.time.YearMonth

private data class OpcionMenu(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
)

@Composable
fun NavegacionGastos(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val modelo: GastosViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )

    val estado by modelo.estado.collectAsState()

    val mesActual = YearMonth.now().toString()
    val gastosMes = estado.transacciones.filter {
        it.fecha.startsWith(mesActual)
    }
    val totalMes = gastosMes.sumOf { it.monto }

    val opciones = listOf(
        OpcionMenu("inicio", "Inicio", Icons.Default.Home),
        OpcionMenu("registrar", "Registrar", Icons.Default.AddCircle),
        OpcionMenu("movimientos", "Movimientos", Icons.Default.List),
        OpcionMenu("reportes", "Reportes", Icons.Default.Assessment)
    )

    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                opciones.forEach { opcion ->
                    NavigationBarItem(
                        selected = rutaActual == opcion.ruta,
                        onClick = {
                            navController.navigate(opcion.ruta) {
                                popUpTo(
                                    navController.graph.findStartDestination().id
                                ) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = opcion.icono,
                                contentDescription = opcion.titulo
                            )
                        },
                        label = { Text(opcion.titulo) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "inicio",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("inicio") {
                PantallaInicio(
                    total = totalMes,
                    cantidad = gastosMes.size,
                    gastos = gastosMes,
                    irARegistro = {
                        navController.navigate("registrar")
                    },
                    irAMovimientos = {
                        navController.navigate("movimientos")
                    }
                )
            }

            composable("registrar") {
                FormularioGasto(
                    modelo = modelo,
                    gastoInicial = null,
                    alGuardar = {
                        navController.navigate("movimientos") {
                            launchSingleTop = true
                        }
                    },
                    alCancelar = {
                        navController.navigate("inicio")
                    }
                )
            }

            composable("movimientos") {
                PantallaMovimientos(
                    gastos = estado.transacciones,
                    modelo = modelo,
                    alEditar = { gasto: Gasto ->
                        navController.navigate("editar/${gasto.id}")
                    }
                )
            }

            composable("editar/{gastoId}") { entrada ->
                val gastoId = entrada.arguments
                    ?.getString("gastoId")
                    ?.toIntOrNull()

                val gasto = estado.transacciones.firstOrNull {
                    it.id == gastoId
                }

                if (gasto != null) {
                    FormularioGasto(
                        modelo = modelo,
                        gastoInicial = gasto,
                        alGuardar = {
                            navController.navigate("movimientos") {
                                popUpTo("movimientos") {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        alCancelar = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    Text("No se encontró el gasto.")
                }
            }

            composable("reportes") {
                PantallaReportes(
                    gastos = estado.transacciones
                )
            }
        }
    }
}
