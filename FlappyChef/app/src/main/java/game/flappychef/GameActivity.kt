package game.flappychef

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
    var playerY by remember { mutableStateOf(500f) }
    var velocity by remember { mutableStateOf(0f) }

    val gravity = 1f
    val jumpForce = -15f
    val obstacles = remember { mutableStateListOf<Obstacle>() }
    val obstacleWidth = 100f
    val obstacleGap = 700f

    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }

    // Obtener la hora actual y determinar el fondo
    val currentHour = remember { Random.nextInt(0, 24) }
    val backgroundRes = if (currentHour in 8..17) {
        R.drawable.fondo_dia // Fondo de día
    } else {
        R.drawable.fondo_noche // Fondo de noche
    }

    // Generar obstáculos periódicamente
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500L)
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
            if (playerY >= screenHeightPx - 50f || playerY <= 0f) {
                onGameOver()
                break
            }

            // Verificar colisiones con obstáculos
            if (checkCollisions(playerY, obstacles, screenHeightPx, obstacleWidth, obstacleGap)) {
                onGameOver()
                break
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

        // Jugador
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Red,
                radius = 50f,
                center = Offset(300f, playerY)
            )
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
fun checkCollisions(playerY: Float, obstacles: List<Obstacle>, screenHeightPx: Float, obstacleWidth: Float, obstacleGap: Float): Boolean {
    val playerLeft = 300f - 50f
    val playerRight = 300f + 50f
    val playerTop = playerY - 50f
    val playerBottom = playerY + 50f

    for (obstacle in obstacles) {
        val upperObstacleHeight = obstacle.gapPosition.toFloat()
        val lowerObstacleHeight = screenHeightPx - (obstacle.gapPosition + obstacleGap).toFloat()

        val upperObstacleLeft = obstacle.x
        val upperObstacleRight = obstacle.x + obstacleWidth
        val upperObstacleTop = 0f
        val upperObstacleBottom = upperObstacleHeight

        val lowerObstacleLeft = obstacle.x
        val lowerObstacleRight = obstacle.x + obstacleWidth
        val lowerObstacleTop = obstacle.gapPosition + obstacleGap
        val lowerObstacleBottom = screenHeightPx

        // Verificar colisión con el obstáculo superior
        val collidesWithUpperObstacle = playerRight > upperObstacleLeft &&
                playerLeft < upperObstacleRight &&
                playerBottom > upperObstacleTop &&
                playerTop < upperObstacleBottom

        // Verificar colisión con el obstáculo inferior
        val collidesWithLowerObstacle = playerRight > lowerObstacleLeft &&
                playerLeft < lowerObstacleRight &&
                playerBottom > lowerObstacleTop &&
                playerTop < lowerObstacleBottom

        if (collidesWithUpperObstacle || collidesWithLowerObstacle) {
            return true
        }
    }

    return false
}

// Clase para definir obstáculos
data class Obstacle(
    var x: Int,
    val gapPosition: Int
)
