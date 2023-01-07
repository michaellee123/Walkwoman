package dog.abcd.walkwoman.model.bean

import android.content.ContentUris
import android.net.Uri
import androidx.core.net.toUri
import java.io.Serializable

data class Album(
    val albumId: Long,
    val album: String,
    val artist: String,
    val artistId: Long,
    val firstYear: Long,
    val lastYear: Long,
    val numSongs: Long,
    val numSongsByArtist: Long
) : Serializable {
    companion object {
        val sArtworkUri = "content://media/external/audio/albumart".toUri()
    }

    val albumArt: Uri get() = ContentUris.withAppendedId(sArtworkUri, albumId)
}
