package game.flappychef

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

        // Obtener la hora actual y determinar si es de día o de noche
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isDayTime = currentHour in 8..17 // Es de día si la hora está entre las 8 AM y las 5 PM
        Log.d("InstructionsActivity", "Current hour: $currentHour, Is it day time? $isDayTime")

        setContent {
            GameTheme(isDayTime = isDayTime) {
                MainMenuScreen(
                    onPlayClick = {
                        val intent = Intent(this, GameActivity::class.java)
                        startActivity(intent)
                    },
                    onInstructionsClick = {
                        val intent = Intent(this, InstructionsActivity::class.java)
                        startActivity(intent)
                    },
                    onSettingsClick = {
                        val intent = Intent(this, SettingsActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun GameTheme(isDayTime: Boolean, content: @Composable () -> Unit) {
    val colors = if (isDayTime) {
        // Colores para el modo día
        lightColorScheme(
            primary = Color(0xFFFF5722),  // Naranja brillante
            secondary = Color(0xFF4CAF50), // Verde
            background = Color(0xFFABABAB), // Gris claro para el fondo de día
            surface = Color(0xFFFFFFFF),  // Blanco para los botones
            onPrimary = Color.White, // Texto negro sobre el fondo naranja
            onSecondary = Color.White, // Texto negro sobre el verde
            onBackground = Color.White, // Texto negro sobre fondo claro
            onSurface = Color.White // Texto negro sobre superficie blanca
        )
    } else {
        // Colores para el modo noche
        darkColorScheme(
            primary = Color(0xFFBB86FC),  // Púrpura claro
            secondary = Color(0xFF03DAC6), // Verde agua
            background = Color(0xFF121212), // Gris oscuro para el fondo de noche
            surface = Color(0xFF121212),  // Fondo oscuro
            onPrimary = Color.White, // Texto blanco sobre el púrpura
            onSecondary = Color.White, // Texto blanco sobre el verde agua
            onBackground = Color.White, // Texto blanco sobre fondo oscuro
            onSurface = Color.White // Texto blanco sobre superficie oscura
        )
    }

    MaterialTheme(
        colorScheme = colors,
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
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,

        ) {
        // Título con el color adecuado según el tema
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
                color = MaterialTheme.colorScheme.onPrimary // El texto se ajusta al tema
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
                color = MaterialTheme.colorScheme.onSecondary // El texto se ajusta al tema
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
                color = MaterialTheme.colorScheme.onSecondary // El texto se ajusta al tema
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview() {
    GameTheme(isDayTime = true) {
        MainMenuScreen(
            onPlayClick = {},
            onInstructionsClick = {},
            onSettingsClick = {}
        )
    }
}


