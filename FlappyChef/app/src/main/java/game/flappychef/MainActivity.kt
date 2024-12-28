package game.flappychef

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameTheme {
                MainMenuScreen(
                    onPlayClick = { /* TODO: Navegar a pantalla de juego */ },
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
    val colors = lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF6200EE),
        secondary = androidx.compose.ui.graphics.Color(0xFF03DAC6)
    )

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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Título
        Text(
            text = "Mi Juego",
            fontSize = 36.sp,
            style = MaterialTheme.typography.headlineMedium
        )

        // Imagen central
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Cambia por tu imagen
            contentDescription = "Imagen del juego",
            modifier = Modifier.size(200.dp)
        )

        // Botones
        Button(onClick = onPlayClick, modifier = Modifier.padding(8.dp)) {
            Text(text = "Jugar")
        }

        Button(onClick = onInstructionsClick, modifier = Modifier.padding(8.dp)) {
            Text(text = "Instrucciones")
        }

        Button(onClick = onSettingsClick, modifier = Modifier.padding(8.dp)) {
            Text(text = "Ajustes")
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


