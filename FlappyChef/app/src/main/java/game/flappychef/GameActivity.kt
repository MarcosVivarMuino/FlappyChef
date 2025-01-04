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

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GameScreen(
                onGameOver = {
                    finish()
                    val intent = Intent(this, GameOverActivity::class.java)
                    startActivity(intent)
                }
            )
        }
    }
}
@Composable
fun GameScreen(onGameOver: () -> Unit) {
    var playerY by remember { mutableStateOf(500f) }
    var playerX by remember { mutableStateOf(300f) }
    var velocity by remember { mutableStateOf(0f) }

    val gravity = 1f
    val jumpForce = -15f
    val obstacles = remember { mutableStateListOf<Obstacle>() }
    val obstacleWidth = 100f
    val obstacleGap = 700f
    val enemy = remember { Enemy(x = 780f, y = Random.nextInt(100, 1820).toFloat()) } // Enemigo en el borde derecho
    val balls = remember { mutableStateListOf<Ball>() }
    var playerColor by remember { mutableStateOf(Color.Red) } // Color inicial del jugador (rojo)
    val horizontalEnemies = remember { mutableStateListOf<HorizontalEnemy>() } // Lista de enemigos horizontales
    var isGameOver by remember { mutableStateOf(false) }

    var showBurnImage by remember { mutableStateOf(false) }  // Para controlar la visibilidad de la imagen
    var burnStartTime by remember { mutableStateOf(0L) }       // Para guardar el tiempo de inicio del contador

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

    //Crear bolas
    LaunchedEffect(Unit) {
        while (true) {
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
            delay(2000L) // Crear un enemigo cada 5 segundos
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
            delay(4000L)
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

    // Movimiento del jugador y detección de colisiones con obstáculos
    LaunchedEffect(Unit) {
        while (!isGameOver) {
            delay(10L)
            playerY += velocity
            velocity += gravity

            // Si el jugador sale de los límites, activa Game Over
            if (playerY >= screenHeightPx - 50f || playerY <= 0f) {
                if (!isGameOver) {
                    isGameOver = true
                    onGameOver()
                }
                break
            }

            // Verificar colisiones con obstáculos
            if (checkCollisions(playerY, obstacles, screenHeightPx, obstacleWidth, obstacleGap)) {
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
            delay(10L) // Chequear cada 10ms
            // Usamos un for convencional para poder usar break
            for (ball in balls) {
                if (checkCollisionBall(playerX, playerY,
                        50f, 50f,
                        ball.x, ball.y, 10f)) {
                    // Si hay colisión, mostrar la imagen de "te_quemas" y empezar el temporizador
                    showBurnImage = true
                    burnStartTime = System.currentTimeMillis()  // Guardar el momento de la colisión

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
                        onGameOver()
                    }
                    return@LaunchedEffect
                }
            }
        }
    }



    // Detectar la acción de "Soplar" (simulada con un toque en la pantalla)
    Box(Modifier.fillMaxSize().pointerInput(Unit) {
        detectTapGestures {
            velocity = jumpForce

            if (showBurnImage) {
                showBurnImage = false
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

        // Obstáculos
        for (obstacle in obstacles) {
            val torreAltaImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.torre_alta)

            Canvas(Modifier.fillMaxSize()) {
                drawImage(
                    image = torreAltaImage,
                    topLeft = Offset(
                        x = obstacle.x.toFloat(), // Posición horizontal del obstáculo
                        y = 0f
                    )
                )

            }
        }
    }

    val teQuemasImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.te_quemas)

    // Dibujar la imagen cuando el jugador se ha quemado
    if (showBurnImage) {
        Canvas(Modifier.fillMaxSize()) {
            // Centrar la imagen en la pantalla
            val imageWidth = teQuemasImage.width.toFloat()
            val imageHeight = teQuemasImage.height.toFloat()
            val offsetX = screenWidthPx - imageWidth
            val offsetY = screenHeightPx - imageHeight

            // Dibujar la imagen en el centro de la pantalla
            drawImage(
                image = teQuemasImage,
                topLeft = Offset(x = offsetX, y = offsetY)
            )
        }

        // Verificar si han pasado 5 segundos
        val elapsedTime = System.currentTimeMillis() - burnStartTime
        if (elapsedTime > 5000L) {
            onGameOver()
        }
    }
}

// Función para detectar colisiones entre el jugador y los obstáculos
fun checkCollisions(playerY: Float, obstacles: List<Obstacle>, screenHeightPx: Float, obstacleWidth: Float, obstacleGap: Float): Boolean {
    // Posiciones del jugador
    val playerLeft = 300f - 50f
    val playerRight = 300f + 50f
    val playerTop = playerY - 50f
    val playerBottom = playerY + 50f

    for (obstacle in obstacles) {
        // Coordenadas del obstáculo superior
        val upperObstacleLeft = obstacle.x
        val upperObstacleRight = obstacle.x + obstacleWidth
        val upperObstacleTop = 0f
        val upperObstacleBottom = obstacle.gapPosition.toFloat()


        // Verificar colisión con el obstáculo superior
        val collidesWithUpperObstacle = playerRight > upperObstacleLeft &&
                playerLeft < upperObstacleRight &&
                playerBottom > upperObstacleTop &&
                playerTop < upperObstacleBottom


        // Si colisiona con cualquiera de los dos, retorna true
        if (collidesWithUpperObstacle) {
            return true
        }
    }
    return false
}

// Función para verificar la colisión entre el jugador y la bola
fun checkCollisionBall(playerX: Float, playerY: Float, playerWidth: Float, playerHeight: Float, ballX: Float, ballY: Float, ballRadius: Float): Boolean {
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
