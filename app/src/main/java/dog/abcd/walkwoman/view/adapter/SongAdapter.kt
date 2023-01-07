package dog.abcd.walkwoman.view.adapter

import android.view.View
import com.bumptech.glide.Glide
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.base.QuickAdapter
import dog.abcd.walkwoman.base.ViewBindingHolder
import dog.abcd.walkwoman.databinding.ItemSongBinding
import dog.abcd.walkwoman.model.bean.Song

class SongAdapter : QuickAdapter<Song, ItemSongBinding>() {
    override fun convert(holder: ViewBindingHolder<ItemSongBinding>, item: Song) {
        holder.bind.tvTitle.text = item.title
        holder.bind.tvArtist.text = item.artist

        holder.bind.hires.visibility = if (item.bitrate > 960000) View.VISIBLE else View.GONE

        Glide.with(holder.bind.ivAlbum)
            .load(item.albumArt)
            .placeholder(R.mipmap.default_audio_art)
            .error(R.mipmap.default_audio_art)
            .into(holder.bind.ivAlbum)
    }
}