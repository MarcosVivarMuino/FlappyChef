package game.flappychef

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import game.flappychef.ui.theme.FlappyChefTheme

class InstructionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        Text(
            text = "Instrucciones",
            fontSize = 38.sp,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )


        Text(
            text = "1. Inclina el móvil para que el personaje se mueva.\n" +
                    "2. Evita los obstáculos para no perder.\n" +
                    "3. Que no te pinchen los tenedores.\n" +
                    "4. Sopla al dispositivo para apagar el fuego si el personaje se incendia.\n" +
                    "5. Usa los potenciadores para ayudarte.",
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Potenciadores",
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "INMUNIDAD: Te hace invencible a las torres de platos por un tiempo.",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(8.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.powerup_invencible),
                contentDescription = "PowerUp de Inmunidad",
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(80.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "RALENTIZACIÓN: Hace que los enemigos 'tenedores' se muevan más despacio.",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(8.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.powerup_slow),
                contentDescription = "PowerUp de Ralentización",
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(80.dp),
                contentScale = ContentScale.Fit
            )

        }
        Button(onClick = onBackClick, modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Volver",
                fontSize = 28.sp,

                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
