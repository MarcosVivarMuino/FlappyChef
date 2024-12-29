package game.flappychef

import android.content.Intent
import android.media.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import java.util.Calendar
import kotlin.random.Random



class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameScreen()
        }
    }
}


@Composable
fun GameScreen() {
    var playerY by remember { mutableStateOf(500f) }
    var velocity by remember { mutableStateOf(0f) }
    var isGameOver by remember { mutableStateOf(false) }

    val gravity = 1f
    val jumpForce = -15f

    // Obtener las dimensiones reales de la pantalla en píxeles
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val screenHeightPx = with(LocalDensity.current) { screenHeightDp.dp.toPx() }

    val screenWidthDp = configuration.screenWidthDp
    val screenWidthPx = with(LocalDensity.current) { screenWidthDp.dp.toPx() }

    // Obtener hora actual
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    // Seleccionar el fondo según la hora
    val backgroundRes = if (currentHour in 8..17) {
        R.drawable.fondo_dia // Fondo de día
    } else {
        R.drawable.fondo_noche // Fondo de noche
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Cyan)
    ) {
        // Dibujar el fondo adaptado a la pantalla
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),  // Asegura que la imagen ocupe toda la pantalla
            contentScale = ContentScale.Crop   // Asegura que la imagen cubra completamente el fondo, recortándola si es necesario
        )

        // Todo lo demás (personaje, obstáculos, etc.)
        GameContent(
            screenHeightPx = screenHeightPx,
            screenWidthPx = screenWidthPx,
            onGameOver = { isGameOver = true } // Llamar la función cuando el juego termine
        )
    }
}

@Composable
fun GameContent(screenHeightPx: Float, screenWidthPx: Float, onGameOver: () -> Unit) {
    var playerY by remember { mutableStateOf(500f) }
    var velocity by remember { mutableStateOf(0f) }
    var isGameOver by remember { mutableStateOf(false) }

    val gravity = 1f
    val jumpForce = -15f
    val obstacles = remember { mutableStateListOf<Obstacle>() }

    // Dimensiones de los obstáculos
    val obstacleWidth = 100f
    val obstacleGap = 300f // Aumentar el hueco entre los obstáculos

    // Generar obstáculos cada cierto tiempo
    LaunchedEffect(Unit) {
        while (!isGameOver) {
            delay(1500L) // Aproximadamente un obstáculo cada 1.5 segundos

            // Crear un nuevo obstáculo
            obstacles.add(
                Obstacle(
                    x = screenWidthPx.toInt(),
                    gapPosition = Random.nextInt((screenHeightPx - obstacleGap).toInt())
                )
            )
        }
    }

    // Actualizar posición de los obstáculos
    LaunchedEffect(Unit) {
        while (!isGameOver) {
            delay(10L) // ~60 FPS
            for (obstacle in obstacles) {
                obstacle.x -= 5 // Mover los obstáculos hacia el personaje
            }

            // Eliminar obstáculos que ya no son visibles
            obstacles.removeAll { it.x < -obstacleWidth }
        }
    }

    // Lógica de movimiento del jugador
    LaunchedEffect(Unit) {
        while (!isGameOver) {
            delay(10L) // ~60 FPS
            playerY += velocity
            velocity += gravity

            // Detectar si la bola toca el suelo o el techo
            if (playerY >= screenHeightPx - 50f) { // 50f es el radio del personaje
                playerY = screenHeightPx - 50f // Coloca al jugador en el suelo
                velocity = 0f // Detener el movimiento vertical
                onGameOver() // Terminar el juego
            }

            if (playerY <= 0f) {
                playerY = 0f // No dejar que pase del techo
                velocity = 0f // Detener el movimiento vertical
                onGameOver() // Terminar el juego
            }

            // Detectar colisiones con obstáculos
            for (obstacle in obstacles) {
                val upperObstacleHeight = obstacle.gapPosition.toFloat()
                val lowerObstacleHeight = screenHeightPx - (obstacle.gapPosition + obstacleGap).toFloat()

                // Verificar colisión con el obstáculo superior
                if (playerY - 50f < upperObstacleHeight && playerY + 50f > 0f && 300f + 50f > obstacle.x && 300f - 50f < obstacle.x + obstacleWidth) {
                    onGameOver() // Terminar el juego
                }

                // Verificar colisión con el obstáculo inferior
                if (playerY + 50f > obstacle.gapPosition + obstacleGap && playerY - 50f < screenHeightPx && 300f + 50f > obstacle.x && 300f - 50f < obstacle.x + obstacleWidth) {
                    onGameOver() // Terminar el juego
                }
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    if (!isGameOver) {
                        velocity = jumpForce // Aplicar fuerza de salto cuando se toca la pantalla
                    }
                }
            }
    ) {
        // Dibujar personaje
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Red,
                radius = 50f,
                center = Offset(300f, playerY)
            )
        }

        // Dibujar obstáculos
        for (obstacle in obstacles) {
            val upperObstacleHeight = obstacle.gapPosition.toFloat()
            val lowerObstacleHeight = screenHeightPx - (obstacle.gapPosition + obstacleGap).toFloat()

            Canvas(Modifier.fillMaxSize()) {
                // Torre superior
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(obstacle.x.toFloat(), 0f),
                    size = androidx.compose.ui.geometry.Size(obstacleWidth, upperObstacleHeight)
                )
                // Torre inferior
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(
                        obstacle.x.toFloat(),
                        obstacle.gapPosition + obstacleGap
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        obstacleWidth,
                        lowerObstacleHeight
                    )
                )
            }
        }

        // Si el juego ha terminado, mostrar pantalla de Game Over
        if (isGameOver) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Game Over",
                    fontSize = 32.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Clase para definir obstáculos
data class Obstacle(
    var x: Int,
    val gapPosition: Int
)