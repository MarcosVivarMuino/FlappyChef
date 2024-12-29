package game.flappychef

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class GameOverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameTheme {
                GameOverScreen(
                    onBackToMainMenuClick = {
                        // Volver al menú principal
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Termina esta actividad para no regresar a ella
                    }
                )
            }
        }
    }
}

@Composable
fun GameOverScreen(onBackToMainMenuClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Texto de "Game Over"
            Text(
                text = "Game Over",
                fontSize = 36.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón para volver al menú principal
            Button(onClick = onBackToMainMenuClick) {
                Text(text = "Volver al Menú")
            }
        }
    }
}
