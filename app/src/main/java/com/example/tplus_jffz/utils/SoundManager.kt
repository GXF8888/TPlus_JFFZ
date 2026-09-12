package com.example.tplus_jffz.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.tplus_jffz.R

object SoundManager {
    private var successPlayer: MediaPlayer? = null
    private var errorPlayer: MediaPlayer? = null

    fun init(context: Context) {
        successPlayer = MediaPlayer.create(context, R.raw.succeed)
        errorPlayer = MediaPlayer.create(context, R.raw.errar)
    }

    fun playSuccess() {
        successPlayer?.start()
    }

    fun playError() {
        errorPlayer?.start()
    }

    fun release() {
        successPlayer?.release()
        errorPlayer?.release()
        successPlayer = null
        errorPlayer = null
    }
}
