package dog.abcd.walkwoman.model

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.jeremyliao.liveeventbus.LiveEventBus
import dog.abcd.fastmvp.IDisposableHandler
import dog.abcd.walkwoman.base.App
import dog.abcd.walkwoman.constant.EventKeys
import dog.abcd.walkwoman.model.bean.Album
import dog.abcd.walkwoman.model.bean.Song
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

object LocalMediaModel : IDisposableHandler {

    override var compositeDisposable: CompositeDisposable? = null

    val unknown = "<unknown>"

    val projection = arrayOf(
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.BITRATE,
        MediaStore.Audio.Media.DISC_NUMBER,
        MediaStore.Audio.Media.GENRE,
        MediaStore.Audio.Media.IS_FAVORITE,
        MediaStore.Audio.Media.NUM_TRACKS,
        MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
    )

    var selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

    val songs = ArrayList<Song>()

    val albums = ArrayList<Album>()

    fun refresh() {
        compositeDisposable?.dispose()
        compositeDisposable = null
        addDisposable(Single.create<Boolean> {
            refreshSongs()
            refreshAlbum()
            refreshPlaylist()
            it.onSuccess(true)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            LiveEventBus.get<List<Song>>(EventKeys.localSongs).post(songs)
            LiveEventBus.get<List<Album>>(EventKeys.localAlbums).post(albums)
        }, {
            it.printStackTrace()
        }))
    }

    private fun refreshSongs() {
        songs.clear()
        val resolver: ContentResolver = App.instance.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? =
            resolver.query(
                uri,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
            )
        when {
            cursor == null -> {
                // query failed, handle error.
            }
            !cursor.moveToFirst() -> {
                // no media on the device
            }
            else -> {
                val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val displayNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val bitrateColumn = cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE)
                val discNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISC_NUMBER)
                val genreColumn = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
                val isFavoriteColumn = cursor.getColumnIndex(MediaStore.Audio.Media.IS_FAVORITE)
                val numTracksColumn = cursor.getColumnIndex(MediaStore.Audio.Media.NUM_TRACKS)
                val bucketDisplayNameColumn =
                    cursor.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
                val cdTrackNumberColumn =
                    cursor.getColumnIndex(MediaStore.Audio.Media.CD_TRACK_NUMBER)
                do {
                    cursor.apply {
                        songs.add(
                            Song(
                                getLong(idColumn),
                                getString(artistColumn),
                                getString(titleColumn),
                                getString(dataColumn),
                                getString(displayNameColumn),
                                getLong(albumIdColumn),
                                getLong(durationColumn),
                                getLong(bitrateColumn),
                                getLong(discNumberColumn),
                                getStringOrNull(genreColumn) ?: unknown,
                                getInt(isFavoriteColumn) != 0,
                                getLong(numTracksColumn),
                                getString(bucketDisplayNameColumn),
                                getLongOrNull(cdTrackNumberColumn) ?: 0
                            )
                        )

                    }
                } while (cursor.moveToNext())
            }
        }
        cursor?.close()
    }

    private fun refreshAlbum() {
        albums.clear()
        val resolver: ContentResolver = App.instance.contentResolver
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val cursor: Cursor? =
            resolver.query(
                uri,
                null,
                null,
                null,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
            )
        when {
            cursor == null -> {
                // query failed, handle error.
            }
            !cursor.moveToFirst() -> {
                // no media on the device
            }
            else -> {
                val idColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ID)
                val albumColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM)
                val artistColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST)
                val artistIdColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST_ID)
                val firstYearColumn =
                    cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.FIRST_YEAR)
                val lastYearColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.LAST_YEAR)
                val numSongsColumn =
                    cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS)
                val numSongsForArtistColumn =
                    cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS_FOR_ARTIST)
                do {
                    cursor.apply {
                        albums.add(
                            Album(
                                getLong(idColumn),
                                getString(albumColumn),
                                getString(artistColumn),
                                getLong(artistIdColumn),
                                getLong(firstYearColumn),
                                getLong(lastYearColumn),
                                getLong(numSongsColumn),
                                getLong(numSongsForArtistColumn)
                            )
                        )
                    }
                } while (cursor.moveToNext())
            }
        }
        cursor?.close()
    }

    private fun refreshPlaylist() {
        //TODO 自己实现播放列表
    }

}