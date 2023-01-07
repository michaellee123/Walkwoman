package dog.abcd.walkwoman.utils

import dog.abcd.walkwoman.model.bean.Song

interface PlaybackController {
    fun changePlaylist(list: List<Song>)
    fun nextPlay(song: Song)
    fun shuffle(shuffle: Boolean)
    fun pause()
    fun stop()
    fun start()
    fun start(song: Song)
    fun next()
    fun previous()
    fun seek(msec: Int)
    fun offTimer(millis: Int)
    val isPlaying: Boolean
    val progress: Int
}