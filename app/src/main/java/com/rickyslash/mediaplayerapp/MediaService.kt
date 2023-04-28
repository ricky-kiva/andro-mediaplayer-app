package com.rickyslash.mediaplayerapp

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import java.io.IOException
import java.lang.ref.WeakReference

// extending MediaPlayerCallback class and Service()
class MediaService : Service(), MediaPlayerCallback {

    private var mMediaPlayer: MediaPlayer? = null
    private var isReady: Boolean = false

    // get message from Messenger by IncomingHandler() function
    private val mMessenger = Messenger(IncomingHandler(this))

    // this enables MediaService to bind with MainActivity and send messages to it
    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind: ")
        // retrieve IBinder from mMessenger
        return mMessenger.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // get the intent action
        val action = intent?.action
        if (action != null) {
            when (action) {
                ACTION_CREATE -> if (mMediaPlayer == null) {
                    init()
                }
                ACTION_DESTROY -> if (mMediaPlayer?.isPlaying as Boolean) {
                    stopSelf() // stop the service
                }
                else -> {
                    init()
                }
            }
        }
        Log.d(TAG, "onStartCommand: ")
        return flags
    }

    // this class is used to handle messages that are sent to Messenger
    // Handler provides way to schedule & execute code in particular thread
    // Looper allows a thread to run `message loop` (receive message from queue & process them)
    internal class IncomingHandler(playerCallback: MediaPlayerCallback): Handler(Looper.getMainLooper()) {

        // using WeakReference prevents memory leaks in the app
        // it prevents class from holding `strong reference` of MediaPlayerCallback when it's no longer needed from the app
        private val mediaPlayerCallbackWeakReference: WeakReference<MediaPlayerCallback> = WeakReference(playerCallback)

        // this called whenever message is received by Messenger
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                PLAY -> mediaPlayerCallbackWeakReference.get()?.onPlay()
                STOP -> mediaPlayerCallbackWeakReference.get()?.onStop()
                else -> super.handleMessage(msg)
            }
        }
    }

    // overriding from MediaPlayerCallback class
    override fun onPlay() {
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

    // overriding from MediaPlayerCallback class
    override fun onStop() {
        // stop the player
        if (mMediaPlayer?.isPlaying as Boolean || isReady) {
            mMediaPlayer?.stop()
            isReady = false
        }
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

    companion object {
        const val ACTION_CREATE = "com.rickyslash.mediaplayerapp.mediaservice.create"
        const val ACTION_DESTROY = "com.rickyslash.mediaplayerapp.mediaservice.destroy"
        const val TAG = "MediaService"
        const val PLAY = 0
        const val STOP = 1
    }

}