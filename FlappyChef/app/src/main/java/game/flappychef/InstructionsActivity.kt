package game.flappychef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import game.flappychef.ui.theme.FlappyChefTheme

class InstructionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameTheme {
                InstructionsScreen(onBackClick = { finish() })
            }
        }
    }
}

@Composable
fun InstructionsScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Instrucciones",
            fontSize = 28.sp,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "1. Toca la pantalla para que el personaje vuele.\n" +
                    "2. Evita los obstáculos para no perder.\n" +
                    "3. Sopla al dispositivo para apagar el fuego si el personaje se incendia.\n" +
                    "4. Agita el dispositivo para usar los Power Ups que recojas.",
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )

        Button(onClick = onBackClick, modifier = Modifier.padding(8.dp)) {
            Text(text = "Volver")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InstructionsScreenPreview() {
    GameTheme {
        InstructionsScreen(onBackClick = {})
    }
}