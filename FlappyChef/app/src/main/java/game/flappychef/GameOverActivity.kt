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
        val finalScore = intent.getIntExtra("finalScore", 0)

        setContent {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isDayTime = currentHour in 8..17
            Log.d("InstructionsActivity", "Current hour: $currentHour, Is it day time? $isDayTime")

            GameTheme(isDayTime = isDayTime){
                GameOverScreen(
                    finalScore,
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
fun GameOverScreen(finalScore: Int, onRetryClick: () -> Unit, onExitClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Game Over!",
            fontSize = 38.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Puntuación Final: $finalScore",
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = onRetryClick,
            modifier = Modifier
                .padding(8.dp)
                .height(56.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Reintentar",
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = onExitClick,
            modifier = Modifier
                .padding(8.dp)
                .height(56.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Menu principal",
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onSecondary)
        }
    }
}