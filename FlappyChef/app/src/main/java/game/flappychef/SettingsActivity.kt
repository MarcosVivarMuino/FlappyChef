package game.flappychef

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.media.AudioManager
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isDayTime = currentHour in 8..17 // Es de día si la hora está entre las 8 AM y las 5 PM

        setContent {
            GameTheme(isDayTime = isDayTime) {
                SettingsScreen(onBackClick = { finish() })
            }
        }
    }
}

@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    val audioManager = context.getSystemService(AudioManager::class.java)

    var volume by remember { mutableStateOf(prefs.getFloat("volume", audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) /
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat())) }

    var profileImagePath by remember { mutableStateOf(prefs.getString("profile_image", null)) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val playerWidth = 100
            val playerHeight = 100
            val resizedBitmap = Bitmap.createScaledBitmap(it, playerWidth, playerHeight, true)
            val file = File(context.filesDir, "profile_image.png")

            try {
                FileOutputStream(file).use { fos ->
                    resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                prefs.edit().putString("profile_image", file.absolutePath).apply()
                profileImagePath = file.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Ajustes",
            fontSize = 38.sp,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineMedium
        )

        profileImagePath?.let {
            val bitmap = BitmapFactory.decodeFile(it)
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Perfil", modifier = Modifier.size(100.dp))
        } ?: Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Perfil",
            modifier = Modifier.size(100.dp).padding(8.dp)
        )

        Button(onClick = { launcher.launch() }, modifier = Modifier.padding(8.dp)) {
            Text(text = "Cambiar Foto de Perfil",
                fontSize = 28.sp
            )
        }

        Text(text = "Volumen",
            fontSize = 28.sp,
            modifier = Modifier.padding(8.dp),
            color = MaterialTheme.colorScheme.onBackground)

        Slider(
            value = volume,
            onValueChange = {
                volume = it
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    (it * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)).toInt(),
                    0
                )
                prefs.edit().putFloat("volume", it).apply()
            },
            valueRange = 0f..1f,
            modifier = Modifier.padding(8.dp)
        )

        Button(onClick = onBackClick, modifier = Modifier.padding(8.dp)) {
            Text(text = "Volver",
                fontSize = 28.sp
            )
        }
    }
}
