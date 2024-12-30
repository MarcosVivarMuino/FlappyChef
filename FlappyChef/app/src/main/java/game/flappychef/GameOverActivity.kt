package game.flappychef


import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class GameOverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            // Obtener la hora actual y determinar si es de día o de noche
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isDayTime = currentHour in 8..17
            Log.d("InstructionsActivity", "Current hour: $currentHour, Is it day time? $isDayTime")

            GameTheme(isDayTime = isDayTime){
                GameOverScreen(
                    onRetryClick = {
                        val intent = Intent(this, GameActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onExitClick = {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }

        }
    }
}

@Composable
fun GameOverScreen(onRetryClick: () -> Unit, onExitClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(text = "¡Game Over!", fontSize = 36.sp, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onRetryClick,
            modifier = Modifier

                .height(56.dp)
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Reintentar", color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = onExitClick,
            modifier = Modifier

                .height(56.dp)
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)) {
            Text("Menu principal", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}