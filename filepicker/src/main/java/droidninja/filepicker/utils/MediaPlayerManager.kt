package droidninja.filepicker.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class MediaPlayerManager {

    companion object {
        private var instance: MediaPlayerManager? = null
        private var mediaPlayer: MediaPlayer? = null

        fun getInstance(): MediaPlayerManager {
            if (instance == null) instance = MediaPlayerManager()
            return instance as MediaPlayerManager
        }
    }

    val isPlaying: Boolean?
        get() = if (mediaPlayer != null) {
            mediaPlayer?.isPlaying
        } else {
            false
        }


    fun play(context: Context, uriSound: Uri,
             completionListener: MediaPlayer.OnCompletionListener? = null,
             errorListener: MediaPlayer.OnErrorListener? = null) {
        if (mediaPlayer != null) {
            if (mediaPlayer?.isPlaying == true) mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
        mediaPlayer = MediaPlayer.create(context.applicationContext, uriSound)
        mediaPlayer?.setOnCompletionListener(completionListener)
        mediaPlayer?.setOnErrorListener(errorListener)
        mediaPlayer?.start()
    }

    fun stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer?.isPlaying == true) mediaPlayer?.stop()

            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

}