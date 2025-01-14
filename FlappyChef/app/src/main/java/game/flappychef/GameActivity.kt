package game.flappychef

import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.drawscope.withTransform
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
import androidx.activity.ComponentActivity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlin.random.Random
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.coroutines.launch

class GameActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var playerY = mutableStateOf(500f)
    private var puntuacion = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            GameScreen(
                playerY = playerY,
                puntuacion = puntuacion,
                onGameOver = {
                    val finalScore = puntuacion.value
                    val intent = Intent(this, GameOverActivity::class.java).apply {
                        putExtra("finalScore", finalScore)
                    }
                    finish()
                    startActivity(intent)
                }
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val z = event.values[2]
            val sensitivity = -10f
            val deltaZ = z * sensitivity

            runOnUiThread {
                playerY.value = (playerY.value + deltaZ).coerceIn(0f, 1920f)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

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

    @Composable
    fun GameScreen(
        playerY: MutableState<Float>,
        puntuacion: MutableState<Int>,
        onGameOver: () -> Unit) {
        val context = LocalContext.current
        val soundManager = remember { SoundManager(context) }
        val backgroundMusic = remember { BackgroundMusic(context) }

        DisposableEffect(Unit) {
            onDispose {
                backgroundMusic.stop()
                backgroundMusic.release()
            }
        }

        var isGamePaused by remember { mutableStateOf(true) }
        var playerX by remember { mutableStateOf(300f) }
        val playerWidth = 100f
        val playerHeight = 100f

        val enemy = remember {
            Enemy(
                x = 780f,
                y = Random.nextInt(100, 1820).toFloat()
            )
        }
        val balls = remember { mutableStateListOf<Ball>() }
        var playerColor by remember { mutableStateOf(Color.Red) }
        val horizontalEnemies = remember { mutableStateListOf<HorizontalEnemy>() }
        var isGameOver by remember { mutableStateOf(false) }

        var showBurnImage by remember { mutableStateOf(false) }

        val configuration = LocalConfiguration.current
        val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
        val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }


        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val backgroundRes = if (currentHour in 8..17) {
            R.drawable.fondo_dia
        } else {
            R.drawable.fondo_noche
        }

        //Micro
        val micThreshold = 15000
        var micAmplitude by remember { mutableStateOf(0) }
        var isRecording by remember { mutableStateOf(false) }
        var isBurning by remember { mutableStateOf(false) }

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

        // Recuperar la imagen guardada desde SharedPreferences
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val profileImagePath = prefs.getString("profile_image", null)


        var profileImageBitmap by remember {
            mutableStateOf<ImageBitmap?>(null)
        }

        //Powerups
        val powerUps = remember { mutableStateListOf<PowerUp>() }
        var isImmune by remember { mutableStateOf(false) }
        var isSlowEnemies by remember { mutableStateOf(false) }
        val towers = remember { mutableStateListOf<Pair<ImageBitmap, Offset>>() } // Lista de torres con su posici贸n

        // Cargar los sprites de las torres
        val torreAltaImage = ImageBitmap.imageResource(id = R.drawable.torre_alta)
        val torreMediaImage = ImageBitmap.imageResource(id = R.drawable.torre_media)
        val torreBajaImage = ImageBitmap.imageResource(id = R.drawable.torre_baja)
        val towerSprites = listOf(torreAltaImage, torreMediaImage, torreBajaImage)


        // Cargar la imagen de perfil desde la ruta si existe
        LaunchedEffect(profileImagePath) {
            profileImagePath?.let { path ->
                val playerBitmap = BitmapFactory.decodeFile(path)
                profileImageBitmap = playerBitmap?.asImageBitmap()
            }
        }

        if(!isGamePaused){
            backgroundMusic.start()

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

                        if (micAmplitude > micThreshold && isBurning) {
                            isBurning = false
                            puntuacion.value += 5
                        }

                        delay(100L)
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
                            x = enemy.x + 50,
                            y = enemy.y + 40
                        )
                    )

                }
            }


            // Mover las bolas de derecha a izquierda
            LaunchedEffect(balls) {
                while (true) {
                    delay(10L)
                    balls.forEach { ball ->
                        ball.x -= 10
                    }

                    balls.removeAll { it.x < 0 }
                }
            }

            // Generar torres aleatorias cada 3 segundos
            LaunchedEffect(Unit) {
                while (true) {
                    delay(3000L)
                    val randomSprite = towerSprites.random()
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
                    delay(10L)
                    towers.replaceAll { (sprite, position) ->
                        sprite to position.copy(x = position.x - 5)
                    }
                    towers.removeAll { (_, position) -> position.x < -100 }
                }
            }

            // Mover los power-ups de derecha a izquierda
            LaunchedEffect(powerUps) {
                while (true) {
                    delay(10L)
                    powerUps.forEach { powerUp ->
                        powerUp.x -= 3
                    }
                    powerUps.removeAll { it.x < 0 }
                }
            }

            // Generar power-ups cada segundo
            LaunchedEffect(Unit) {
                while (!isGameOver) {
                    delay(6500L)

                    val randomX = 1000f
                    val randomY = Random.nextInt(100, screenHeightPx.toInt() - 100).toFloat()
                    val powerUpType =
                        if (Random.nextBoolean()) PowerUpType.IMMUNITY else PowerUpType.SLOW_ENEMIES

                    powerUps.add(PowerUp(x = randomX, y = randomY, type = powerUpType))

                }
            }

            // Detectar colisiones entre el jugador y los power-ups
            LaunchedEffect(powerUps) {
                while (!isGameOver) {
                    delay(10L)
                    for (powerUp in powerUps) {
                        if (checkCollisionPU(playerX, playerY.value, 50f, 50f, powerUp.x, powerUp.y, 20f)) {

                            when (powerUp.type) {
                                PowerUpType.IMMUNITY -> {
                                    soundManager.playPowerUp2Sound()
                                    if (!isImmune) {
                                        isImmune = true
                                        launch {
                                            delay(10000L)
                                            isImmune = false
                                        }
                                    }
                                }

                                PowerUpType.SLOW_ENEMIES -> {
                                    soundManager.playPowerUp1Sound()
                                    if (!isSlowEnemies) {
                                        isSlowEnemies = true

                                        launch {
                                            delay(10000L)
                                            isSlowEnemies = false
                                        }
                                    }
                                }
                            }

                            powerUps.remove(powerUp)
                            break
                        }
                    }
                }
            }


            // Cambiar la altura del enemigo aleatoriamente
            LaunchedEffect(Unit) {
                while (true) {
                    delay(3000L)
                    enemy.y = Random.nextInt(100, 1820).toFloat()
                }
            }

            // Generar enemigos horizontales cada 5 segundos
            LaunchedEffect(Unit) {
                while (true) {
                    soundManager.playForkSound()
                    delay(3000L)
                    horizontalEnemies.add(
                        HorizontalEnemy(
                            x = screenWidthPx,
                            y = Random.nextInt(100, 1820).toFloat()
                        )
                    )
                }
            }

            // Actualizar posici贸n de los enemigos horizontales
            LaunchedEffect(Unit) {
                while (true) {
                    delay(15L)
                    for (enemy in horizontalEnemies) {
                        enemy.x -= 15
                    }

                    horizontalEnemies.removeAll { it.x < -150f }
                }
            }

            //Colisiones con torres de platos
            LaunchedEffect(towers) {
                while (!isGameOver) {
                    delay(10L)

                    if (checkCollisions(playerX, playerY.value, 50f, 50f, towers, isImmune, soundManager)) {

                        if (!isGameOver) {
                            isGameOver = true
                            onGameOver()
                        }
                        break
                    }
                }
            }

            // Movimiento del jugador y detecci贸n de colisiones con limites
            LaunchedEffect(Unit) {
                while (!isGameOver) {
                    delay(10L)

                    if (playerY.value >= screenHeightPx - 50f || playerY.value <= 0f) {
                        if (!isGameOver) {
                            isGameOver = true
                            onGameOver()

                        }
                        break
                    }
                }
            }

            // Detectar colisiones entre el jugador y las bolas
            LaunchedEffect(balls) {
                while (true) {
                    delay(10L)
                    for (ball in balls) {
                        if (checkCollisionBall(
                                playerX, playerY.value,
                                50f, 50f,
                                ball.x, ball.y, 30f
                            )
                        ) {
                            showBurnImage = true
                            isBurning = true
                            break
                        }

                    }
                }
            }

            // Incrementar la puntuaci贸n cada segundo
            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000L)
                    puntuacion.value += 1
                }
            }


            // Detecci贸n de colisiones con enemigos horizontales
            LaunchedEffect(horizontalEnemies) {
                while (!isGameOver) {
                    delay(10L)
                    for (enemy in horizontalEnemies) {
                        if (checkCollisionHorizontalEnemy(
                                playerX,
                                playerY.value,
                                25f, 25f,
                                enemy.x,
                                enemy.y,
                                enemy.width,
                                enemy.height
                            )
                        ) {
                            if (!isGameOver) {
                                isGameOver = true
                                onGameOver()
                            }
                            return@LaunchedEffect
                        }
                    }
                }
            }

            // Actualizar posici贸n de los enemigos horizontales con ralentizaci贸n de power-ups
            LaunchedEffect(Unit) {
                while (!isGameOver) {
                    delay(10L)
                    val speed = if (isSlowEnemies) 1 else 15
                    for (enemy in horizontalEnemies) {
                        enemy.x -= speed
                    }
                    horizontalEnemies.removeAll { it.x < -150f }
                }
            }


        }


        Box(Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures {
                if (isGamePaused) {
                    isGamePaused = false

                }
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
                if (profileImageBitmap != null) {
                    withTransform({
                        translate(left = playerX - playerWidth / 2, top = playerY.value - playerHeight / 2)
                        scale(
                            scaleX = playerWidth / profileImageBitmap!!.width,
                            scaleY = playerHeight / profileImageBitmap!!.height
                        )
                    }) {
                        drawImage(image = profileImageBitmap!!)
                    }
                } else {
                    drawCircle(
                        color = playerColor,
                        radius = playerWidth / 2,
                        center = Offset(playerX, playerY.value)
                    )
                }
            }
            if (!isGamePaused) {
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

                val PU_invencible: ImageBitmap =
                    ImageBitmap.imageResource(id = R.drawable.powerup_invencible)
                val PU_slow: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.powerup_slow)

                // Dibujar los Power-Ups en la pantalla
                powerUps.forEach { powerUp ->
                    Canvas(Modifier.fillMaxSize()) {
                        val image = when (powerUp.type) {
                            PowerUpType.IMMUNITY -> PU_invencible
                            PowerUpType.SLOW_ENEMIES -> PU_slow
                        }
                        drawImage(
                            image = image,
                            topLeft = Offset(powerUp.x, powerUp.y)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${puntuacion.value}",
                        fontSize = 38.sp,
                        color = Color.White
                    )
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

                }

                // Boton de Pausa/Resumir
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(26.dp)
                ) {
                    Button(
                        onClick = { isGamePaused = !isGamePaused },
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)){
                        if(!isGamePaused){
                            Text(
                                text = "P",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                        }
                    }
                }

            }else{
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000).copy(alpha = 0.7f))
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Text(
                            text = "COLOQUE EL PERSONAJE Y TOQUE LA PANTALLA",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center,
                            textDecoration = TextDecoration.None
                        )
                    }
                }
            }
        }
    }

    // Funcion para detectar colisiones entre el jugador y los obstaculos
    fun checkCollisions(
        playerX: Float,
        playerY: Float,
        playerWidth: Float,
        playerHeight: Float,
        towers: List<Pair<ImageBitmap, Offset>>,
        isImmune: Boolean,
        soundManager: SoundManager
    ): Boolean {
        if (isImmune) return false

        for ((sprite, position) in towers) {
            val towerLeft = position.x
            val towerRight = position.x + sprite.width
            val towerTop = position.y
            val towerBottom = position.y + sprite.height

            val playerLeft = playerX - playerWidth / 2
            val playerRight = playerX + playerWidth / 2
            val playerTop = playerY - playerHeight / 2
            val playerBottom = playerY + playerHeight / 2


            if (!(playerRight < towerLeft || playerLeft > towerRight || playerBottom < towerTop || playerTop > towerBottom)) {
                soundManager.playTowerSound()
                return true
            }
        }
        return false
    }

    // Funcion para verificar la colision entre el jugador y la bola
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

        return !(playerRight < ballLeft || playerLeft > ballRight || playerBottom < ballTop || playerTop > ballBottom)
    }

    // Funcion para verificar la colision entre el jugador y un enemigo horizontal
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

        return !(playerRight < enemyLeft || playerLeft > enemyRight || playerBottom < enemyTop || playerTop > enemyBottom)
    }

    // Funcion para verificar la colision entre el jugador y un power-up
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

        return !(playerRight < powerUpLeft || playerLeft > powerUpRight || playerBottom < powerUpTop || playerTop > powerUpBottom)
    }

    data class Enemy(
        var x: Float,
        var y: Float
    )

    data class HorizontalEnemy(
        var x: Float,
        var y: Float,
        val width: Float = 150f,
        val height: Float = 50f
    )


    data class Ball(
        var x: Float,
        var y: Float
    )

    // Clase para definir obstaculos
    data class Obstacle(
        var x: Int,
        val gapPosition: Int
    )

    data class PowerUp(
        var x: Float,
        var y: Float,
        val type: PowerUpType
    )

    enum class PowerUpType {
        IMMUNITY,
        SLOW_ENEMIES
    }
}