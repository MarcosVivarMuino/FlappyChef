package game.flappychef


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
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
            GameOverScreen(
                onRetryClick = {
                    val intent = Intent(this, GameActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onExitClick = {
                    finishAffinity() // Cierra toda la aplicación
                }
            )
        }
    }
}

@Composable
fun GameOverScreen(onRetryClick: () -> Unit, onExitClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "¡Game Over!", fontSize = 36.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onRetryClick) {
            Text("Reintentar")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = onExitClick) {
            Text("Salir")
        }
    }
}