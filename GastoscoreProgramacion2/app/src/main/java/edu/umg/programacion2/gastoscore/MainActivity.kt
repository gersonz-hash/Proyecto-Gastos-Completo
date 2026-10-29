
package edu.umg.programacion2.gastoscore

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.umg.programacion2.gastos.modelo.Categoria
import edu.umg.programacion2.gastos.modelo.Gasto
import edu.umg.programacion2.gastoscore.ui.theme.GastoscoreProgramacion2Theme
import java.time.LocalDate
import java.time.YearMonth

private val Fondo = Color(0xFFF3F6FA)
private val AzulPrincipal = Color(0xFF1565C0)
private val AzulOscuro = Color(0xFF123B5D)
private val TextoSecundario = Color(0xFF64748B)
private val Verde = Color(0xFF16845B)
private val Rojo = Color(0xFFC62828)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GastoscoreProgramacion2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Fondo
                ) {
                    Scaffold(
                        containerColor = Fondo
                    ) { padding ->
                        FormularioGasto(
                            modifier = Modifier.padding(padding)
                        )
                    }
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
    var gastoEditandoId by remember { mutableStateOf<Int?>(null) }
    var categoriaFiltro by remember { mutableStateOf<String?>(null) }
    var menuFiltroAbierto by remember { mutableStateOf(false) }
    var textoBusqueda by remember { mutableStateOf("") }
    var mesFiltro by remember { mutableStateOf("Todos") }
    var menuMesAbierto by remember { mutableStateOf(false) }
    var gastoPendienteEliminar by remember { mutableStateOf<Gasto?>(null) }

    val context = LocalContext.current

    val modelo: GastosViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )

    val estado by modelo.estado.collectAsState()

    val mesesDisponibles = estado.transacciones
        .mapNotNull { gasto ->
            runCatching {
                YearMonth.parse(gasto.fecha.take(7))
            }.getOrNull()
        }
        .distinct()
        .sortedDescending()

    val gastosDelMes = estado.transacciones.filter { gasto ->
        mesFiltro == "Todos" ||
                runCatching {
                    YearMonth.parse(gasto.fecha.take(7)).toString()
                }.getOrNull() == mesFiltro
    }

    val resumenPorCategoria = gastosDelMes
        .groupBy { it.categoria }
        .mapValues { (_, gastos) ->
            gastos.sumOf { it.monto }
        }
        .toList()
        .sortedByDescending { it.second }

    val totalDelMes = gastosDelMes.sumOf { it.monto }

    val gastosFiltrados = estado.transacciones.filter { gasto ->
        val coincideCategoria =
            categoriaFiltro == null ||
                    gasto.categoria.name == categoriaFiltro

        val coincideBusqueda = gasto.descripcion.contains(
            textoBusqueda.trim(),
            ignoreCase = true
        )

        val coincideMes = mesFiltro == "Todos" ||
                runCatching {
                    YearMonth.parse(gasto.fecha.take(7)).toString()
                }.getOrNull() == mesFiltro

        coincideCategoria && coincideBusqueda && coincideMes
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "Control de gastos",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AzulOscuro
            )

            Text(
                text = "Administrá tu dinero de forma sencilla",
                fontSize = 14.sp,
                color = TextoSecundario
            )
        }

        // Tarjeta principal
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = AzulPrincipal
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Total de gastos registrados",
                    color = Color.White,
                    fontSize = 15.sp
                )

                Text(
                    text = "Q %.2f".format(estado.egresos),
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Revisá tus movimientos y categorías",
                    color = Color(0xFFE3F2FD),
                    fontSize = 13.sp
                )
            }
        }

        // Formulario
        TituloSeccion("Registrar movimiento")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Ej. Almuerzo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    label = { Text("Monto en quetzales") },
                    placeholder = { Text("Ej. 45.50") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Categoría",
                    color = AzulOscuro,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = { menuAbierto = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE5EFFB),
                        contentColor = AzulOscuro
                    )
                ) {
                    Text("Seleccionar: ${categoria.name}")
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
                        val cantidad = monto.trim()
                            .replace(",", ".")
                            .toDoubleOrNull()

                        if (
                            descripcion.isBlank() ||
                            cantidad == null ||
                            !cantidad.isFinite() ||
                            cantidad <= 0
                        ) {
                            Toast.makeText(
                                context,
                                "Ingresá una descripción y un monto válido",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val idActual = gastoEditandoId

                            val fechaOriginal = estado.transacciones
                                .firstOrNull { it.id == idActual }
                                ?.fecha

                            val gasto = Gasto(
                                id = idActual ?: 0,
                                descripcion = descripcion.trim(),
                                monto = cantidad,
                                categoria = categoria,
                                fecha = fechaOriginal ?: LocalDate.now().toString()
                            )

                            if (idActual == null) {
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
                            } else {
                                modelo.editar(gasto) { error ->
                                    if (error == null) {
                                        Toast.makeText(
                                            context,
                                            "Gasto actualizado correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        descripcion = ""
                                        monto = ""
                                        categoria = Categoria.ALIMENTACION
                                        gastoEditandoId = null
                                    } else {
                                        Toast.makeText(
                                            context,
                                            error,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPrincipal
                    )
                ) {
                    Text(
                        text = if (gastoEditandoId == null) {
                            "Guardar gasto"
                        } else {
                            "Actualizar gasto"
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (gastoEditandoId != null) {
                    TextButton(
                        onClick = {
                            descripcion = ""
                            monto = ""
                            categoria = Categoria.ALIMENTACION
                            gastoEditandoId = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar edición")
                    }
                }
            }
        }

        // Resumen del período
        TituloSeccion("Resumen por categoría")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Período: $mesFiltro",
                    color = TextoSecundario,
                    fontSize = 14.sp
                )

                Text(
                    text = "Total del período: Q %.2f".format(totalDelMes),
                    color = AzulOscuro,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                if (resumenPorCategoria.isEmpty()) {
                    Text(
                        text = "Todavía no hay gastos para este período.",
                        color = TextoSecundario
                    )
                } else {
                    resumenPorCategoria.forEach { (nombreCategoria, total) ->
                        val porcentaje = if (totalDelMes > 0.0) {
                            total * 100.0 / totalDelMes
                        } else {
                            0.0
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = nombreCategoria.name,
                                color = AzulOscuro,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Gastado: Q %.2f".format(total),
                                color = TextoSecundario
                            )

                            Text(
                                text = "Porcentaje: %.1f%%".format(porcentaje),
                                color = AzulPrincipal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Búsqueda y filtros
        TituloSeccion("Buscar movimientos")

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            label = { Text("Buscar por descripción") },
            placeholder = { Text("Ej. Almuerzo o transporte") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Text(
            text = "Filtrar por mes",
            color = AzulOscuro,
            fontWeight = FontWeight.SemiBold
        )

        Button(
            onClick = { menuMesAbierto = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE5EFFB),
                contentColor = AzulOscuro
            )
        ) {
            Text(
                if (mesFiltro == "Todos") {
                    "Todos los meses"
                } else {
                    "Mes: $mesFiltro"
                }
            )
        }

        DropdownMenu(
            expanded = menuMesAbierto,
            onDismissRequest = { menuMesAbierto = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todos los meses") },
                onClick = {
                    mesFiltro = "Todos"
                    menuMesAbierto = false
                }
            )

            mesesDisponibles.forEach { mes ->
                DropdownMenuItem(
                    text = { Text(mes.toString()) },
                    onClick = {
                        mesFiltro = mes.toString()
                        menuMesAbierto = false
                    }
                )
            }
        }

        Text(
            text = "Filtrar por categoría",
            color = AzulOscuro,
            fontWeight = FontWeight.SemiBold
        )

        Button(
            onClick = { menuFiltroAbierto = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE5EFFB),
                contentColor = AzulOscuro
            )
        ) {
            Text("Categoría: ${categoriaFiltro ?: "Todas"}")
        }

        DropdownMenu(
            expanded = menuFiltroAbierto,
            onDismissRequest = { menuFiltroAbierto = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas las categorías") },
                onClick = {
                    categoriaFiltro = null
                    menuFiltroAbierto = false
                }
            )

            Categoria.entries.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion.name) },
                    onClick = {
                        categoriaFiltro = opcion.name
                        menuFiltroAbierto = false
                    }
                )
            }
        }

        // Lista de movimientos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Movimientos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AzulOscuro
            )

            Text(
                text = "${gastosFiltrados.size} encontrados",
                fontSize = 12.sp,
                color = TextoSecundario
            )
        }

        if (gastosFiltrados.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Todavía no hay movimientos",
                        color = AzulOscuro,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Registrá un gasto para verlo aquí.",
                        color = TextoSecundario
                    )
                }
            }
        } else {
            gastosFiltrados.forEach { gasto ->
                TarjetaGasto(
                    gasto = gasto,
                    alEditar = {
                        descripcion = gasto.descripcion
                        monto = gasto.monto.toString()
                        categoria = gasto.categoria
                        gastoEditandoId = gasto.id
                    },
                    alEliminar = {
                        gastoPendienteEliminar = gasto
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    // Confirmación para eliminar
    if (gastoPendienteEliminar != null) {
        AlertDialog(
            onDismissRequest = {
                gastoPendienteEliminar = null
            },
            title = {
                Text("Eliminar gasto")
            },
            text = {
                Text(
                    "¿Estás seguro de que querés eliminar " +
                            "\"${gastoPendienteEliminar?.descripcion}\"?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val gasto = gastoPendienteEliminar
                        gastoPendienteEliminar = null

                        if (gasto != null) {
                            modelo.eliminar(gasto.id) { error ->
                                if (error == null) {
                                    if (gastoEditandoId == gasto.id) {
                                        descripcion = ""
                                        monto = ""
                                        categoria = Categoria.ALIMENTACION
                                        gastoEditandoId = null
                                    }

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
                    }
                ) {
                    Text(
                        "Sí, eliminar",
                        color = Rojo
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        gastoPendienteEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TituloSeccion(titulo: String) {
    Text(
        text = titulo,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = AzulOscuro
    )
}

@Composable
fun TarjetaGasto(
    gasto: Gasto,
    alEliminar: () -> Unit,
    alEditar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = gasto.descripcion,
                        color = AzulOscuro,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = gasto.categoria.name,
                        color = TextoSecundario,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "Q %.2f".format(gasto.monto),
                    color = Rojo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Fecha: ${gasto.fecha}",
                color = TextoSecundario,
                fontSize = 12.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = alEditar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPrincipal
                    )
                ) {
                    Text("Editar")
                }

                Button(
                    onClick = alEliminar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEBEE),
                        contentColor = Rojo
                    )
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}
