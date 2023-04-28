package com.rickyslash.mediaplayerapp

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var mMediaPlayer: MediaPlayer? = null
    private var isReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPlay = findViewById<Button>(R.id.btn_play)
        val btnStop = findViewById<Button>(R.id.btn_stop)

        btnPlay.setOnClickListener {
            if (!isReady) {
                // prepare the player for playback
                mMediaPlayer?.prepareAsync()
            } else {
                // sets the logic for start button
                if (mMediaPlayer?.isPlaying as Boolean) {
                    mMediaPlayer?.pause()
                } else {
                    mMediaPlayer?.start()
                }
            }
        }

        btnStop.setOnClickListener {
            // stop the player
            if (mMediaPlayer?.isPlaying as Boolean || isReady) {
                mMediaPlayer?.stop()
                isReady = false
            }
        }

        init()
    }

    private fun init() {
        // instantiating MediaPlayer
        mMediaPlayer = MediaPlayer()
        val attribute = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA) // set usage of the audio (as Media playback)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // set content type of audio (Sonification is for short audio clips)
            .build()
        mMediaPlayer?.setAudioAttributes(attribute) // pass attributes to MediaPlayer

        // get the descriptor for the media
        // `descriptor` is unique integer to identify open file within a process
        val afd = applicationContext.resources.openRawResourceFd(R.raw.pink_floyd_high_hopes)
        try {
            // set the datasource for the MediaPlayer
            mMediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // this function is called when MediaPlayer is prepared to play the media
        mMediaPlayer?.setOnPreparedListener {
            isReady = true
            // starts the MediaPlayer playback
            mMediaPlayer?.start()
        }

        // this called when MediaPlayer gets error while playing the media
        mMediaPlayer?.setOnErrorListener { _, _, _ -> false }
    }

}