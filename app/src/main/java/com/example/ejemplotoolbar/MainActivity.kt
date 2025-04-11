@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ejemplotoolbar


import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ejemplotoolbar.ui.theme.EjemploToolbarTheme
import java.time.LocalDateTime
import kotlin.Int
import com.example.ejemplotoolbar.data.*
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable

data class Jugador(
    val title: String,
    var monedas: Int,
    var rachas: Int,
    var fecha: LocalDateTime? = null
)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var nombre = IniciarDatos(this)
        setContent {
          EjemploToolbarTheme {
              MorraVirtualApp(nombre)
            }
        }
    }
}

@SuppressLint("CheckResult")
@RequiresApi(Build.VERSION_CODES.O)
fun IniciarDatos(context: Context): Jugador{
    val dbHelper = DataPartida(context)
    var db = dbHelper.writableDatabase
    return  UltimaFila(db)
}

@Composable
fun MorraVirtualApp (jugador: Jugador) {
    var currentStep by remember { mutableIntStateOf(1) }
    var ganador by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        when (currentStep) {
            1 -> {
                MorraScreen(
                    imageResourceId = R.drawable.lamorravirtual,
                    contentDescriptionId = R.string.welcome,
                    onImageClick = {currentStep = 2},

                    )


            }
            2 -> {
                //Thread.sleep(3000)
                InterfaceUsuario(jugador,
                    imageResourceId = R.drawable.lamorratheme,
                    contentDescriptionId = R.string.welcome,
                    onImageClick = {currentStep = 3}
                )
            }
            3 -> {
                InterfaceJuego(jugador,
                    imageResourceId = R.drawable.lamorratheme,
                    contentDescriptionId = R.string.welcome,
                    onImageClick = {currentStep = 4},
                    ganador = ganador,
                    onWinnerChange = {ganador = it}
                )

            }
            4 -> {
                PantallaFinal(jugador,
                    imageResourceId = R.drawable.lamorratheme,
                    contentDescriptionId = R.string.welcome,
                    onImageClick = {currentStep = 2},
                    ganador = ganador
                )
            }
        }
    }
}

@Composable
fun MorraScreen(
    imageResourceId: Int,
    contentDescriptionId: Int,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier

){
    Box(modifier){
        Image(
            painter = painterResource(imageResourceId),
            contentDescription = stringResource(contentDescriptionId),
            contentScale = ContentScale.Crop

        )
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ){

            Spacer(modifier = Modifier.height(550.dp))
            Button( onClick = onImageClick,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(40.dp)
                //.padding(30.dp)
            ) {
                Text(
                    stringResource(R.string.clic_welcome)
                )
            }

        }
    }
}

@Composable
fun InterfaceUsuario(jugador: Jugador,
    imageResourceId: Int,
    contentDescriptionId: Int,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier){
    Scaffold(
        topBar = { Toolbar(jugador)},
        content = { innerPadding ->

            Box(modifier){
                Image(
                    painter = painterResource(imageResourceId),
                    contentDescription = stringResource(contentDescriptionId),
                    contentScale = ContentScale.Crop
                )
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ){
                    Spacer(modifier = Modifier.height(200.dp))
                    Button(
                        onClick = onImageClick,
                    ) {
                        Text(
                            text = stringResource(R.string.iniciar_juego),
                            fontSize = (24.sp),
                            fontWeight = FontWeight.Bold

                        )
                    }

                }
            }
        }
    )
}
@Composable
fun InterfaceJuego(jugador: Jugador,
    imageResourceId: Int,
    contentDescriptionId: Int,
    onImageClick: () -> Unit,
                   ganador: String,
                   onWinnerChange: (String) -> Unit,
    modifier: Modifier = Modifier){
    Scaffold(
        topBar = { Toolbar(jugador)},

        content = { innerPadding ->



            Box(modifier) {
                Image(
                    painter = painterResource(imageResourceId),
                    contentDescription = stringResource(contentDescriptionId),
                    contentScale = ContentScale.Crop
                )
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {

                    // inicio partida, puntos a 0
                    var puntos1 by remember{mutableIntStateOf(value = 0)}
                    var puntos2 by remember{mutableIntStateOf(value = 0) }
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    )
                    {
                        val nombre = jugador.title
                        Text(text= "Contrincantes: ",
                            fontSize = (24.sp),
                            fontWeight = FontWeight.Bold)
                        Column {

                            Text(nombre,
                                fontSize = (24.sp),
                                fontWeight = FontWeight.Bold)
                            Text("$puntos1 Puntos",
                                fontSize = (24.sp),
                                fontWeight = FontWeight.Bold)

                        }
                        Column {

                            Text("Maquina",
                                fontSize = (24.sp),
                                fontWeight = FontWeight.Bold)
                            Text("${puntos2 }Puntos",
                                fontSize = (24.sp),
                                fontWeight = FontWeight.Bold)

                        }
                    }

                    val context = LocalContext.current
                    val dbHelper = DataPartida(context)
                    var db = dbHelper.writableDatabase
                    var mano1 by remember{mutableStateOf(value ="")}
                    var mano0 by remember{mutableIntStateOf(value = 0)}
                    var mano2 by remember{mutableIntStateOf(value = 0) }
                    var total1 by remember { mutableStateOf(value = "") }
                    var total2 by remember{mutableIntStateOf(value = 0)}
                    var suma1 by remember { mutableIntStateOf(value = 0) }
                    var maxtotal by remember  { mutableIntStateOf(value = 0) }
                    var resta1 by remember  { mutableIntStateOf(value = 0) }
                    var resta2 by remember  { mutableIntStateOf(value = 0) }
                    var gana by remember {mutableStateOf(value = "")}
                    val disposable = CompositeDisposable()
                    Column(
                        modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EntradaDeDatos(title = "Tu Mano: (del 0 al 5) ", text = mano1, onValueChange = {mano1 = it} )
                        EntradaDeDatos(title = "Tu Apuesta: (del 0 al 10)", text = total1, onValueChange = {total1 = it} )
                        Spacer(modifier = Modifier.height(50.dp))
                        Button(

                            onClick = {
                                mano0 = mano1.toInt()
                                // comprueba si la apuesta es valida
                                if( 0 <= mano0 && mano0 <= 5 && mano0 <= total1.toInt() && total1.toInt() <= mano0 + 5) {
                                    mano2 = (0..5).random()
                                    maxtotal = mano2 + 5
                                    total2 = (mano2..maxtotal).random()
                                    suma1 = mano0 + mano2
                                    resta1 = positivoConv(suma1 - total1.toInt())
                                    resta2 = positivoConv(suma1 - total2)
                                    // revisa la solución de la apuesta
                                    if (total1.toInt() != total2) {
                                        if (total1.toInt() == suma1) {
                                            puntos1 += 5
                                            gana = "jugador"
                                            jugador.monedas += 5
                                        } else if (total2 == suma1) {
                                            puntos2 += 5
                                            gana = "maquina"
                                            jugador.monedas -= 5
                                        } else if (resta1 < resta2) {
                                            puntos1 += 1
                                            gana = "jugador"
                                            jugador.monedas += 1
                                        } else {
                                            puntos2 += 1
                                            gana = "maquina"
                                            jugador.monedas -= 1
                                        }

                                    }else{
                                        Toast.makeText(context, "Tablas, no hay vencedor", Toast.LENGTH_SHORT).show()
                                    }
                                    if (puntos1 >= 5) {
                                        // racha + 1
                                        jugador.rachas += 1
                                        Toast.makeText(context, "fin de la partida ganador jugador", Toast.LENGTH_SHORT).show()
                                        disposable.add( guardarPartida(db ,jugador.title , jugador.monedas, jugador.rachas)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(
                                                { rowId -> Log.d("DB", "Insertado con ID: $rowId") },
                                                { error -> Log.e("DB", "error", error)}
                                            )
                                        )

                                        // guardo la partida
                                        onWinnerChange.invoke("Jugador")
                                        onImageClick.invoke()
                                    } else if (puntos2 >= 5) {
                                        // se acabo la racha
                                        jugador.rachas = 0
                                        Toast.makeText(context, "fin de la partida ganadora la maquina", Toast.LENGTH_SHORT).show()
                                        // guardo la partida
                                        disposable.add( guardarPartida(db ,jugador.title , jugador.monedas, jugador.rachas)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(
                                                { rowId -> Log.d("DB", "Insertado con ID: $rowId") },
                                                { error -> Log.e("DB", "error", error)}
                                            )
                                        )
                                        onWinnerChange("Maquina")
                                        onImageClick.invoke()
                                    }
                                }else{

                                    Toast.makeText(context, "Error en la apuesta, recuerda tu mano tiene de 0 a 5 dedos y tu apuesta el numero de tu apuesta hasta + 5", Toast.LENGTH_SHORT).show()
                                }
                            },
                        ) {

                            Text(
                                text = stringResource(R.string.nueva_apuesta),
                                fontSize = (24.sp),
                                fontWeight = FontWeight.Bold

                            )
                        }
                        Text(text = "Apuesta del oponente $total2",
                            fontSize = (24.sp),
                            fontWeight = FontWeight.Bold)
                        Row (modifier = modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceEvenly)
                        {
                            ImagenMano(mano0)
                            ImagenMano(mano2)
                        }
                        Button(
                            onClick = onImageClick,
                            modifier = Modifier.height(70.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.salir_partida),
                                fontSize = (24.sp),
                                fontWeight = FontWeight.Bold

                            )
                        }

                    }
                }
            }
        }
    )
}

/*fun guardaPartidaDB(nombre: String, monedas: Int, rachas: Int){
    context
    val idPartida = guardarPartida(context = Context, dataPartida, nombre, monedas, rachas)
    if (idPartida != null) {
        if (idPartida > 0) {
            println("Partida guardada con ID: $idPartida")
        } else {
            println("Error al guardar la partida")
        }
    }
}
*/

@Composable
fun ImagenMano(a: Int){
    when(a){
        0 -> {ZeroImagen()}
        1 -> {UnoImagen()}
        2 -> {DosImagen()}
        3 -> {TresImagen()}
        4 -> {CuatroImagen()}
        5 -> {CincoImagen()}
    }
}

@Composable
fun ZeroImagen(){
    Image(
        painter = painterResource(id = R.drawable.puno),
        contentDescription = "puño cerrado 0",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun UnoImagen(){
    Image(
        painter = painterResource(id = R.drawable.uno),
        contentDescription = "un dedo 1",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun DosImagen(){
    Image(
        painter = painterResource(id = R.drawable.dos),
        contentDescription = "dos dedos 2",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun TresImagen(){
    Image(
        painter = painterResource(id = R.drawable.tres),
        contentDescription = "tres dedos 3",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun CuatroImagen(){
    Image(
        painter = painterResource(id = R.drawable.cuatro),
        contentDescription = "cuatro dedos 4",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun CincoImagen(){
    Image(
        painter = painterResource(id = R.drawable.cinco),
        contentDescription = "cinco dedos 5",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun PantallaFinal(jugador: Jugador,
                  ganador: String,
    imageResourceId: Int,
    contentDescriptionId: Int,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier) {

    Scaffold(
        topBar = { Toolbar(jugador)},
        content = { innerPadding ->

            Box(modifier) {
                Image(
                    painter = painterResource(imageResourceId),
                    contentDescription = stringResource(contentDescriptionId),
                    contentScale = ContentScale.Crop
                )
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Text(
                        text = stringResource(R.string.ganador,ganador),
                        style = MaterialTheme.typography.displaySmall
                    )
                    Spacer(modifier = Modifier.height(200.dp))
                    Button(
                        onClick = onImageClick,
                    ) {
                        Text(
                            text = stringResource(R.string.final_oartida),
                            fontSize = (24.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun Toolbar(jugador: Jugador){
    TopAppBar(
        title = { Text("La Morra Virtual") },
        navigationIcon = {
            IconButton(onClick = { /* Acción de navegación */ }) {
                Icon(Icons.Default.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            Monedasyrachas(jugador)
            IconButton(onClick = { /* Acción de más opciones */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
            }
        },
        modifier = Modifier.fillMaxWidth(),
        //elevation = 4.dp
    )
}


@Composable
fun Monedasyrachas(jugador: Jugador){
    IconButton(onClick = { /* Acción de búsqueda */ }) {
        //Icon(Icons.Default.Search , contentDescription = "Buscar")
        Image(
            painter = painterResource(R.drawable.racha),
            contentDescription = "Racha"
        )
    }
    Text(text = "${jugador.rachas}")
    IconButton(onClick = { /* Acción de búsqueda */ }) {
        //Icon(Icons.Default.Search , contentDescription = "Buscar")
        Image(
            painter = painterResource(R.drawable.money),
            contentDescription = "Monedas"
        )
    }
    Text(text = "${jugador.monedas}")
}

@Composable
fun JuegoPartida(){
    Box(Modifier
        .fillMaxSize()
        .padding(16.dp)){
        Apostar()

    }
}

@Composable
fun Apostar(){
    Row {
        Text("prueba1")

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val nombre = Jugador("javier", 30, 4)
    EjemploToolbarTheme {
        MorraVirtualApp(nombre)
    }
}

@Composable
fun EntradaDeDatos (
    title: String,
    text: String,
    onValueChange: (String) -> Unit
){
    TextField(
        value = text,
        onValueChange = onValueChange,
        label = {Text(text = title)},
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

fun positivoConv (a: Int): Int{
    if (a < 0 ){
        return a * (-1)
    }else
        return a
}