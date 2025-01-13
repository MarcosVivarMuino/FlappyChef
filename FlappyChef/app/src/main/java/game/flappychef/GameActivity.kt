package game.flappychef

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.util.Calendar
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.imageResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.sqrt

class GameActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var shakeThreshold = 15f // Sensibilidad del agite
    private var lastShakeTime = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el sensor de movimiento
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            GameScreen(
                onGameOver = {
                    finishAffinity()
                    finish()
                    val intent = Intent(this, GameOverActivity::class.java)
                    startActivity(intent)
                },
                onShakeDetected = {
                    // Aquí manejas el evento de agite para activar power-ups
                    // o pasarlo a la lógica de `GameScreen`
                }
            )
        }
    }




    @Composable
    fun GameScreen(onGameOver: () -> Unit, onShakeDetected: () -> Unit) {
        // Obtén el contexto usando LocalContext
        val context = LocalContext.current
        // Usa `remember` para crear y almacenar una instancia de SoundManager
        val soundManager = remember { SoundManager(context) }

        val backgroundMusic = remember { BackgroundMusic(context) }

        // Inicia la música de fondo cuando el personaje esté en fuego
        LaunchedEffect(Unit) {
            backgroundMusic.start()
        }

        DisposableEffect(Unit) {
            onDispose {
                backgroundMusic.stop()
                backgroundMusic.release()
            }
        }

        var playerY by remember { mutableStateOf(500f) }
        var playerX by remember { mutableStateOf(300f) }
        var velocity by remember { mutableStateOf(0f) }

        val gravity = 1f
        val jumpForce = -15f
        val enemy = remember {
            Enemy(
                x = 780f,
                y = Random.nextInt(100, 1820).toFloat()
            )
        } // Enemigo en el borde derecho
        val balls = remember { mutableStateListOf<Ball>() }
        var playerColor by remember { mutableStateOf(Color.Red) } // Color inicial del jugador (rojo)
        val horizontalEnemies =
            remember { mutableStateListOf<HorizontalEnemy>() } // Lista de enemigos horizontales
        var isGameOver by remember { mutableStateOf(false) }

        var showBurnImage by remember { mutableStateOf(false) }  // Para controlar la visibilidad de la imagen
        var burnStartTime by remember { mutableStateOf(0L) }       // Para guardar el tiempo de inicio del contador

        val powerUps = remember { mutableStateListOf<PowerUp>() }
        var isImmune by remember { mutableStateOf(false) }
        var isSlowEnemies by remember { mutableStateOf(false) }

        val configuration = LocalConfiguration.current
        val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
        val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }

        // Get the current hour from the system clock
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val backgroundRes = if (currentHour in 8..17) {
            R.drawable.fondo_dia // Fondo de día
        } else {
            R.drawable.fondo_noche // Fondo de noche
        }

        // Variables del micrófono
        val micThreshold = 12000
        var micAmplitude by remember { mutableStateOf(0) }
        var isRecording by remember { mutableStateOf(false) }
        var isBurning by remember { mutableStateOf(false) }



        // Configuración del micrófono
        val audioRecord = remember {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioRecord.getMinBufferSize(
                    44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
            )
        }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                audioRecord.startRecording()
                isRecording = true

                while (isRecording) {
                    val buffer = ShortArray(1024)
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    micAmplitude = buffer.take(read).maxOrNull()?.toInt() ?: 0

                    // Activar "saltar" si se detecta un sonido fuerte
                    if (micAmplitude > micThreshold && isBurning) {
                        isBurning = false
                    }

                    delay(100L) // Leer el micrófono cada 100ms
                }
            }
        }

        //Crear bolas
        LaunchedEffect(Unit) {
            while (true) {
                soundManager.playBallSound()
                delay(5000L)
                balls.add(
                    Ball(
                        x = enemy.x,
                        y = enemy.y
                    )
                )

            }
        }


        // Mover las bolas de derecha a izquierda
        LaunchedEffect(balls) {
            while (true) {
                delay(10L) // Esperar 10ms antes de mover las bolas
                balls.forEach { ball ->
                    ball.x -= 10 // Mover bola hacia la izquierda
                }
                // Eliminar bolas que ya se salieron de la pantalla
                balls.removeAll { it.x < 0 }
            }
        }

        val towers =
            remember { mutableStateListOf<Pair<ImageBitmap, Offset>>() } // Lista de torres con su posición

        // Cargar los sprites de las torres
        val torreAltaImage = ImageBitmap.imageResource(id = R.drawable.torre_alta)
        val torreMediaImage = ImageBitmap.imageResource(id = R.drawable.torre_media)
        val torreBajaImage = ImageBitmap.imageResource(id = R.drawable.torre_baja)
        val towerSprites = listOf(torreAltaImage, torreMediaImage, torreBajaImage)

        // Generar torres aleatorias cada 3 segundos
        LaunchedEffect(Unit) {
            while (true) {
                delay(3000L) // Esperar 3 segundos
                val randomSprite = towerSprites.random() // Seleccionar un sprite aleatorio
                towers.add(
                    Pair(
                        randomSprite,
                        Offset(screenWidthPx, screenHeightPx - randomSprite.height.toFloat())
                    )
                )

            }
        }

        // Mover las torres de derecha a izquierda
        LaunchedEffect(towers) {
            while (true) {
                delay(10L) // Mover las torres cada 10ms
                towers.replaceAll { (sprite, position) ->
                    sprite to position.copy(x = position.x - 5) // Mover a la izquierda
                }
                towers.removeAll { (_, position) -> position.x < -100 } // Eliminar torres fuera de la pantalla
            }
        }

        // Mover los power-ups de derecha a izquierda
        LaunchedEffect(powerUps) {
            while (true) {
                delay(10L) // Esperar 10ms antes de mover los power-ups
                powerUps.forEach { powerUp ->
                    powerUp.x -= 3 // Mover power-up hacia la izquierda
                }
                // Eliminar power-ups que ya se salieron de la pantalla
                powerUps.removeAll { it.x < 0 }
            }
        }

        // Cambiar la altura del enemigo aleatoriamente
        LaunchedEffect(Unit) {
            while (true) {
                delay(3000L) // Cambiar altura cada 3 segundos
                enemy.y = Random.nextInt(100, 1820).toFloat()
            }
        }

        // Generar enemigos horizontales cada 5 segundos
        LaunchedEffect(Unit) {
            while (true) {
                soundManager.playForkSound()
                delay(3000L) // Crear un enemigo cada 5 segundos
                horizontalEnemies.add(
                    HorizontalEnemy(
                        x = screenWidthPx, // Inicia en el borde derecho de la pantalla
                        y = Random.nextInt(100, 1820).toFloat() // Posición aleatoria en el eje Y
                    )
                )
            }
        }

        // Actualizar posición de los enemigos horizontales
        LaunchedEffect(Unit) {
            while (true) {
                delay(15L) // Mover enemigos cada 10ms
                for (enemy in horizontalEnemies) {
                    enemy.x -= 15 // Velocidad de movimiento en -X
                }
                // Eliminar enemigos que salgan de la pantalla
                horizontalEnemies.removeAll { it.x < -150f }
            }
        }

        LaunchedEffect(towers) {
            while (!isGameOver) {
                delay(10L) // Chequear colisiones cada 10ms

                if (checkCollisions(playerX, playerY, 50f, 50f, towers, isImmune, soundManager)) {
                    if (!isGameOver) {
                        isGameOver = true
                        releaseResources(backgroundMusic, soundManager, isRecording, audioRecord)
                        onGameOver()
                    }
                    break
                }
            }
        }
        LaunchedEffect(Unit) {
            // Simula que `onShakeDetected` se invoca al detectar un agite
            snapshotFlow { isImmune || isSlowEnemies }.collect { activated ->
                if (activated) {
                    delay(10000L) // Duración de 10 segundos para los power-ups
                    isImmune = false
                    isSlowEnemies = false
                }
            }
        }

        fun handleShakeEvent() {
            if (powerUps.isNotEmpty()) {
                val powerUp = powerUps.removeFirst()
                when (powerUp.type) {
                    PowerUpType.IMMUNITY -> isImmune = true
                    PowerUpType.SLOW_ENEMIES -> isSlowEnemies = true
                }
            }
        }

        onShakeDetected() // Llama directamente al callback cuando sea necesario


        // Movimiento del jugador y detección de colisiones con limites
        LaunchedEffect(Unit) {
            while (!isGameOver) {
                delay(10L)
                playerY += velocity
                velocity += gravity

                // Si el jugador sale de los límites, activa Game Over
                if (playerY >= screenHeightPx - 50f || playerY <= 0f) {
                    if (!isGameOver) {
                        isGameOver = true
                        releaseResources(backgroundMusic, soundManager, isRecording, audioRecord)
                        onGameOver()

                    }
                    break
                }
            }
        }

        // Detectar colisiones entre el jugador y las bolas
        LaunchedEffect(balls) {
            while (true) {
                delay(10L) // Chequear cada 10ms
                // Usamos un for convencional para poder usar break
                for (ball in balls) {
                    if (checkCollisionBall(
                            playerX, playerY,
                            50f, 50f,
                            ball.x, ball.y, 30f
                        )
                    ) {
                        // Si hay colisión, mostrar la imagen de "te_quemas" y empezar el temporizador
                        showBurnImage = true
                        isBurning = true
                        burnStartTime =
                            System.currentTimeMillis()  // Guardar el momento de la colisión

                        // Detener el ciclo y continuar con la lógica
                        break  // Salir del ciclo una vez que detectamos la colisión
                    }
                }
            }
        }


        // Detección de colisiones con enemigos horizontales
        LaunchedEffect(horizontalEnemies) {
            while (!isGameOver) {
                delay(10L)
                for (enemy in horizontalEnemies) {
                    if (checkCollisionHorizontalEnemy(
                            playerX,
                            playerY,
                            25f, 25f,
                            enemy.x,
                            enemy.y,
                            enemy.width,
                            enemy.height
                        )
                    ) {
                        if (!isGameOver) {
                            isGameOver = true
                            releaseResources(backgroundMusic, soundManager, isRecording, audioRecord)
                            onGameOver()


                        }
                        return@LaunchedEffect
                    }
                }
            }
        }

        // Generar power-ups cada segundo
        LaunchedEffect(Unit) {
            while (!isGameOver) {
                delay(1000L)  // Crear un power-up cada segundo

                // Crear un nuevo power-up de tipo aleatorio
                val randomX = Random.nextInt(100, screenWidthPx.toInt() - 100).toFloat()
                val randomY = Random.nextInt(100, screenHeightPx.toInt() - 100).toFloat()
                val powerUpType =
                    if (Random.nextBoolean()) PowerUpType.IMMUNITY else PowerUpType.SLOW_ENEMIES

                // Agregar el power-up a la lista
                powerUps.add(PowerUp(x = randomX, y = randomY, type = powerUpType))

            }
        }

        // Detectar colisiones entre el jugador y los power-ups
        LaunchedEffect(powerUps) {
            while (!isGameOver) {
                delay(10L) // Chequear cada 10ms
                for (powerUp in powerUps) {
                    if (checkCollisionPU(playerX, playerY, 50f, 50f, powerUp.x, powerUp.y, 20f)) {
                        // Activar el efecto del power-up según el tipo
                        when (powerUp.type) {
                            PowerUpType.IMMUNITY -> {
                                soundManager.playPowerUp2Sound()
                                if (!isImmune) {
                                    isImmune = true
                                    // Desactivar la inmunidad después de 10 segundos
                                    launch {
                                        delay(10000L)  // Duración de la inmunidad
                                        isImmune = false
                                    }
                                }
                            }

                            PowerUpType.SLOW_ENEMIES -> {
                                soundManager.playPowerUp1Sound()
                                if (!isSlowEnemies) {
                                    isSlowEnemies = true
                                    // Desactivar la ralentización después de 10 segundos
                                    launch {
                                        delay(10000L)  // Duración de la ralentización
                                        isSlowEnemies = false
                                    }
                                }
                            }
                        }
                        // Eliminar el power-up recogido
                        powerUps.remove(powerUp)
                        break // Si un power-up es recogido, salimos del bucle
                    }
                }
            }
        }

        // Actualizar posición de los enemigos horizontales con ralentización de power-ups
        LaunchedEffect(Unit) {
            while (!isGameOver) {
                delay(10L) // Mover enemigos cada 10ms
                val speed = if (isSlowEnemies) 1 else 15 // Si está ralentizado, mueve más lento
                for (enemy in horizontalEnemies) {
                    enemy.x -= speed // Velocidad de movimiento en -X
                }
                // Eliminar enemigos que salgan de la pantalla
                horizontalEnemies.removeAll { it.x < -150f }
            }
        }


        Box(Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures {
                velocity = jumpForce
            }
        }) {

            // Fondo
            Image(
                painter = painterResource(id = backgroundRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )


            // Jugador
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = playerColor,
                    radius = 50f,
                    center = Offset(300f, playerY)
                )
            }

            // Enemigo
            // Cargar la imagen desde los recursos
            val ollaImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.olla)
            Canvas(Modifier.fillMaxSize()) {
                drawImage(
                    image = ollaImage,
                    topLeft = Offset(enemy.x, enemy.y),
                )
            }

            // Bolas lanzadas por el enemigo
            val ballImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.bola_fuego)
            balls.forEach { ball ->
                Canvas(Modifier.fillMaxSize()) {
                    drawImage(
                        image = ballImage,
                        topLeft = Offset(ball.x, ball.y),
                    )
                }
            }

            // Enemigos horizontales
            val tenedorImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.tenedor)
            horizontalEnemies.forEach { enemy ->
                Canvas(Modifier.fillMaxSize()) {
                    drawImage(
                        image = tenedorImage,
                        topLeft = Offset(enemy.x, enemy.y),
                    )
                }
            }

            //Torres
            towers.forEach { (sprite, position) ->
                Canvas(Modifier.fillMaxSize()) {
                    drawImage(
                        image = sprite,
                        topLeft = position
                    )
                }
            }

            // Dibujar los power-ups en la pantalla
            powerUps.forEach { powerUp ->
                Canvas(Modifier.fillMaxSize()) {
                    val color = when (powerUp.type) {
                        PowerUpType.IMMUNITY -> Color.Cyan  // Inmunidad
                        PowerUpType.SLOW_ENEMIES -> Color.Magenta  // Ralentización
                    }
                    drawCircle(
                        color = color,
                        radius = 20f,
                        center = Offset(powerUp.x, powerUp.y)
                    )
                }
            }

        }

        val teQuemasImage = R.drawable.te_quemas;

        // Verificar si la imagen "te quemas" debe ser mostrada
        if (isBurning) {
            Image(
                painter = painterResource(id = teQuemasImage),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )


            // Verificar si han pasado 5 segundos
            val elapsedTime = System.currentTimeMillis() - burnStartTime
            if (elapsedTime > 5000L) {
                releaseResources(backgroundMusic, soundManager, isRecording, audioRecord)
                onGameOver() // Terminar el juego después de 5 segundos

            }
        }




    }

    fun releaseResources(
        backgroundMusic: BackgroundMusic,
        soundManager: SoundManager,
        isRecording: Boolean,
        audioRecord: AudioRecord
    ){
        // Detener y liberar música de fondo
        backgroundMusic.stop()
        backgroundMusic.release()

        // Detener y liberar SoundManager
        soundManager.stopAll()
        soundManager.release()

        // Detener AudioRecord
        if (isRecording) {
            audioRecord.stop()
            audioRecord.release()
        }
        // Detener sensores
        sensorManager.unregisterListener(this)

        // Otras tareas de limpieza, si aplica
    }

    // Función para detectar colisiones entre el jugador y los obstáculos
    fun checkCollisions(
        playerX: Float,
        playerY: Float,
        playerWidth: Float,
        playerHeight: Float,
        towers: List<Pair<ImageBitmap, Offset>>,
        isImmune: Boolean,
        soundManager: SoundManager
    ): Boolean {
        if (isImmune) return false // Si el jugador es inmune, no hay colisión

        for ((sprite, position) in towers) {
            val towerLeft = position.x
            val towerRight = position.x + sprite.width
            val towerTop = position.y
            val towerBottom = position.y + sprite.height

            val playerLeft = playerX - playerWidth / 2
            val playerRight = playerX + playerWidth / 2
            val playerTop = playerY - playerHeight / 2
            val playerBottom = playerY + playerHeight / 2

            // Verificar si las áreas del jugador y la torre se solapan
            if (!(playerRight < towerLeft || playerLeft > towerRight || playerBottom < towerTop || playerTop > towerBottom)) {
                soundManager.playTowerSound()
                return true // Colisión detectada
            }
        }
        return false // No hay colisión
    }

    // Función para verificar la colisión entre el jugador y la bola
    fun checkCollisionBall(
        playerX: Float,
        playerY: Float,
        playerWidth: Float,
        playerHeight: Float,
        ballX: Float,
        ballY: Float,
        ballRadius: Float
    ): Boolean {
        val playerLeft = playerX - playerWidth
        val playerRight = playerX + playerWidth
        val playerTop = playerY - playerHeight
        val playerBottom = playerY + playerHeight

        val ballLeft = ballX - ballRadius
        val ballRight = ballX + ballRadius
        val ballTop = ballY - ballRadius
        val ballBottom = ballY + ballRadius

        // Verificar si hay colisión entre el jugador y la bola
        return !(playerRight < ballLeft || playerLeft > ballRight || playerBottom < ballTop || playerTop > ballBottom)
    }

    // Función para verificar la colisión entre el jugador y un enemigo horizontal
    fun checkCollisionHorizontalEnemy(
        playerX: Float, playerY: Float, playerWidth: Float, playerHeight: Float,
        enemyX: Float, enemyY: Float, enemyWidth: Float, enemyHeight: Float
    ): Boolean {
        val playerLeft = playerX - playerWidth / 2
        val playerRight = playerX + playerWidth / 2
        val playerTop = playerY - playerHeight / 2
        val playerBottom = playerY + playerHeight / 2

        val enemyLeft = enemyX
        val enemyRight = enemyX + enemyWidth
        val enemyTop = enemyY
        val enemyBottom = enemyY + enemyHeight

        // Verificar si hay colisión
        return !(playerRight < enemyLeft || playerLeft > enemyRight || playerBottom < enemyTop || playerTop > enemyBottom)
    }

    // Función para verificar la colisión entre el jugador y un power-up
    fun checkCollisionPU(
        playerX: Float,
        playerY: Float,
        playerWidth: Float,
        playerHeight: Float,
        powerUpX: Float,
        powerUpY: Float,
        powerUpRadius: Float
    ): Boolean {
        val playerLeft = playerX - playerWidth / 2
        val playerRight = playerX + playerWidth / 2
        val playerTop = playerY - playerHeight / 2
        val playerBottom = playerY + playerHeight / 2

        val powerUpLeft = powerUpX - powerUpRadius
        val powerUpRight = powerUpX + powerUpRadius
        val powerUpTop = powerUpY - powerUpRadius
        val powerUpBottom = powerUpY + powerUpRadius

        // Verificar si hay colisión entre el jugador y el power-up
        return !(playerRight < powerUpLeft || playerLeft > powerUpRight || playerBottom < powerUpTop || playerTop > powerUpBottom)
    }

    data class Enemy(
        var x: Float,  // Posición X fija del enemigo (siempre en el borde derecho)
        var y: Float   // Posición Y que varía con el tiempo
    )

    data class HorizontalEnemy(
        var x: Float,  // Posición X del enemigo
        var y: Float,  // Posición Y del enemigo
        val width: Float = 150f, // Ancho del rectángulo
        val height: Float = 50f  // Altura del rectángulo
    )


    data class Ball(
        var x: Float,  // Posición X de la bola (se mueve de derecha a izquierda)
        var y: Float   // Posición Y de la bola
    )

    // Clase para definir obstáculos
    data class Obstacle(
        var x: Int,
        val gapPosition: Int
    )

    data class PowerUp(
        var x: Float, // Posición X
        var y: Float, // Posición Y
        val type: PowerUpType // Tipo de power-up
    )

    enum class PowerUpType {
        IMMUNITY,  // Inmunidad
        SLOW_ENEMIES // Ralentizar enemigos
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calcular la magnitud del vector de aceleración
            val acceleration = sqrt(x * x + y * y + z * z)
            val currentTime = System.currentTimeMillis()

            if (acceleration > shakeThreshold && currentTime - lastShakeTime > 1000) {
                lastShakeTime = currentTime
                onShakeDetected()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario implementar para este caso
    }

    private fun onShakeDetected() {
        // Invoca la lógica del juego para manejar el agite
        // Esta será pasada al `GameScreen` como parámetro
    }

    private fun activatePowerUps() {
        // Maneja la lógica de activación de power-ups aquí si es necesario
    }


}