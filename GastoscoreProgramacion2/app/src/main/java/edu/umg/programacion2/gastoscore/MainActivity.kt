
package edu.umg.programacion2.gastoscore

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val PREFERENCIAS_TEMA = "preferencias_tema"
private const val CLAVE_MODO_OSCURO = "modo_oscuro"

private val AzulPrincipal = Color(0xFF64B5F6)
private val AzulOscuro = Color(0xFFF1F5F9)
private val TextoSecundario = Color(0xFFB0BEC5)
private val Rojo = Color(0xFFFF8A80)
private val Verde = Color(0xFF69D9A0)

private val ColoresGrafica = listOf(
    Color(0xFF64B5F6),
    Color(0xFF26A69A),
    Color(0xFFFFA726),
    Color(0xFFAB47BC),
    Color(0xFFEF5350),
    Color(0xFF78909C),
    Color(0xFF66BB6A),
    Color(0xFF5C6BC0)
)

private fun formatoQ(valor: Double): String =
    "Q " + String.format(Locale.US, "%.2f", valor)

// Convierte yyyy-MM-dd a dd/MM/yyyy para mostrar la fecha.
private fun formatoFecha(fecha: String): String {
    return try {
        LocalDate.parse(fecha).format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
    } catch (e: Exception) {
        fecha
    }
}

private fun iconoCategoria(nombre: String): String {
    val c = nombre.uppercase()
    return when {
        "ALIMENT" in c -> "🍔"
        "TRANSPORTE" in c -> "🚗"
        "SALUD" in c -> "💊"
        "EDUC" in c -> "📚"
        "VIVIENDA" in c -> "🏠"
        "ENTRETEN" in c -> "🎮"
        "SERVICIO" in c -> "💡"
        "ROPA" in c -> "👕"
        "AHORRO" in c -> "🐷"
        else -> "🛍️"
    }
}

private fun colorCategoria(nombre: String): Color {
    val c = nombre.uppercase()
    return when {
        "ALIMENT" in c -> Color(0xFFFFE8D6)
        "TRANSPORTE" in c -> Color(0xFFDCEBFA)
        "SALUD" in c -> Color(0xFFFFE0E0)
        "EDUC" in c -> Color(0xFFE8E0FF)
        "VIVIENDA" in c -> Color(0xFFFFE8C7)
        "ENTRETEN" in c -> Color(0xFFF3DDFB)
        "SERVICIO" in c -> Color(0xFFFFF3C4)
        "ROPA" in c -> Color(0xFFDDF5EE)
        "AHORRO" in c -> Color(0xFFDDF5E4)
        else -> Color(0xFFE7EDF5)
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val contexto = this@MainActivity

            var modoOscuro by rememberSaveable {
                mutableStateOf(
                    contexto.getSharedPreferences(
                        PREFERENCIAS_TEMA,
                        MODE_PRIVATE
                    ).getBoolean(CLAVE_MODO_OSCURO, true)
                )
            }

            GastoscoreProgramacion2Theme(
                darkTheme = modoOscuro
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.background
                                )
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 4.dp
                                ),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (modoOscuro) {
                                    "Modo oscuro"
                                } else {
                                    "Modo claro"
                                },
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = {
                                    modoOscuro = !modoOscuro

                                    contexto.getSharedPreferences(
                                        PREFERENCIAS_TEMA,
                                        MODE_PRIVATE
                                    ).edit()
                                        .putBoolean(
                                            CLAVE_MODO_OSCURO,
                                            modoOscuro
                                        )
                                        .apply()
                                }
                            ) {
                                Text(
                                    if (modoOscuro) "☀️ Claro" else "🌙 Oscuro"
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            NavegacionGastos()
                        }
                    }
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

    val mesActual = YearMonth.now().toString()

    val gastosMes = estado.transacciones.filter {
        it.fecha.startsWith(mesActual)
    }

    val totalMes = gastosMes.sumOf { it.monto }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
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
                            label = {
                                Text(nombre)
                            }
                        )
                    }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (pantalla) {
                "Inicio" -> PantallaInicio(
                    total = totalMes,
                    cantidad = gastosMes.size,
                    gastos = gastosMes,
                    irARegistro = {
                        pantalla = "Registrar"
                    },
                    irAMovimientos = {
                        pantalla = "Movimientos"
                    }
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

@Composable
fun Encabezado(
    titulo: String,
    subtitulo: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            titulo,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            subtitulo,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// INICIO: RESUMEN DEL MES ACTUAL

@Composable
fun PantallaInicio(
    total: Double,
    cantidad: Int,
    gastos: List<Gasto>,
    irARegistro: () -> Unit,
    irAMovimientos: () -> Unit
) {
    val categorias = gastos
        .groupBy { it.categoria }
        .mapValues { (_, lista) -> lista.sumOf { it.monto } }

    val categoriaMayor = categorias.maxByOrNull { it.value }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    "¡Hola!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "Control de gastos",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Tu resumen financiero",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(58.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Q",
                        color = MaterialTheme.colorScheme.primary,
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
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF1976D2),
                                Color(0xFF0D2840)
                            )
                        )
                    )
                    .padding(23.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "GASTOS DEL MES",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )

                Text(
                    formatoQ(total),
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    YearMonth.now().toString(),
                    color = Color.White.copy(alpha = 0.9f)
                )

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.25f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Gastos registrados",
                        color = Color.White
                    )

                    Text(
                        cantidad.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            "Resumen financiero",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaResumen(
                titulo = "Gastos del mes",
                valor = formatoQ(total),
                simbolo = "💸",
                modifier = Modifier.weight(1f)
            )

            TarjetaResumen(
                titulo = "Movimientos",
                valor = cantidad.toString(),
                simbolo = "📋",
                modifier = Modifier.weight(1f),
                colorSimbolo = Verde
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "🏆 Categoría con más gastos",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                if (categoriaMayor == null) {
                    Text(
                        "Todavía no hay gastos registrados este mes.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "${iconoCategoria(categoriaMayor.key.name)} " +
                                categoriaMayor.key.name,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        formatoQ(categoriaMayor.value),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "💡 Consejo financiero",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Revisá tus reportes para identificar las categorías " +
                            "donde podés reducir tus gastos.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            "Accesos rápidos",
            color = MaterialTheme.colorScheme.onBackground,
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
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                "➕ Registrar nuevo gasto",
                color = MaterialTheme.colorScheme.onPrimary
            )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Surface(
                color = if (colorSimbolo == Verde) {
                    Color(0xFF183D2D)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(simbolo, fontSize = 22.sp)
                }
            }

            Text(
                titulo,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Text(
                valor,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// REGISTRAR Y EDITAR GASTOS

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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        Encabezado(
            if (gastoInicial == null) "Nuevo gasto" else "Editar gasto",
            "Ingresá los datos de tu gasto"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                Text(
                    "Descripción",
                    color = MaterialTheme.colorScheme.onSurface,
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Text(
                            "Q",
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true
                )

                Text(
                    "Categoría",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Box {
                    OutlinedButton(
                        onClick = { menuAbierto = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text(
                            "${iconoCategoria(categoria.name)} ${categoria.name}",
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
                                        "${iconoCategoria(opcion.name)} ${opcion.name}"
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
                            // Para un gasto nuevo se obtiene la fecha actual
                            // justo cuando se presiona Guardar gasto.
                            // Al editar, se conserva la fecha original.
                            val fechaGasto = if (gastoInicial == null) {
                                LocalDate.now().toString()
                            } else {
                                gastoInicial.fecha
                            }

                            val gasto = Gasto(
                                id = gastoInicial?.id ?: 0,
                                descripcion = descripcion.trim(),
                                monto = cantidad,
                                categoria = categoria,
                                fecha = fechaGasto
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
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (gastoInicial == null) {
                            "Guardar gasto"
                        } else {
                            "Actualizar gasto"
                        },
                        color = MaterialTheme.colorScheme.onPrimary
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

        Text(
            "Total histórico de gastos: ${formatoQ(estado.egresos)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        runCatching {
            YearMonth.parse(it.fecha.take(7))
        }.getOrNull()
    }.distinct().sortedDescending()

    val filtrados = gastos.filter { gasto ->
        gasto.descripcion.contains(
            busqueda.trim(),
            ignoreCase = true
        ) &&
                (categoriaFiltro == null ||
                        gasto.categoria.name == categoriaFiltro) &&
                (mesFiltro == "Todos" ||
                        gasto.fecha.startsWith(mesFiltro))
    }

    val totalFiltrado = filtrados.sumOf { it.monto }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Encabezado(
            "Movimientos",
            "Consultá y administrá tus gastos"
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por descripción...") },
            leadingIcon = { Text("🔎") },
            shape = RoundedCornerShape(15.dp),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { menuMes = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (mesFiltro == "Todos") {
                            "📅 Meses"
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

            Box(Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { menuCategoria = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        categoriaFiltro ?: "Categorías",
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
                                    "${iconoCategoria(opcion.name)} ${opcion.name}"
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

        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(17.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "${filtrados.size} movimientos",
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        "Total mostrado",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Text(
                    formatoQ(totalFiltrado),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        if (filtrados.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay movimientos para mostrar.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    filtrados,
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
            title = {
                Text("Eliminar gasto")
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
                    Text(
                        "Eliminar",
                        color = Rojo
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendienteEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// REPORTES Y ESTADÍSTICAS

@Composable
fun PantallaReportes(gastos: List<Gasto>) {
    var mesFiltro by remember { mutableStateOf("Todos") }
    var menuMes by remember { mutableStateOf(false) }

    val meses = gastos.mapNotNull {
        runCatching {
            YearMonth.parse(it.fecha.take(7))
        }.getOrNull()
    }.distinct().sortedDescending()

    val gastosPeriodo = gastos.filter {
        mesFiltro == "Todos" || it.fecha.startsWith(mesFiltro)
    }

    val total = gastosPeriodo.sumOf { it.monto }

    val resumen = gastosPeriodo
        .groupBy { it.categoria }
        .mapValues { (_, lista) -> lista.sumOf { it.monto } }
        .toList()
        .sortedByDescending { it.second }

    val conteoMeses = gastos.groupBy { it.fecha.take(7) }
        .mapValues { (_, lista) -> lista.sumOf { it.monto } }
        .toList()
        .sortedByDescending { it.first }
        .take(6)
        .reversed()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Encabezado(
            "Mis reportes",
            "Gráficas y estadísticas de tus gastos"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "📅 Período de consulta",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Box {
                    OutlinedButton(
                        onClick = { menuMes = true },
                        modifier = Modifier.fillMaxWidth()
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
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF1976D2),
                                Color(0xFF0D2840)
                            )
                        )
                    )
                    .padding(21.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "TOTAL DEL PERÍODO",
                    color = Color.White.copy(alpha = 0.85f)
                )

                Text(
                    formatoQ(total),
                    color = Color.White,
                    fontSize = 31.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "${gastosPeriodo.size} gastos registrados",
                    color = Color.White
                )
            }
        }

        Text(
            "Distribución por categoría",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        if (resumen.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    "📊 Todavía no hay datos suficientes para mostrar gráficas.",
                    modifier = Modifier.padding(22.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GraficaCircular(resumen, total)

                    Spacer(Modifier.height(10.dp))

                    resumen.forEachIndexed { index, item ->
                        val porcentaje = if (total > 0) {
                            item.second * 100.0 / total
                        } else {
                            0.0
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(13.dp),
                                color = ColoresGrafica[
                                    index % ColoresGrafica.size
                                ],
                                shape = RoundedCornerShape(4.dp)
                            ) {}

                            Spacer(Modifier.width(8.dp))

                            Text(
                                "${iconoCategoria(item.first.name)} ${item.first.name}",
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )

                            Text(
                                String.format(Locale.US, "%.1f%%", porcentaje),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                formatoQ(item.second),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Text(
            "Comparación mensual",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        if (conteoMeses.isEmpty()) {
            Text(
                "No hay datos mensuales disponibles.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    val maximo = conteoMeses
                        .maxOf { it.second }
                        .coerceAtLeast(1.0)

                    conteoMeses.forEach { (mes, monto) ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    mes,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    formatoQ(monto),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = {
                                    (monto / maximo)
                                        .toFloat()
                                        .coerceIn(0f, 1f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(9.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun GraficaCircular(
    resumen: List<Pair<Categoria, Double>>,
    total: Double
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(190.dp)
        ) {
            if (total > 0.0) {
                var anguloInicial = -90f
                val grosor = 28.dp.toPx()

                resumen.forEachIndexed { index, item ->
                    val barrido = (
                            item.second / total * 360.0
                            ).toFloat()

                    drawArc(
                        color = ColoresGrafica[
                            index % ColoresGrafica.size
                        ],
                        startAngle = anguloInicial,
                        sweepAngle = barrido,
                        useCenter = false,
                        topLeft = Offset(
                            grosor / 2,
                            grosor / 2
                        ),
                        size = Size(
                            size.width - grosor,
                            size.height - grosor
                        ),
                        style = Stroke(
                            width = grosor,
                            cap = StrokeCap.Butt
                        )
                    )

                    anguloInicial += barrido
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Total",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Text(
                formatoQ(total),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }
    }
}

// TARJETA DE MOVIMIENTO

@Composable
fun TarjetaGasto(
    gasto: Gasto,
    alEliminar: () -> Unit,
    alEditar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = colorCategoria(gasto.categoria.name),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Box(
                        modifier = Modifier.size(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            iconoCategoria(gasto.categoria.name),
                            fontSize = 24.sp
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        gasto.descripcion,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        gasto.categoria.name,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )

                    // Mostrar la fecha como dd/MM/yyyy.
                    Text(
                        formatoFecha(gasto.fecha),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Text(
                    formatoQ(gasto.monto),
                    color = Rojo,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = alEditar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "✏️ Editar",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                OutlinedButton(
                    onClick = alEliminar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        Color(0xFFFF8A80)
                    )
                ) {
                    Text(
                        "🗑️ Eliminar",
                        color = Rojo
                    )
                }
            }
        }
    }
}
