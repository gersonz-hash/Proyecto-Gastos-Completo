
package edu.umg.programacion2.gastoscore

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                    NavegacionGastos()
                }
            }
        }
    }
}

@Composable
fun NavegacionGastos() {
    var pantalla by remember { mutableStateOf("Inicio") }
    var gastoEditando by remember { mutableStateOf<Gasto?>(null) }

    val context = LocalContext.current
    val modelo: GastosViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )
    val estado by modelo.estado.collectAsState()

    Scaffold(
        containerColor = Fondo,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                listOf(
                    "Inicio",
                    "Registrar",
                    "Movimientos",
                    "Reportes"
                ).forEach { nombre ->
                    NavigationBarItem(
                        selected = pantalla == nombre,
                        onClick = {
                            pantalla = nombre
                            if (nombre != "Registrar") {
                                gastoEditando = null
                            }
                        },
                        icon = {
                            Text(
                                text = when (nombre) {
                                    "Inicio" -> "⌂"
                                    "Registrar" -> "+"
                                    "Movimientos" -> "☰"
                                    else -> "▤"
                                },
                                fontSize = 20.sp
                            )
                        },
                        label = { Text(nombre) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (pantalla) {
                "Inicio" -> PantallaInicio(
                    total = estado.egresos,
                    cantidad = estado.transacciones.size,
                    irARegistro = { pantalla = "Registrar" },
                    irAMovimientos = { pantalla = "Movimientos" }
                )

                "Registrar" -> FormularioGasto(
                    modelo = modelo,
                    gastoInicial = gastoEditando,
                    alGuardar = {
                        gastoEditando = null
                        pantalla = "Movimientos"
                    },
                    alCancelar = {
                        gastoEditando = null
                    }
                )

                "Movimientos" -> PantallaMovimientos(
                    gastos = estado.transacciones,
                    modelo = modelo,
                    alEditar = { gasto ->
                        gastoEditando = gasto
                        pantalla = "Registrar"
                    }
                )

                "Reportes" -> PantallaReportes(
                    gastos = estado.transacciones
                )
            }
        }
    }
}

@Composable
fun Encabezado(
    titulo: String,
    subtitulo: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = titulo,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = AzulOscuro
        )
        Text(
            text = subtitulo,
            fontSize = 14.sp,
            color = TextoSecundario
        )
    }
}

@Composable
fun TarjetaTotal(
    total: Double,
    texto: String = "Total de gastos registrados"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = AzulPrincipal
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = texto,
                color = Color.White,
                fontSize = 15.sp
            )
            Text(
                text = "Q %.2f".format(total),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PantallaInicio(
    total: Double,
    cantidad: Int,
    irARegistro: () -> Unit,
    irAMovimientos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "¡Hola!",
                    color = TextoSecundario,
                    fontSize = 15.sp
                )
                Text(
                    text = "Control de gastos",
                    color = AzulOscuro,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Organizá tu dinero fácilmente",
                    color = TextoSecundario,
                    fontSize = 14.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFDCEBFA),
                modifier = Modifier.size(58.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Q",
                        color = AzulPrincipal,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = AzulPrincipal
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1976D2),
                                Color(0xFF123B5D)
                            )
                        )
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "TOTAL DE EGRESOS",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Q %.2f".format(total),
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.25f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Movimientos registrados",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )

                    Surface(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = cantidad.toString(),
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 7.dp
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Text(
            text = "Resumen general",
            color = AzulOscuro,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFFDCEBFA),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Q",
                                color = AzulPrincipal,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Egresos",
                        color = TextoSecundario,
                        fontSize = 13.sp
                    )

                    Text(
                        text = "Q %.2f".format(total),
                        color = AzulOscuro,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFFE1F5E8),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓",
                                color = Color(0xFF23844B),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Movimientos",
                        color = TextoSecundario,
                        fontSize = 13.sp
                    )

                    Text(
                        text = cantidad.toString(),
                        color = AzulOscuro,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = "Accesos rápidos",
            color = AzulOscuro,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = irARegistro,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPrincipal
                    )
                ) {
                    Text(
                        text = "+   Registrar nuevo gasto",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = irAMovimientos,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(
                        text = "Ver todos los movimientos",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Text(
            text = "Tus finanzas, organizadas en un solo lugar.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = TextoSecundario,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FormularioGasto(
    modelo: GastosViewModel,
    gastoInicial: Gasto?,
    alGuardar: () -> Unit,
    alCancelar: () -> Unit
) {
    val context = LocalContext.current
    val estado by modelo.estado.collectAsState()

    var descripcion by remember(gastoInicial?.id) {
        mutableStateOf(gastoInicial?.descripcion ?: "")
    }
    var monto by remember(gastoInicial?.id) {
        mutableStateOf(gastoInicial?.monto?.toString() ?: "")
    }
    var categoria by remember(gastoInicial?.id) {
        mutableStateOf(gastoInicial?.categoria ?: Categoria.ALIMENTACION)
    }
    var menuAbierto by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Fondo)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Encabezado(
            if (gastoInicial == null) "Registrar gasto" else "Editar gasto",
            "Ingresá los datos del movimiento"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(18.dp)
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
                    singleLine = true
                )

                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    label = { Text("Monto en quetzales") },
                    placeholder = { Text("Ej. 45.50") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Categoría",
                    color = AzulOscuro,
                    fontWeight = FontWeight.SemiBold
                )

                Box {
                    OutlinedButton(
                        onClick = { menuAbierto = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(categoria.name)
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
                            val gasto = Gasto(
                                id = gastoInicial?.id ?: 0,
                                descripcion = descripcion.trim(),
                                monto = cantidad,
                                categoria = categoria,
                                fecha = gastoInicial?.fecha
                                    ?: LocalDate.now().toString()
                            )

                            val terminar: (String?) -> Unit = { error ->
                                if (error == null) {
                                    Toast.makeText(
                                        context,
                                        if (gastoInicial == null)
                                            "Gasto registrado correctamente"
                                        else
                                            "Gasto actualizado correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    alGuardar()
                                } else {
                                    Toast.makeText(
                                        context,
                                        error,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            if (gastoInicial == null) {
                                modelo.registrar(gasto, terminar)
                            } else {
                                modelo.editar(gasto, terminar)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (gastoInicial == null) "Guardar gasto"
                        else "Actualizar gasto"
                    )
                }

                if (gastoInicial != null) {
                    OutlinedButton(
                        onClick = alCancelar,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar edición")
                    }
                }
            }
        }

        TarjetaTotal(estado.egresos)
    }
}

@Composable
fun PantallaMovimientos(
    gastos: List<Gasto>,
    modelo: GastosViewModel,
    alEditar: (Gasto) -> Unit
) {
    val context = LocalContext.current
    var busqueda by remember { mutableStateOf("") }
    var categoriaFiltro by remember { mutableStateOf<String?>(null) }
    var mesFiltro by remember { mutableStateOf("Todos") }
    var menuCategoria by remember { mutableStateOf(false) }
    var menuMes by remember { mutableStateOf(false) }
    var pendienteEliminar by remember { mutableStateOf<Gasto?>(null) }

    val meses = gastos.mapNotNull {
        runCatching {
            YearMonth.parse(it.fecha.take(7))
        }.getOrNull()
    }.distinct().sortedDescending()

    val filtrados = gastos.filter { gasto ->
        val coincideTexto = gasto.descripcion.contains(
            busqueda.trim(),
            ignoreCase = true
        )
        val coincideCategoria = categoriaFiltro == null ||
                gasto.categoria.name == categoriaFiltro
        val coincideMes = mesFiltro == "Todos" ||
                gasto.fecha.startsWith(mesFiltro)

        coincideTexto && coincideCategoria && coincideMes
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Encabezado(
            "Movimientos",
            "Buscá, editá o eliminá tus gastos"
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Buscar por descripción") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { menuMes = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (mesFiltro == "Todos") "Todos los meses"
                        else mesFiltro
                    )
                }

                DropdownMenu(
                    expanded = menuMes,
                    onDismissRequest = { menuMes = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos los meses") },
                        onClick = {
                            mesFiltro = "Todos"
                            menuMes = false
                        }
                    )

                    meses.forEach { mes ->
                        DropdownMenuItem(
                            text = { Text(mes.toString()) },
                            onClick = {
                                mesFiltro = mes.toString()
                                menuMes = false
                            }
                        )
                    }
                }
            }

            Box(Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { menuCategoria = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(categoriaFiltro ?: "Todas")
                }

                DropdownMenu(
                    expanded = menuCategoria,
                    onDismissRequest = { menuCategoria = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas") },
                        onClick = {
                            categoriaFiltro = null
                            menuCategoria = false
                        }
                    )

                    Categoria.entries.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion.name) },
                            onClick = {
                                categoriaFiltro = opcion.name
                                menuCategoria = false
                            }
                        )
                    }
                }
            }
        }

        Text(
            text = "${filtrados.size} movimientos encontrados",
            color = TextoSecundario,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (filtrados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay movimientos que coincidan.",
                    color = TextoSecundario
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtrados, key = { it.id }) { gasto ->
                    TarjetaGasto(
                        gasto = gasto,
                        alEditar = { alEditar(gasto) },
                        alEliminar = { pendienteEliminar = gasto }
                    )
                }
            }
        }
    }

    pendienteEliminar?.let { gasto ->
        AlertDialog(
            onDismissRequest = { pendienteEliminar = null },
            title = { Text("Eliminar gasto") },
            text = {
                Text("¿Querés eliminar \"${gasto.descripcion}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendienteEliminar = null
                        modelo.eliminar(gasto.id) { error ->
                            Toast.makeText(
                                context,
                                error ?: "Gasto eliminado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = Rojo
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendienteEliminar = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PantallaReportes(gastos: List<Gasto>) {
    var mesFiltro by remember { mutableStateOf("Todos") }
    var menuMes by remember { mutableStateOf(false) }

    val meses = gastos.mapNotNull {
        runCatching {
            YearMonth.parse(it.fecha.take(7))
        }.getOrNull()
    }.distinct().sortedDescending()

    val gastosDelMes = gastos.filter {
        mesFiltro == "Todos" || it.fecha.startsWith(mesFiltro)
    }

    val total = gastosDelMes.sumOf { it.monto }

    val resumen = gastosDelMes
        .groupBy { it.categoria }
        .mapValues { (_, lista) -> lista.sumOf { it.monto } }
        .toList()
        .sortedByDescending { it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Fondo)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Encabezado(
            "Reportes",
            "Revisá en qué categorías gastás más"
        )

        Box {
            OutlinedButton(
                onClick = { menuMes = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (mesFiltro == "Todos") "Todos los meses"
                    else "Período: $mesFiltro"
                )
            }

            DropdownMenu(
                expanded = menuMes,
                onDismissRequest = { menuMes = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Todos los meses") },
                    onClick = {
                        mesFiltro = "Todos"
                        menuMes = false
                    }
                )

                meses.forEach { mes ->
                    DropdownMenuItem(
                        text = { Text(mes.toString()) },
                        onClick = {
                            mesFiltro = mes.toString()
                            menuMes = false
                        }
                    )
                }
            }
        }

        TarjetaTotal(
            total = total,
            texto = "Total del período"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Resumen por categoría",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulOscuro
                )

                if (resumen.isEmpty()) {
                    Text(
                        text = "Todavía no hay gastos para este período.",
                        color = TextoSecundario
                    )
                } else {
                    resumen.forEach { (categoria, cantidad) ->
                        val porcentaje = if (total > 0) {
                            cantidad * 100 / total
                        } else {
                            0.0
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = categoria.name,
                                color = AzulOscuro,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Gastado: Q %.2f".format(cantidad),
                                color = TextoSecundario
                            )

                            LinearProgressIndicator(
                                progress = {
                                    (porcentaje / 100).toFloat()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = AzulPrincipal
                            )

                            Text(
                                text = "%.1f%% del total".format(porcentaje),
                                color = TextoSecundario,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
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
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar")
                }

                OutlinedButton(
                    onClick = alEliminar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Eliminar",
                        color = Rojo
                    )
                }
            }
        }
    }
}
