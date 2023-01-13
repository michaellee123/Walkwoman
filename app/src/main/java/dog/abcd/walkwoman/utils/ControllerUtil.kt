package dog.abcd.walkwoman.utils

import dog.abcd.walkwoman.base.App
import dog.abcd.walkwoman.model.bean.Song

fun changePlaylist(list: List<Song>) {
    App.instance.controller?.changePlaylist(list)
}

fun nextPlay(song: Song) {
    App.instance.controller?.nextPlay(song)
}

fun shuffle(shuffle: Boolean) {
    App.instance.controller?.shuffle(shuffle)
}

fun repeatOne(repeatOne: Boolean) {
    App.instance.controller?.repeatOne(repeatOne)
}

fun pause() {
    App.instance.controller?.pause()
}

fun stop() {
    App.instance.controller?.stop()
}

fun start() {
    App.instance.controller?.start()
}

fun start(song: Song) {
    App.instance.controller?.start(song)
}

fun next() {
    App.instance.controller?.next()
}

fun previous() {
    App.instance.controller?.previous()
}

fun seek(msec: Int) {
    App.instance.controller?.seek(msec)
}

fun offTimer(millis: Int) {
    App.instance.controller?.offTimer(millis)
}

val isPlaying get() = App.instance.controller?.isPlaying ?: false

val playProgress get() = App.instance.controller?.progress ?: 0