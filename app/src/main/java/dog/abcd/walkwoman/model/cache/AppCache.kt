package dog.abcd.walkwoman.model.cache

import dog.abcd.walkwoman.model.bean.Song

object AppCache {
    val lastPlaylist = Cache<List<Song>>("lastPlaylist", ArrayList())
    val repeatOne = Cache("repeatOne", false)
    val shuffle = Cache("shuffle", false)
}