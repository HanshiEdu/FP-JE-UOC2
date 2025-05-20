@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ejemplotoolbar


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Canvas
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.location.Location
import android.media.AudioAttributes
import android.media.MediaPlayer
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import androidx.core.net.toUri
import com.example.ejemplotoolbar.data.DataPartida
import com.example.ejemplotoolbar.data.UltimaFila
import com.example.ejemplotoolbar.data.guardarPartida
import com.example.ejemplotoolbar.ui.theme.EjemploToolbarTheme
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.IOException
import java.time.LocalDateTime
import android.media.SoundPool

import android.os.Environment
import android.provider.CalendarContract
import android.provider.MediaStore
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import androidx.compose.runtime.DisposableEffect
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth


data class Jugador(
    var title: String,
    var monedas: Int,
    var rachas: Int,
    var fecha: LocalDateTime? = null,
    var latitud: Double,
    var longitud: Double
)
// ToolbarActions para el desplegable de la barra de acción
data class ToolbarActions(
    val onHelp: () -> Unit = {},
    val onHome: () -> Unit = {}
)
// variable global para controlar la barra de acción
val LocalToolbarActions = compositionLocalOf { ToolbarActions() }

// AyudaController para el manejo de la pagina de ayuda o webview
class AyudaController {
    var mostrarAyuda by mutableStateOf(false)
        private set

    fun mostrar() {
        mostrarAyuda = true
    }

    fun ocultar() {
        mostrarAyuda = false
    }
}
// variable global para controlar la pagina de ayuda
val LocalAyudaController = compositionLocalOf { AyudaController() }

// CalendarioDisponible para almacenar la información de un calendario, no será de utilidad para hacer una lista de calendarios.
data class CalendarioDisponible(
    val id: Long,
    val nombre: String,
    val cuenta: String
)

// main, inicio de la activity ponemos en marcha la musica, las variables necesarias y la llamada al controlador de pantallas MorraVirtualApp
class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)?.let {
            Log.d("FirebaseInit", "FirebaseApp inicializado correctamente.")
        } ?: Log.e("FirebaseInit", "Error: FirebaseApp es null")
        super.onCreate(savedInstanceState)
        var nombre = IniciarDatos(this)
        setContent {
          EjemploToolbarTheme {
              MorraVirtualApp(nombre)
            }
        }
        // Inicializa el MediaPlayer
        mediaPlayer = MediaPlayer()

        // Ruta del archivo de música
        val musicPath = "android.resource://" + packageName + "/" + R.raw.jazz_in_paris


        try {
            mediaPlayer.setDataSource(this, musicPath.toUri())
            mediaPlayer.prepare()


        } catch (e: IOException) {
            e.printStackTrace()
        }
        mediaPlayer.start()

    }
    override fun onPause() {
        super.onPause()
        mediaPlayer.stop()
        mediaPlayer.release()

    }
   override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()

    }

}
// es una función de prueba para iniciar los datos de usuario
@SuppressLint("CheckResult")
@RequiresApi(Build.VERSION_CODES.O)

fun IniciarDatos(context: Context): Jugador{
    val dbHelper = DataPartida(context)
    var db = dbHelper.writableDatabase
    return  UltimaFila(db)
}

// controlador de pantallas
@Composable
fun MorraVirtualApp (jugador: Jugador) {
    var currentStep by remember { mutableIntStateOf(1) }
    var ganador by remember { mutableStateOf("") }
    val soundPool = rememberSoundPool()
    val soundPool2 = rememberSoundPool()

    val context = LocalContext.current
    val soundId = remember { soundPool.load(context, R.raw.button, 1) }
    val soundId2 = remember { soundPool2.load(context, R.raw.winner, 1) }

    val loaded = remember { mutableStateOf(false) }
    ConAyudaOverlay {
        Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
        ) {

            when (currentStep) {

                1 -> {

                    MorraScreen(
                        imageResourceId = R.drawable.lamorravirtual,
                        contentDescriptionId = R.string.welcome,
                        onImageClick = { jugadorFromLogin ->
                            jugador.title = jugadorFromLogin.title
                            jugador.monedas = jugadorFromLogin.monedas
                            currentStep = 2

                        },
                    )
                }


                2 -> {
                    //Thread.sleep(3000)

                    InterfaceUsuario(
                        jugador,
                        imageResourceId = R.drawable.lamorratheme,
                        contentDescriptionId = R.string.welcome,
                        onImageClick = {
                            currentStep = 3
                            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                        }
                    )
                }

                3 -> {
                    InterfaceJuego(
                        jugador,
                        imageResourceId = R.drawable.lamorratheme,
                        contentDescriptionId = R.string.welcome,
                        onImageClick = {
                            currentStep = 4
                            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                        },
                        ganador = ganador,
                        onWinnerChange = {
                            ganador = it
                            soundPool2.play(soundId2, 1f, 1f, 0, 0, 1f)
                        }
                    )

                }

                4 -> {
                    PantallaFinal(
                        jugador,
                        imageResourceId = R.drawable.lamorratheme,
                        contentDescriptionId = R.string.welcome,
                        onImageClick = {
                            currentStep = 2
                            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                        },
                        ganador = ganador
                    )
                }
            }
        }
    }

}

// pantalla de inicio, solo aparece 1 vez
@Composable
fun MorraScreen(
    imageResourceId: Int,
    contentDescriptionId: Int,
    onImageClick: (Jugador) -> Unit,
    modifier: Modifier = Modifier

){
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Firebase.auth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        currentUser = Firebase.auth.currentUser
                    }
                }
        } catch (e: ApiException) {
            Log.e("Login", "Error al iniciar sesión con Google", e)
        }
    }

    val token = stringResource(R.string.default_web_client_id)
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

            Spacer(modifier = Modifier.height(540.dp))
            if (currentUser == null) {
                Button(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(token)
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Iniciar sesión con Google")
                }
            }else {
                // Usuario autenticado, crear objeto Jugador y continuar
                val jugador = Jugador(
                    title = currentUser?.displayName ?: "Jugador",
                    monedas = 20,
                    rachas = 0,
                    fecha = null,
                    latitud = 0.0,
                    longitud = 0.0
                )
                LaunchedEffect(Unit) {
                    onImageClick(jugador)
                }
            }


        }
    }
}
// el resto de pantallas tienen en comunun topBar

@Composable
fun Toolbar(jugador: Jugador){

    var expanded by remember { mutableStateOf(false) }
    val actions = LocalToolbarActions.current

    TopAppBar(
        title = { Text("La Morra Virtual") },
        navigationIcon = {
            IconButton(onClick = { /* Acción de navegación */ }) {
                Icon(Icons.Default.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            Monedasyrachas(jugador)
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Ayuda") },
                    onClick = {
                        expanded = false
                        actions.onHelp()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Inicio") },
                    onClick = {
                        expanded = false
                        actions.onHome()
                    }
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        //elevation = 4.dp
    )
}


// pantalla inicial donde podemos iniciar una partida
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
// en esta pantalla se lleva a cabo la partida
@Composable
fun InterfaceJuego(jugador: Jugador,
                    imageResourceId: Int,
                    contentDescriptionId: Int,
                    onImageClick: () -> Unit,
                   ganador: String,
                   onWinnerChange: (String) -> Unit,
                   modifier: Modifier = Modifier,
                   onCapturaLista: (Bitmap) -> Unit ={}
){
    val context = LocalContext.current
    val dbHelper = DataPartida(context)
    var db = dbHelper.writableDatabase
    val activity = LocalActivity.current

    val captureController = remember { CaptureController() }
    var mostrarPantallaFinal by remember { mutableStateOf(false) }
    var capturaPendiente by remember { mutableStateOf(false) }
    var mostrarElegirCalendario by remember { mutableStateOf(false) }
    var calendarIdSeleccionado by remember { mutableStateOf<Long?>(null) }
    var mostrarDialogoGuardarImagen by remember { mutableStateOf(false) }
    var bitmapPendienteGuardar by remember { mutableStateOf<Bitmap?>(null) }
    // Activa el estado de captura antes de mostrar la pantalla final
    // Componente de captura

    LaunchedEffect(Unit) {
        crearCanalDeNotificacion(context)
    }
    CaptureComposable(
        captureController = captureController,
        onCaptured = { bitmap ->
            bitmapPendienteGuardar = bitmap
            mostrarDialogoGuardarImagen = true
        }
    ) {
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
                        if (mostrarElegirCalendario) {
                            ElegirCalendario (
                                onDismiss = { mostrarElegirCalendario = false },
                                onCalendarioSeleccionado = { calendarioId ->
                                    agregarVictoriaCalendario(context, calendarioId)
                                    mostrarElegirCalendario = false
                                    mostrarPantallaFinal = true
                                    capturaPendiente = true
                                }
                            )
                        }
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
                        val soundPool = rememberSoundPool()

                        val soundId = remember { soundPool.load(context, R.raw.button, 1) }

                        val loaded = remember { mutableStateOf(false) }
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
                                    soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
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

                                            if(activity != null) {
                                                captureController.capture { bitmap ->
                                                    bitmapPendienteGuardar = bitmap
                                                    mostrarDialogoGuardarImagen = true
                                                }
                                                solicitarPermisosCalendario(activity)
                                                mostrarElegirCalendario = true
                                            }
                                            obtenerUbicacion(context) { location ->
                                                location?.let {
                                                    // Guardamos latitud y longitud en la base de datos
                                                    jugador.latitud = location.latitude
                                                    jugador.longitud = location.longitude
                                                }
                                                guardaPartidaDB(db ,jugador.title , jugador.monedas, jugador.rachas, jugador.latitud, jugador.longitud)
                                                Toast.makeText(context, "fin de la partida, ganador jugador Nombre=$jugador.title, Pasta=${jugador.monedas}, Racha=${jugador.rachas}, Posicionamiento={ ${jugador.latitud}, ${jugador.longitud}}", Toast.LENGTH_SHORT).show()
                                                Log.d("victoriadebug","fin de la partida ganador jugador Nombre=$jugador.title, Pasta=$jugador.monedas, Racha=$jugador.rachas, Posicionamiento= $jugador.latitud, $jugador.longitud")

                                            }
                                            emitirNotificacionVictoria(context)
                                        } else if (puntos2 >= 5) {
                                            // se acabo la racha
                                            jugador.rachas = 0
                                            Toast.makeText(context, "fin de la partida ganadora la maquina", Toast.LENGTH_SHORT).show()
                                            // guardo la partida
                                            guardaPartidaDB(db ,jugador.title , jugador.monedas, jugador.rachas)
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

    if (mostrarDialogoGuardarImagen && bitmapPendienteGuardar != null) {

        Log.d("debugguardarimagen","llamo a la funcion dialogoguardarimagen 1")
        DialogoGuardarImagen(
            context,
            bitmap = bitmapPendienteGuardar!!,
            onGuardar = {
                mostrarDialogoGuardarImagen = false
                bitmapPendienteGuardar = null
                guardarImagenEnGaleria(context, it)
            },
            onCancelar = {

                Log.d("debugguardarimagen","llamo a la funcion dialogoguardarimagen 3")
                mostrarDialogoGuardarImagen = false
                bitmapPendienteGuardar = null
            }
        )
    }
    // Llamamos a capture después de que se renderiza la UI
    LaunchedEffect(capturaPendiente) {
        if (capturaPendiente) {
            delay(300) // Espera un frame para la renderización
            // Realiza la captura
            captureController.capture { bitmap ->
                bitmapPendienteGuardar = bitmap
                mostrarDialogoGuardarImagen = true
            }
            // Llamada final a la UI
            onWinnerChange.invoke("Jugador")
            onImageClick.invoke()
        }
    }


}

// pantalla final de partida
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



// Funciones para mostrar una imagen determinada -------------------------------------------------------------------

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

// función para mostrar el webview ----------------------------------------------------------------------------------
@Composable
fun HelpSection(url: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient() // Evita abrir navegador externo
            settings.javaScriptEnabled = true
            loadUrl(url)
        }
    })
}

// funcion para mostrar en el toolbar la información del jugador ------------------------------------------------------
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


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val nombre = Jugador("javier", 30, 4, null, 0.0, 0.0)
    EjemploToolbarTheme {
        MorraVirtualApp(nombre)
    }
}

// recoger datos de apuesta -----------------------------------------------------------------------------------------------------------
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

// musica ---------------------------------------------------------------------------------------------------------------------------
fun positivoConv (a: Int): Int{
    if (a < 0 ){
        return a * (-1)
    }else
        return a
}
@Composable
fun rememberSoundPool(): SoundPool {
    val context = LocalContext.current
    return remember {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
    }
}


// geolocalización -------------------------------------------------------------------------------------------------------
fun obtenerUbicacion(context: Context, onUbicacionObtenida: (Location?) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d("debugubicacion", "Ubicación obtenida: Lat=${location.latitude}, Lng=${location.longitude}")
                } else {
                    Log.d("debugubicacion", "Ubicación es null")
                }
                onUbicacionObtenida(location)
            }
    } else {
        onUbicacionObtenida(null)
    }
}

// guardar iamgen -----------------------------------------------------------------------------------------------------

// captura de imagen
class CaptureController {
    var bitmap: Bitmap? by mutableStateOf(null)
    var requestCapture by mutableStateOf(false)

    private var onCapturedCallback: ((Bitmap) -> Unit)? = null

    fun capture(onCaptured: (Bitmap) -> Unit) {

        onCapturedCallback = onCaptured
        requestCapture = true
    }

    fun onCaptured(bitmap: Bitmap) {
        this.bitmap = bitmap
        onCapturedCallback?.invoke(bitmap)
        onCapturedCallback = null // evitar múltiples llamadas
    }
}

@Composable
fun CaptureComposable(
    captureController: CaptureController,
    onCaptured: (Bitmap) -> Unit,
    content: @Composable () -> Unit
) {
    Log.d("CaptureDebug", "entro en captureComposable")

    AndroidView(
        factory = { context ->
            ComposeView(context).apply {
                setContent {
                    content()
                }
            }

        },
        update = { view ->
            if (captureController.requestCapture) {
                view.post {
                    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    view.draw(canvas)
                    Log.d("CaptureDebug", "Imagen capturada, tamaño: ${bitmap.width}x${bitmap.height}")
                    captureController.bitmap = bitmap
                    captureController.requestCapture = false
                    onCaptured(bitmap)
                }
            }else{
                Log.d("CaptureDebug", "entro en el else")
            }
        },
        modifier = Modifier.fillMaxSize()
    )

}


// cuadro dialogo para guardar la imagen o cancelar
@Composable
fun DialogoGuardarImagen(
    context: Context,
    bitmap: Bitmap,
    onGuardar: (Bitmap) -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onCancelar() },
        title = { Text("Guardar imagen") },
        text = { Text("¿Quieres guardar la imagen de la victoria en la galería?") },
        confirmButton = {
            TextButton(onClick = { onGuardar(bitmap) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancelar() }) {
                Text("Cancelar")
            }
        }
    )
}


// guardar la imagen en la galeria dek juego
fun guardarImagenEnGaleria(context: Context, bitmap: Bitmap) {
    val filename = "victoria_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Morra")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        resolver.openOutputStream(it)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
    }

}


// para encapsular la aplicación y mostrar la ayuda ----------------------------------------------------------------------------


@Composable
fun ConAyudaOverlay(content: @Composable () -> Unit) {
    val ayudaController = remember { AyudaController() }

    CompositionLocalProvider(
        LocalAyudaController provides ayudaController,
        LocalToolbarActions provides ToolbarActions(
            onHelp = { ayudaController.mostrar() },
            onHome = { /* acción de inicio si quieres */ }
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            if (ayudaController.mostrarAyuda) {
                BackHandler(enabled = true) {
                    ayudaController.ocultar()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { ayudaController.ocultar() }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar ayuda", tint = Color.White)
                            }
                        }

                        HelpSection("file:///android_asset/ayuda.html")
                    }
                }
            }
        }
    }
}

// para guardar una partida ganada en un calendario --------------------------------------------------------------------------

// obtener calendarios .uoc.edu
fun obtenerCalendarios(context: Context): List<CalendarioDisponible> {
    val calendarios = mutableListOf<CalendarioDisponible>()

    val projection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.ACCOUNT_NAME
    )

    val cursor = context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        null,
        null,
        null
    )

    cursor?.use {
        val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
        val nameIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
        val accountIndex = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)

        while (it.moveToNext()) {
            val id = it.getLong(idIndex)
            val nombre = it.getString(nameIndex)
            val cuenta = it.getString(accountIndex)

            Log.d("CalendarioDebug", "Calendario encontrado: Nombre=$nombre, Cuenta=$cuenta, ID=$id")

            calendarios.add(CalendarioDisponible(id, nombre, cuenta))
        }
    }

    return calendarios
}

//elegir calendario
@Composable
fun ElegirCalendario(
    onDismiss: () -> Unit,
    onCalendarioSeleccionado: (Long) -> Unit
) {
    val context = LocalContext.current
    val calendarios = remember { obtenerCalendarios(context).filter { calendario ->
        calendario.cuenta.contains("@uoc.edu")
    } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("Selecciona un calendario")},
        text= {
            Column {
                calendarios.forEach { calendario ->
                    Button(
                        onClick = {
                            onCalendarioSeleccionado(calendario.id)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(calendario.nombre)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
// agregar la victoria en el calendario elegido
fun agregarVictoriaCalendario(context: Context, calendarId: Long,titulo: String = "¡Victoria en el juego LA MORRA!", descripcion: String = "Ganaste una partida") {
    // val calendarId = obtenerPrimerCalendarioId(context) ?: return

    Log.d("CaptureDebug", "Estas en la funcion agregarvictoriacalendario")
    val inicio = Calendar.getInstance()
    val fin = Calendar.getInstance().apply{add(Calendar.HOUR, 1)}
    //fin.add(Calendar.HOUR, 1) // duración de 1 hora

    val values = ContentValues().apply {
        put(CalendarContract.Events.DTSTART, inicio.timeInMillis)
        put(CalendarContract.Events.DTEND, fin.timeInMillis)
        put(CalendarContract.Events.TITLE, titulo)
        put(CalendarContract.Events.DESCRIPTION, descripcion)
        put(CalendarContract.Events.CALENDAR_ID, calendarId)
        put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
    }

    try {
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        if (uri != null) {
            Log.d("CALENDARIO", "Evento cuardado correctamente: $uri")
            Log.d("CaptureDebug", "Insertando evento en calendario ID: $calendarId")
        }else{
            Log.d("CALENDARIO", "Error al guardar el evento")
        }
    }catch ( e: SecurityException){
        Log.e("CaptureDebug", "Error de permisos al guardar evento: ${e.message}")
    }catch (e: Exception) {
        Log.e("CaptureDebug", "Error desconocido al guardar evento: ${e.message}")
    }
}

// guardar la partida en la base de datos ---------------------------------------------------------------------------------------------------------
fun guardaPartidaDB(dbpartida: SQLiteDatabase, nombre: String, monedas: Int, rachas: Int, latitud: Double = 0.0, longitud: Double = 0.0){

    val disposable = CompositeDisposable()
    disposable.add( guardarPartida(dbpartida ,nombre , monedas, rachas, latitud, longitud)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { rowId -> Log.d("DB", "Insertado con ID: $rowId") },
            { error -> Log.e("DB", "error", error)}
        )
    )
}

// permisos de la aplicación, aprovechando la función para el calendario tambien pediremos la de localizacion -----------------------------------------------
/*fun solicitarPermisosCalendario(activity: Activity) {
    val permisosFaltantes = mutableListOf<String>()

    if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
        permisosFaltantes.add(android.Manifest.permission.WRITE_CALENDAR)
        permisosFaltantes.add(Manifest.permission.READ_CALENDAR)
    }

    if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        permisosFaltantes.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permisosFaltantes.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            1001
        )
    }

    if (permisosFaltantes.isNotEmpty()){
        ActivityCompat.requestPermissions(
            activity,
            permisosFaltantes.toTypedArray(),
            100 // código de solicitud
        )
    }
}
*/
fun solicitarPermisosCalendario(activity: Activity) {
    val permisosFaltantes = mutableSetOf<String>() // Set para evitar duplicados

    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
        permisosFaltantes.add(Manifest.permission.WRITE_CALENDAR)
    }
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
        permisosFaltantes.add(Manifest.permission.READ_CALENDAR)
    }
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        permisosFaltantes.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        permisosFaltantes.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        permisosFaltantes.add(Manifest.permission.POST_NOTIFICATIONS)

    }

    if (permisosFaltantes.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            activity,
            permisosFaltantes.toTypedArray(),
            100 // Código de solicitud general
        )
    }
}
// Notificaciones ---------------------------------------------------------------------------------------------------------------------------------------

// abrir canal
fun crearCanalDeNotificacion(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val canal = NotificationChannel(
            "victoria_id", // ID del canal
            "Victorias",   // Nombre visible
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificaciones de victoria"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(canal)
    }
}

// emitir notificacion

fun emitirNotificacionVictoria(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        // No tienes permiso para notificaciones. Puedes omitir o pedirlo en otra parte.
        Log.w("Notificacion", "Permiso de notificaciones no concedido.")
        return
    }

    val builder = NotificationCompat.Builder(context, "victoria_id")
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Usa tu ícono
        .setContentTitle("¡Victoria!")
        .setContentText("Has ganado una partida. ¡Felicidades" +
                "" +
                "!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    val manager = NotificationManagerCompat.from(context)
    manager.notify(1, builder.build())
}

