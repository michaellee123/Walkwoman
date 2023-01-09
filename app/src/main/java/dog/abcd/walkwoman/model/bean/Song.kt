package dog.abcd.walkwoman.model.bean

import android.content.ContentUris
import android.net.Uri
import androidx.core.net.toUri

data class Song(
    val id: Long,
    val artist: String,
    val title: String,
    val data: String,
    val displayName: String,
    val albumId: Long,
    val duration: Long,
    val bitrate: Long,
    val discNumber: Long,
    val genre: String,
    val isFavorite: Boolean,
    val numTracks: Long,
    val bucketDisplayName: String,
    val cdTrackNumber: Long,
    val track:Long
) {
    companion object {
        val sArtworkUri = "content://media/external/audio/albumart".toUri()
    }

    val albumArt: Uri get() = ContentUris.withAppendedId(sArtworkUri, albumId)

    override fun toString(): String {
        return "Song(id=$id, artist='$artist', title='$title', data='$data', displayName='$displayName', albumId=$albumId, duration=$duration, bitrate=$bitrate, discNumber=$discNumber, genre='$genre', isFavorite=$isFavorite, numTracks=$numTracks, bucketDisplayName='$bucketDisplayName', cdTrackNumber=$cdTrackNumber ,track=$track)"
    }


}