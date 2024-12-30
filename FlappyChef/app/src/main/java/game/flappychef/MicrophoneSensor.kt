package game.flappychef

import android.media.MediaRecorder
import android.util.Log

// Clase que maneja la grabación del micrófono
class MicrophoneSensor {
    private var mediaRecorder: MediaRecorder? = null
    private val threshold = 2000 // Umbral de sonido para considerar como un soplido

    fun startListening() {
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("/dev/null") // No guardamos el archivo
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("MicrophoneSensor", "Error al iniciar el micrófono", e)
        }
    }

    fun stopListening() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("MicrophoneSensor", "Error al detener el micrófono", e)
        }
    }

    fun isBlown(): Boolean {
        val amplitude = mediaRecorder?.maxAmplitude ?: 0
        return amplitude > threshold
    }
}