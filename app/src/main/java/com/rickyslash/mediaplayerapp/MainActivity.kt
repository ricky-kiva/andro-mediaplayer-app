package com.rickyslash.mediaplayerapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var mBoundServiceIntent: Intent
    private var mService: Messenger? = null
    private var mServiceBound = false

    // call this on bind service
    private val mServiceConnection = object : ServiceConnection {
        // this will do when service is connected
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // setup Messenger to service (IBinder) for sending message to MediaService later
            mService = Messenger(service)
            // set flag
            mServiceBound = true
        }

        // this will do when service is disconnected
        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            // set flag
            mServiceBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPlay = findViewById<Button>(R.id.btn_play)
        val btnStop = findViewById<Button>(R.id.btn_stop)

        btnPlay.setOnClickListener {
            if (mServiceBound) {
                try {
                    // send message MediaService.PLAY
                    mService?.send(Message.obtain(null, MediaService.PLAY, 0, 0))
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }

        btnStop.setOnClickListener {
            if (mServiceBound) {
                try {
                    // send message MediaService.STOP
                    mService?.send(Message.obtain(null, MediaService.STOP, 0, 0))
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }

        // making Intent for the service
        mBoundServiceIntent = Intent(this@MainActivity, MediaService::class.java)
        mBoundServiceIntent.action = MediaService.ACTION_CREATE // add action for the intent (ACTION_CREATE)

        // starts the service
        startService(mBoundServiceIntent) // this ensuring the service continue to run in background even it has been unbind
        // bind the service
        bindService(mBoundServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        unbindService(mServiceConnection) // unbind the service
        mBoundServiceIntent.action = MediaService.ACTION_DESTROY // set action for the intent (ACTION_DESTROY)

        // ensure MediaService is still running
        startService(mBoundServiceIntent)
    }

    companion object {
        const val TAG = "MainActivity"
    }

}