package game.flappychef

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameTheme {
                MainMenuScreen(
                    onPlayClick = {
                        val intent = Intent(this, GameActivity::class.java)
                        startActivity(intent)
                    },
                    onInstructionsClick = {
                        val intent = Intent(this, InstructionsActivity::class.java)
                        startActivity(intent)
                    },
                    onSettingsClick = { /* TODO: Ir a ajustes */ }
                )
            }
        }
    }
}

@Composable
fun GameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFFFF5722),  // Naranja brillante
            secondary = Color(0xFF4CAF50), // Verde
            background = Color(0xFFF8E4A7), // Amarillo suave para el fondo
            surface = Color(0xFFFFFFFF),  // Blanco para los botones
            onPrimary = Color.White, // Texto blanco sobre el fondo naranja
            onSecondary = Color.White // Texto blanco sobre el verde
        ),
        typography = Typography(),
        content = content
    )
}

@Composable
fun MainMenuScreen(
    onPlayClick: () -> Unit,
    onInstructionsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título con estilo más divertido y colores llamativos
        Text(
            text = "Flappy Chef",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            style = TextStyle(letterSpacing = 3.sp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Imagen central relacionada con la temática de un cocinero
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Cambia por una imagen de un chef volador
            contentDescription = "Imagen del juego",
            modifier = Modifier
                .size(250.dp)
                .padding(16.dp) // Espacio alrededor de la imagen
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Botones con bordes redondeados y colores vivos
        Button(
            onClick = onPlayClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Jugar",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onInstructionsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Instrucciones",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSettingsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Ajustes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview() {
    GameTheme {
        MainMenuScreen(
            onPlayClick = {},
            onInstructionsClick = {},
            onSettingsClick = {}
        )
    }
}
