package game.flappychef

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.icu.util.Calendar
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GameScreen(
                onGameOver = {
                    // Aquí no terminamos la actividad, simplemente mostramos la pantalla Game Over
                    val intent = Intent(this, GameOverActivity::class.java)
                    startActivity(intent)
                }
            )
        }
    }
}
@Composable
fun GameScreen(onGameOver: () -> Unit) {
    var playerY by remember { mutableStateOf(400f) }
    var playerX by remember { mutableStateOf(300f) }
    val playerWidth = 75f
    val playerHeight = 75f

    var velocity by remember { mutableStateOf(0f) }
    val gravity = 0.4f
    val jumpForce = -9f
    val obstacles = remember { mutableStateListOf<Obstacle>() }
    val obstacleWidth = 100f
    val obstacleGap = 700f

    val enemy = remember { Enemy(x = 950f, y = Random.nextInt(100, 600).toFloat()) } // Enemigo en el borde derecho
    val balls = remember { mutableStateListOf<Ball>() }
    var playerColor by remember { mutableStateOf(Color.Red) } // Color inicial del jugador (rojo)
    val horizontalEnemies = remember { mutableStateListOf<HorizontalEnemy>() } // Lista de enemigos horizontales


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

    // Recuperar la imagen guardada desde SharedPreferences
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    val profileImageUri = prefs.getString("profile_image", null)

    // Convertir la ruta del archivo en un bitmap
    val playerBitmap = remember(profileImageUri) {
        profileImageUri?.let { uri ->
            BitmapFactory.decodeFile(uri)
        }
    }


    // Generar bolas cada 20 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L) // Esperar 20 segundos
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
            delay(5000L) // Crear un enemigo cada 5 segundos
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
            delay(10L) // Mover enemigos cada 10ms
            for (enemy in horizontalEnemies) {
                enemy.x -= 15 // Velocidad de movimiento en -X
            }
            // Eliminar enemigos que salgan de la pantalla
            horizontalEnemies.removeAll { it.x < -150f }
        }
    }



    // Generar obstáculos periódicamente
    LaunchedEffect(Unit) {
        while (true) {
            delay(8000L)
            obstacles.add(
                Obstacle(
                    x = screenWidthPx.toInt(),
                    gapPosition = Random.nextInt((screenHeightPx - obstacleGap).toInt())
                )
            )
        }
    }

    // Actualizar posición de obstáculos
    LaunchedEffect(Unit) {
        while (true) {
            delay(10L)
            for (obstacle in obstacles) {
                obstacle.x -= 5
            }
            obstacles.removeAll { it.x < -obstacleWidth }
        }
    }

    // Movimiento del jugador y detección de colisiones
    LaunchedEffect(Unit) {
        while (true) {
            delay(10L)
            playerY += velocity
            velocity += gravity

            // Terminar el juego si el jugador sale de los límites
            if (playerY >= screenHeightPx - playerHeight || playerY <= 0f) {
                onGameOver()
                break
            }

            // Verificar colisiones con obstáculos
            if (checkCollisions(playerY, obstacles, screenHeightPx, obstacleWidth, obstacleGap, playerWidth, playerHeight)) {
                onGameOver()
                break
            }
        }
    }

    // Detectar colisiones entre el jugador y las bolas
    LaunchedEffect(balls) {
        while (true) {
            delay(10L) // Chequear cada 10ms
            balls.forEach { ball ->
                if (checkCollisionBall(playerX, playerY, 50f, 50f, ball.x, ball.y, 10f)) {
                    // Si hay colisión, cambiar el color del jugador a azul
                    playerColor = Color.Blue
                    // Volver al color original después de 5 segundos
                    delay(5000L)
                    playerColor = Color.Red
                }
            }
        }
    }
    // Detectar colisiones entre el jugador y los enemigos horizontales
    LaunchedEffect(horizontalEnemies) {
        while (true) {
            delay(10L) // Chequear cada 10ms
            horizontalEnemies.forEach { enemy ->
                if (checkCollisionHorizontalEnemy(playerX, playerY, 50f, 50f, enemy.x, enemy.y, enemy.width, enemy.height)
                ) {
                    onGameOver() // Terminar el juego si hay colisión
                }
            }
        }
    }

    // Dibujo de la pantalla del juego
    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { velocity = jumpForce }
            }
    ) {
        // Fondo
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Jugador (imagen guardada o predeterminada si no hay imagen)
        if (playerBitmap != null) {
            Image(
                bitmap = playerBitmap.asImageBitmap(),
                contentDescription = "Jugador",
                modifier = Modifier
                    .size(playerWidth.dp, playerHeight.dp)
                    .offset(x = 50.dp, y = playerY.dp)
            )
        } else {
            // Si no hay imagen guardada, mostrar un círculo rojo
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.Red,
                    radius = 50f,
                    center = Offset(50f, playerY)
                )
            }
        }


        // Enemigo
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                color = Color.Red,
                topLeft = Offset(enemy.x, enemy.y),
                size = androidx.compose.ui.geometry.Size(100f, 100f) // El enemigo es un cuadrado de 50x50
            )
        }

        // Bolas lanzadas por el enemigo
        balls.forEach { ball ->
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.Yellow,
                    radius = 30f,
                    center = Offset(ball.x, ball.y)
                )
            }
        }

        // Enemigos horizontales
        horizontalEnemies.forEach { enemy ->
            Canvas(Modifier.fillMaxSize()) {
                drawRect(
                    color = Color.Magenta,
                    topLeft = Offset(enemy.x, enemy.y),
                    size = androidx.compose.ui.geometry.Size(enemy.width, enemy.height)
                )
            }
        }

        // Obstáculos
        for (obstacle in obstacles) {
            val upperObstacleHeight = obstacle.gapPosition.toFloat()
            val lowerObstacleHeight = screenHeightPx - (obstacle.gapPosition + obstacleGap).toFloat()

            Canvas(Modifier.fillMaxSize()) {
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(obstacle.x.toFloat(), 0f),
                    size = androidx.compose.ui.geometry.Size(obstacleWidth, upperObstacleHeight)
                )
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(obstacle.x.toFloat(), obstacle.gapPosition + obstacleGap),
                    size = androidx.compose.ui.geometry.Size(obstacleWidth, lowerObstacleHeight)
                )
            }
        }
    }
}
// Función para detectar colisiones entre el jugador y los obstáculos
fun checkCollisions(
    playerY: Float,
    obstacles: List<Obstacle>,
    screenHeightPx: Float,
    obstacleWidth: Float,
    obstacleGap: Float,
    playerWidth: Float,
    playerHeight: Float
): Boolean {
    // Posición del jugador ajustada a 50f en el eje X (como en el dibujo)
    val playerLeft = 50f
    val playerRight = playerLeft + playerWidth
    val playerTop = playerY
    val playerBottom = playerY + playerHeight

    for (obstacle in obstacles) {
        // Dimensiones del obstáculo superior
        val upperObstacleLeft = obstacle.x
        val upperObstacleRight = obstacle.x + obstacleWidth
        val upperObstacleTop = 0f
        val upperObstacleBottom = obstacle.gapPosition.toFloat()

        // Dimensiones del obstáculo inferior
        val lowerObstacleLeft = obstacle.x
        val lowerObstacleRight = obstacle.x + obstacleWidth
        val lowerObstacleTop = obstacle.gapPosition + obstacleGap
        val lowerObstacleBottom = screenHeightPx

        // Verificar colisión con el obstáculo superior
        val collidesWithUpperObstacle =
            playerRight > upperObstacleLeft &&  // El jugador choca por la derecha
                    playerLeft < upperObstacleRight && // El jugador choca por la izquierda
                    playerBottom > upperObstacleTop && // El jugador está dentro del área vertical
                    playerTop < upperObstacleBottom

        // Verificar colisión con el obstáculo inferior
        val collidesWithLowerObstacle =
            playerRight > lowerObstacleLeft &&
                    playerLeft < lowerObstacleRight &&
                    playerBottom > lowerObstacleTop &&
                    playerTop < lowerObstacleBottom

        // Si colisiona con cualquiera de los obstáculos, devuelve true
        if (collidesWithUpperObstacle || collidesWithLowerObstacle) {
            return true
        }
    }
    return false
}


// Función para verificar la colisión entre el jugador y la bola
fun checkCollisionBall(playerX: Float, playerY: Float, playerWidth: Float, playerHeight: Float,
                       ballX: Float, ballY: Float, ballRadius: Float): Boolean {
    val playerLeft = playerX - playerWidth / 2
    val playerRight = playerX + playerWidth / 2
    val playerTop = playerY - playerHeight / 2
    val playerBottom = playerY + playerHeight / 2

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
