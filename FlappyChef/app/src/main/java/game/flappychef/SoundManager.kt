package game.flappychef

import android.content.Context
import android.media.SoundPool
import android.media.MediaPlayer


class SoundManager(context: Context) {
    private val soundPool = SoundPool.Builder().setMaxStreams(5).build()
    private val ballSound = soundPool.load(context, R.raw.olla_fuego, 1)
    private val forkSound = soundPool.load(context, R.raw.tenedor, 1)
    private val towerSound = soundPool.load(context, R.raw.torre_platos, 1)
    private val powerUp1Sound = soundPool.load(context, R.raw.sonido_powerup, 1)
    private val powerUp2Sound = soundPool.load(context, R.raw.invencible, 1)


    fun playBallSound() {
        soundPool.play(ballSound, 0.5f, 0.5f, 0, 0, 1f)
    }

    fun playForkSound() {
        soundPool.play(forkSound, 0.5f, 0.5f, 0, 0, 1f)
    }

    fun playTowerSound() {
        soundPool.play(towerSound, 0.5f, 0.5f, 0, 0, 1f)
    }

    fun playPowerUp1Sound() {
        soundPool.play(powerUp1Sound, 0.5f, 0.5f, 0, 0, 1f)
    }

    fun playPowerUp2Sound() {
        soundPool.play(powerUp2Sound, 0.5f, 0.5f, 0, 0, 1f)
    }
}

class BackgroundMusic(context: Context) {
    private val mediaPlayer = MediaPlayer.create(context, R.raw.musica_fondo)

    fun start() {
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    fun stop() {
        mediaPlayer.stop()
    }

    fun release() {
        mediaPlayer.release()
    }
}
