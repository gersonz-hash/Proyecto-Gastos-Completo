
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
private val Verde = Color(0xFF23844B)

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

// PANTALLA PRINCIPAL

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
                                color = Verde,
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
        mutableStateOf(
            gastoInicial?.categoria ?: Categoria.ALIMENTACION
        )
    }
    var menuAbierto by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Encabezado(
            if (gastoInicial == null) "Nuevo movimiento"
            else "Editar movimiento",
            "Ingresá los datos de tu gasto"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(17.dp)
            ) {
                Text(
                    text = if (gastoInicial == null)
                        "Detalles del gasto"
                    else
                        "Modificar detalles",
                    color = AzulOscuro,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "Descripción",
                        color = AzulOscuro,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        placeholder = { Text("Ej. Almuerzo") },
                        leadingIcon = {
                            Text("✎", fontSize = 20.sp)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        singleLine = true
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "Monto en quetzales",
                        color = AzulOscuro,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = monto,
                        onValueChange = { monto = it },
                        placeholder = { Text("0.00") },
                        leadingIcon = {
                            Text(
                                "Q",
                                color = AzulPrincipal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp),
                        singleLine = true
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "Categoría",
                        color = AzulOscuro,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Box {
                        OutlinedButton(
                            onClick = { menuAbierto = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text(
                                text = categoria.name,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start,
                                color = AzulOscuro
                            )
                            Text("⌄", fontSize = 20.sp)
                        }

                        DropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = {
                                menuAbierto = false
                            }
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
                }

                HorizontalDivider(color = Fondo)

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPrincipal
                    )
                ) {
                    Text(
                        text = if (gastoInicial == null)
                            "Guardar gasto"
                        else
                            "Actualizar gasto",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (gastoInicial != null) {
                    OutlinedButton(
                        onClick = alCancelar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
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
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "✓",
                    color = AzulPrincipal,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Tus gastos, bajo control",
                        color = AzulOscuro,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total de egresos registrados: Q %.2f"
                            .format(estado.egresos),
                        color = TextoSecundario,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// PANTALLA DE MOVIMIENTOS

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
            "Mis movimientos",
            "Consultá y administrá tus gastos"
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Buscar por descripción") },
            placeholder = { Text("Ej. Transporte") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp),
            singleLine = true
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { menuMes = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp)
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp)
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
            modifier = Modifier.padding(vertical = 10.dp),
            fontSize = 13.sp
        )

        if (filtrados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "☰",
                            fontSize = 32.sp,
                            color = AzulPrincipal
                        )
                        Text(
                            text = "No hay movimientos",
                            color = AzulOscuro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Probá con otro filtro o registrá un gasto.",
                            color = TextoSecundario,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

// PANTALLA DE REPORTES

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
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        Encabezado(
            "Mis reportes",
            "Descubrí en qué categorías gastás más"
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Período de consulta",
                    color = AzulOscuro,
                    fontWeight = FontWeight.SemiBold
                )

                Box {
                    OutlinedButton(
                        onClick = { menuMes = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
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
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = AzulPrincipal
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
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
                    .padding(23.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "TOTAL DEL PERÍODO",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Q %.2f".format(total),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${gastosDelMes.size} movimientos incluidos",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }
        }

        Text(
            text = "Gastos por categoría",
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "▤",
                        color = AzulPrincipal,
                        fontSize = 34.sp
                    )
                    Text(
                        text = "Todavía no hay datos",
                        color = AzulOscuro,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cuando registres gastos, aparecerá aquí el resumen.",
                        color = TextoSecundario,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            resumen.forEach { (categoria, cantidad) ->
                val porcentaje = if (total > 0) {
                    cantidad * 100 / total
                } else {
                    0.0
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(19.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(17.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = categoria.name,
                                color = AzulOscuro,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Q %.2f".format(cantidad),
                                color = AzulPrincipal,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = {
                                (porcentaje / 100).toFloat()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = AzulPrincipal,
                            trackColor = Color(0xFFE2EAF3)
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

// TARJETA INDIVIDUAL DE CADA GASTO

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
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
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
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    Surface(
                        color = Color(0xFFDCEBFA),
                        shape = RoundedCornerShape(13.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(46.dp),
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

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = gasto.descripcion,
                            color = AzulOscuro,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = gasto.categoria.name,
                            color = TextoSecundario,
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = "Q %.2f".format(gasto.monto),
                    color = Rojo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = Fondo)

            Text(
                text = "Fecha: ${gasto.fecha}",
                color = TextoSecundario,
                fontSize = 12.sp
            )

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
                    Text("Editar")
                }

                OutlinedButton(
                    onClick = alEliminar,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(13.dp)
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
