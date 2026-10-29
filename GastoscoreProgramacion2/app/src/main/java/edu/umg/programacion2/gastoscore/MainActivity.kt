package edu.umg.programacion2.gastoscore

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
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
private val Verde = Color(0xFF23844B)

// ICONOS Y COLORES POR CATEGORÍA

private fun iconoCategoria(nombre: String): String {
    val categoria = nombre.uppercase()

    return when {
        "ALIMENT" in categoria -> "🍔"
        "TRANSPORTE" in categoria -> "🚗"
        "SALUD" in categoria -> "💊"
        "EDUC" in categoria -> "📚"
        "VIVIENDA" in categoria -> "🏠"
        "ENTRETEN" in categoria -> "🎮"
        "SERVICIO" in categoria -> "💡"
        "ROPA" in categoria -> "👕"
        "AHORRO" in categoria -> "🐷"
        else -> "🛍️"
    }
}

private fun colorCategoria(nombre: String): Color {
    val categoria = nombre.uppercase()

    return when {
        "ALIMENT" in categoria -> Color(0xFFFFE8D6)
        "TRANSPORTE" in categoria -> Color(0xFFDCEBFA)
        "SALUD" in categoria -> Color(0xFFFFE0E0)
        "EDUC" in categoria -> Color(0xFFE8E0FF)
        "VIVIENDA" in categoria -> Color(0xFFFFE8C7)
        "ENTRETEN" in categoria -> Color(0xFFF3DDFB)
        "SERVICIO" in categoria -> Color(0xFFFFF3C4)
        "ROPA" in categoria -> Color(0xFFDDF5EE)
        "AHORRO" in categoria -> Color(0xFFDDF5E4)
        else -> Color(0xFFE7EDF5)
    }
}

// ACTIVIDAD PRINCIPAL

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

// NAVEGACIÓN

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
            NavigationBar(containerColor = Color.White) {
                listOf("Inicio", "Registrar", "Movimientos", "Reportes")
                    .forEach { nombre ->
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
                                    when (nombre) {
                                        "Inicio" -> "🏠"
                                        "Registrar" -> "➕"
                                        "Movimientos" -> "📋"
                                        else -> "📊"
                                    },
                                    fontSize = 18.sp
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
                        pantalla = "Movimientos"
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

// ENCABEZADO

@Composable
fun Encabezado(titulo: String, subtitulo: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
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

// INICIO

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
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("¡Hola!", color = TextoSecundario)
                Text(
                    "Control de gastos",
                    color = AzulOscuro,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Organizá tu dinero fácilmente",
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
                        "Q",
                        color = AzulPrincipal,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = AzulPrincipal
            )
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1976D2), AzulOscuro)
                        )
                    )
                    .padding(23.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "TOTAL DE EGRESOS",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
                Text(
                    "Q %.2f".format(total),
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Movimientos registrados",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        cantidad.toString(),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            "Resumen general",
            color = AzulOscuro,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaResumen(
                titulo = "Egresos",
                valor = "Q %.2f".format(total),
                simbolo = "💸",
                modifier = Modifier.weight(1f)
            )
            TarjetaResumen(
                titulo = "Movimientos",
                valor = cantidad.toString(),
                simbolo = "✓",
                modifier = Modifier.weight(1f),
                colorSimbolo = Verde
            )
        }

        Text(
            "Accesos rápidos",
            color = AzulOscuro,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

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
            Text("➕ Registrar nuevo gasto")
        }

        OutlinedButton(
            onClick = irAMovimientos,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text("📋 Ver todos los movimientos")
        }
    }
}

@Composable
private fun TarjetaResumen(
    titulo: String,
    valor: String,
    simbolo: String,
    modifier: Modifier = Modifier,
    colorSimbolo: Color = AzulPrincipal
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Surface(
                color = if (colorSimbolo == Verde) {
                    Color(0xFFE1F5E8)
                } else {
                    Color(0xFFDCEBFA)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        simbolo,
                        color = colorSimbolo,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(titulo, color = TextoSecundario, fontSize = 13.sp)
            Text(
                valor,
                color = AzulOscuro,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// REGISTRAR Y EDITAR

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
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        Encabezado(
            if (gastoInicial == null) "Nuevo movimiento"
            else "Editar movimiento",
            "Ingresá los datos de tu gasto"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Descripción",
                    color = AzulOscuro,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej. Almuerzo") },
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true
                )

                Text(
                    "Monto en quetzales",
                    color = AzulOscuro,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Text("Q", color = AzulPrincipal)
                    },
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true
                )

                Text(
                    "Categoría",
                    color = AzulOscuro,
                    fontWeight = FontWeight.SemiBold
                )

                Box {
                    OutlinedButton(
                        onClick = { menuAbierto = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text(
                            "${iconoCategoria(categoria.name)}  ${categoria.name}",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                        Text("⌄")
                    }

                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        Categoria.entries.forEach { opcion ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${iconoCategoria(opcion.name)}  ${opcion.name}"
                                    )
                                },
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
                                        if (gastoInicial == null) {
                                            "Gasto registrado correctamente"
                                        } else {
                                            "Gasto actualizado correctamente"
                                        },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPrincipal
                    )
                ) {
                    Text(
                        if (gastoInicial == null) "Guardar gasto"
                        else "Actualizar gasto"
                    )
                }

                if (gastoInicial != null) {
                    OutlinedButton(
                        onClick = alCancelar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text("Cancelar edición")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE5EFFB)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    "Tus gastos, bajo control",
                    color = AzulOscuro,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Total de egresos registrados: Q %.2f".format(
                        estado.egresos
                    ),
                    color = TextoSecundario,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// MOVIMIENTOS

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
        runCatching { YearMonth.parse(it.fecha.take(7)) }.getOrNull()
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

    val totalFiltrado = filtrados.sumOf { it.monto }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Encabezado(
            "Movimientos",
            "Consultá y administrá tus gastos"
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por descripción...") },
            leadingIcon = {
                Text("🔎", fontSize = 20.sp)
            },
            trailingIcon = {
                if (busqueda.isNotEmpty()) {
                    TextButton(onClick = { busqueda = "" }) {
                        Text("Limpiar")
                    }
                }
            },
            shape = RoundedCornerShape(17.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulPrincipal,
                unfocusedBorderColor = Color(0xFFD8E1EC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Filtrar movimientos",
            color = AzulOscuro,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { menuMes = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text(
                        if (mesFiltro == "Todos") "📅 Mes: todos"
                        else "📅 $mesFiltro",
                        fontSize = 12.sp,
                        maxLines = 1
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
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text(
                        if (categoriaFiltro == null) {
                            "🗂️ Categorías"
                        } else {
                            "${iconoCategoria(categoriaFiltro!!)} $categoriaFiltro"
                        },
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                DropdownMenu(
                    expanded = menuCategoria,
                    onDismissRequest = { menuCategoria = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas las categorías") },
                        onClick = {
                            categoriaFiltro = null
                            menuCategoria = false
                        }
                    )

                    Categoria.entries.forEach { opcion ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${iconoCategoria(opcion.name)}  ${opcion.name}"
                                )
                            },
                            onClick = {
                                categoriaFiltro = opcion.name
                                menuCategoria = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Resultados",
                        color = TextoSecundario,
                        fontSize = 13.sp
                    )
                    Text(
                        "${filtrados.size} movimientos",
                        color = AzulOscuro,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total mostrado",
                        color = TextoSecundario,
                        fontSize = 12.sp
                    )
                    Text(
                        "Q %.2f".format(totalFiltrado),
                        color = AzulPrincipal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (filtrados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("📋", fontSize = 38.sp)

                        Text(
                            "No hay movimientos",
                            color = AzulOscuro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Text(
                            "Probá con otra búsqueda o cambiá los filtros.",
                            color = TextoSecundario,
                            textAlign = TextAlign.Center
                        )

                        OutlinedButton(
                            onClick = {
                                busqueda = ""
                                categoriaFiltro = null
                                mesFiltro = "Todos"
                            }
                        ) {
                            Text("Limpiar filtros")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filtrados,
                    key = { it.id }
                ) { gasto ->
                    TarjetaGasto(
                        gasto = gasto,
                        alEliminar = {
                            pendienteEliminar = gasto
                        },
                        alEditar = {
                            alEditar(gasto)
                        }
                    )
                }
            }
        }
    }

    pendienteEliminar?.let { gasto ->
        AlertDialog(
            onDismissRequest = {
                pendienteEliminar = null
            },
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    "Eliminar movimiento",
                    color = AzulOscuro,
                    fontWeight = FontWeight.Bold
                )
            },
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
                    Text("Eliminar", color = Rojo)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendienteEliminar = null
                    }
                ) {
                    Text("Cancelar", color = AzulPrincipal)
                }
            }
        )
    }
}

// REPORTES

@Composable
fun PantallaReportes(gastos: List<Gasto>) {
    var mesFiltro by remember { mutableStateOf("Todos") }
    var menuMes by remember { mutableStateOf(false) }

    val meses = gastos.mapNotNull {
        runCatching { YearMonth.parse(it.fecha.take(7)) }.getOrNull()
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
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Encabezado(
            "Mis reportes",
            "Descubrí en qué categorías gastás más"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "📅 Período de consulta",
                    color = AzulOscuro,
                    fontWeight = FontWeight.SemiBold
                )

                Box {
                    OutlinedButton(
                        onClick = { menuMes = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(13.dp)
                    ) {
                        Text(
                            if (mesFiltro == "Todos") {
                                "Todos los meses"
                            } else {
                                mesFiltro
                            }
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
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = AzulPrincipal
            )
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1976D2), AzulOscuro)
                        )
                    )
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "TOTAL DEL PERÍODO",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )

                Text(
                    "Q %.2f".format(total),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.25f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Movimientos incluidos",
                        color = Color.White
                    )
                    Text(
                        gastosDelMes.size.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            "Gastos por categoría",
            color = AzulOscuro,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        if (resumen.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text("📊", fontSize = 40.sp)

                    Text(
                        "Todavía no hay datos",
                        color = AzulOscuro,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Cuando registres gastos, aparecerá aquí el resumen.",
                        color = TextoSecundario,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            resumen.forEach { (categoria, cantidad) ->
                val porcentaje = if (total > 0) {
                    cantidad * 100.0 / total
                } else {
                    0.0
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = colorCategoria(categoria.name),
                                shape = RoundedCornerShape(13.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(46.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        iconoCategoria(categoria.name),
                                        fontSize = 24.sp
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    categoria.name,
                                    color = AzulOscuro,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "%.1f%% del total".format(porcentaje),
                                    color = TextoSecundario,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                "Q %.2f".format(cantidad),
                                color = AzulPrincipal,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = {
                                (porcentaje / 100.0)
                                    .toFloat()
                                    .coerceIn(0f, 1f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = AzulPrincipal,
                            trackColor = Color(0xFFE2EAF3)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// TARJETA DE CADA GASTO

@Composable
fun TarjetaGasto(
    gasto: Gasto,
    alEliminar: () -> Unit,
    alEditar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = colorCategoria(gasto.categoria.name),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                iconoCategoria(gasto.categoria.name),
                                fontSize = 25.sp
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            gasto.descripcion,
                            color = AzulOscuro,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            gasto.categoria.name,
                            color = TextoSecundario,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    "Q %.2f".format(gasto.monto),
                    color = Rojo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = Fondo)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("📅", fontSize = 14.sp)
                Text(
                    gasto.fecha,
                    color = TextoSecundario,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Button(
                    onClick = alEditar,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPrincipal
                    )
                ) {
                    Text("✏️ Editar")
                }

                OutlinedButton(
                    onClick = alEliminar,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                ) {
                    Text("🗑️ Eliminar", color = Rojo)
                }
            }
        }
    }
}