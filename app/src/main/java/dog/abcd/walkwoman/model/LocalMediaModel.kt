package dog.abcd.walkwoman.model

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import com.jeremyliao.liveeventbus.LiveEventBus
import com.orhanobut.logger.Logger
import dog.abcd.fastmvp.IDisposableHandler
import dog.abcd.walkwoman.base.App
import dog.abcd.walkwoman.constant.EventKeys
import dog.abcd.walkwoman.model.bean.Song
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

object LocalMediaModel : IDisposableHandler {

    override var compositeDisposable: CompositeDisposable? = null

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION
    )

    var selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

    val songs = ArrayList<Song>()

    fun refresh() {
        compositeDisposable?.dispose()
        compositeDisposable = null
        addDisposable(Single.create<Boolean> {
            refreshSongs()
            refreshPlaylist()
            it.onSuccess(true)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            LiveEventBus.get<List<Song>>(EventKeys.localSongs).post(songs)
        }, {
            it.printStackTrace()
        }))
    }

    private fun refreshSongs() {
        val resolver: ContentResolver = App.instance.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? =
            resolver.query(uri, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER)
        when {
            cursor == null -> {
                // query failed, handle error.
            }
            !cursor.moveToFirst() -> {
                // no media on the device
            }
            else -> {
                val titleColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
                val idColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
                val artistColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
                val dataColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA)
                val displayNameColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DISPLAY_NAME)
                val albumIdColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DURATION)
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
                                getLong(durationColumn)
                            )
                        )

                    }
                } while (cursor.moveToNext())
            }
        }
        cursor?.close()
    }

    private fun refreshPlaylist() {
        val resolver: ContentResolver = App.instance.contentResolver
        val uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
        val cursor: Cursor? =
            resolver.query(uri, null, null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER)
        when {
            cursor == null -> {
                // query failed, handle error.
            }
            !cursor.moveToFirst() -> {
                // no media on the device
            }
            else -> {
                do {
                    cursor.apply {
                        val bundle = cursor.extras
                        Logger.e(bundle.keySet().firstOrNull() ?: "null")
                    }
                } while (cursor.moveToNext())
            }
        }
        cursor?.close()
    }

}