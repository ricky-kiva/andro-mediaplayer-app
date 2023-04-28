package com.rickyslash.mediaplayerapp

// this service allows player to keep playing without depending on Activity's lifecycle
interface MediaPlayerCallback {
    fun onPlay()
    fun onStop()
}