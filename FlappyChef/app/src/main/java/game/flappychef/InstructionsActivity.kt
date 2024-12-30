package game.flappychef

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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

        // Obtener la hora actual y determinar si es de día o de noche
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isDayTime = currentHour in 8..17
        Log.d("InstructionsActivity", "Current hour: $currentHour, Is it day time? $isDayTime")

        setContent {
            GameTheme(isDayTime = isDayTime) {
                InstructionsScreen(onBackClick = { finish() })
            }
        }
    }
}

@Composable
fun InstructionsScreen(onBackClick: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Título con el color adecuado según el tema
        Text(
            text = "Instrucciones",
            fontSize = 28.sp,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        // Texto de las instrucciones con el color adecuado según el tema
        Text(
            text = "1. Toca la pantalla para que el personaje vuele.\n" +
                    "2. Evita los obstáculos para no perder.\n" +
                    "3. Sopla al dispositivo para apagar el fuego si el personaje se incendia.\n" +
                    "4. Agita el dispositivo para usar los Power Ups que recojas.",
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onBackground // Texto blanco si es de noche, negro si es de día
        )

        // Botón "Volver" con color adecuado según el tema
        Button(onClick = onBackClick, modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Volver",
                color = MaterialTheme.colorScheme.onPrimary // Texto negro si es de día, blanco si es de noche
            )
        }
    }
}
