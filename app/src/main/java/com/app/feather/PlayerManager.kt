import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.widget.SeekBar
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.NavHostFragment
import com.app.feather.AudioPlayerService
import com.app.feather.R
import com.app.feather.databinding.FragmentPlayerBinding
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PlayerManager(
    private val context: Context, val binding: FragmentPlayerBinding,
    val activity: FragmentActivity?
) {
    val player: SimpleExoPlayer = SimpleExoPlayer.Builder(context)
        .setLoadControl(DefaultLoadControl())
        .build()

    private var currentUrl: String? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: PlayerManager? = null

        fun getInstance(
            context: Context, binding: FragmentPlayerBinding,
            activity: FragmentActivity?
        ): PlayerManager {
            return instance ?: synchronized(this) {
                instance ?: PlayerManager(context, binding, activity).also { instance = it }
            }
        }

        fun getInstance() = instance
    }

    fun isTrackLoaded() = player.playbackState != Player.STATE_IDLE
            && player.playbackState != Player.STATE_ENDED

    init {
        // Привязка кнопок и SeekBar к соответствующим элементам из binding
        binding.managerSongButton.setOnClickListener {
            togglePlayback()
        }

        binding.miniPlayerManageButton.setOnClickListener {
            togglePlayback()
        }

        binding.previousSongButton.setOnClickListener {
            playPreviousSong()
        }

        binding.nextSongButton.setOnClickListener {
            playNextSong()
        }

        binding.songProgressBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Перемотка трека при изменении положения SeekBar пользователем
                val duration = player.duration
                val position = (progress * duration) / 100
                if (fromUser) seekTo(position)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Начало перемещения ползунка SeekBar пользователем
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Окончание перемещения ползунка SeekBar пользователем
            }
        })

        // Кнопка поделиться
        binding.sharedSongButton.setOnClickListener {
            // Логика обработки нажатия кнопки поделиться
        }

        // Кнопка повторения трека
        binding.repeatSongButton.setOnClickListener {
            // Логика обработки нажатия кнопки повторения трека
        }

        val handler = Handler()

// Создайте Runnable для обновления прогресса
        val updateProgressRunnable = object : Runnable {
            override fun run() {
                val currentPosition =
                    player.currentPosition // Получите текущую позицию проигрывателя в миллисекундах
                val totalDuration =
                    player.duration // Получите общую продолжительность трека в миллисекундах

                val maxValue =
                    binding.songProgressBar.max // Получите максимальное значение ползунка SeekBar
                val progress =
                    (currentPosition.toFloat() / totalDuration.toFloat() * maxValue.toFloat()) // Вычислите прогресс для ползунка SeekBar


                binding.songProgressBar.progress =
                    progress.toInt() // Обновите прогресс ползунка SeekBar

                // Повторно запустите Runnable через определенный интервал времени (например, каждую секунду)
                handler.post(this)
            }
        }

// Запустите Runnable
        handler.post(updateProgressRunnable)
    }

    fun setMediaUrl(mediaUrl: String) {
        if (mediaUrl == currentUrl) {
            return
        } else currentUrl = mediaUrl

        val dataSourceFactory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, "Feather")
        )
        val mediaItem = MediaItem.fromUri(mediaUrl)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        player.setMediaSource(mediaSourceFactory.createMediaSource(mediaItem))
        player.prepare()
    }

    fun play() {
        player.play()

        context.startService(
            Intent(context, AudioPlayerService::class.java)
        )
    }

    fun updatePlaybackIcon() {
        val playbackIconResId = if (player.isPlaying) {
            R.drawable.pause_icon // Иконка паузы
        } else {
            R.drawable.play_icon // Иконка воспроизведения
        }
        binding.managerSongButton.setIconResource(playbackIconResId)
        binding.miniPlayerManageButton.setIconResource(playbackIconResId)
        activity
            ?.findViewById<FragmentContainerView>(
                R.id.applicationNavigationFragmentContainerView
            )?.getFragment<NavHostFragment>()
            ?.view?.findViewById<FloatingActionButton>(R.id.playlistSongsButton)
            ?.setImageResource(playbackIconResId)

            AudioPlayerService.getInstance()?.buildNotification(playbackIconResId)?.let {
                AudioPlayerService.getInstance()?.updateService(context, it)
            }
    }

    fun pause() {
        player.pause()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun release() {
        player.release()
    }

    fun togglePlayback() {
        if (player.isPlaying) {
            pause()
        } else {
            play()
        }

        updatePlaybackIcon()
    }

    private fun playPreviousSong() {
        // Логика воспроизведения предыдущего трека
    }

    private fun playNextSong() {
        // Логика воспроизведения следующего трека
    }
}
