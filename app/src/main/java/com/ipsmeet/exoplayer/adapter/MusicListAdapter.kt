package com.ipsmeet.exoplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ipsmeet.exoplayer.R
import com.ipsmeet.exoplayer.databinding.RecyclerMusicFileBinding
import com.ipsmeet.exoplayer.dataclass.MusicDataClass

@UnstableApi class MusicListAdapter(val context: Context, private val musicList: List<MusicDataClass>, private val exoPlayer: ExoPlayer, private val listener: OnClick)
    :RecyclerView.Adapter<MusicListAdapter.MusicViewHolder>(){

    class MusicViewHolder(val itemBinding:RecyclerMusicFileBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val itemBinding = RecyclerMusicFileBinding.inflate(LayoutInflater.from(context), parent, false)
        return MusicViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.apply {
            with(musicList[position]) {
                if (this.isPlaying == true) {
                    itemBinding.fileName.apply {
                        setTextColor(ContextCompat.getColor(context, R.color.red))
                        setTypeface(null, Typeface.BOLD)
                    }
                    itemBinding.imgVMenuMore.setColorFilter(ContextCompat.getColor(context, R.color.red))
                } else {
                    itemBinding.fileName.apply {
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                        setTypeface(null, Typeface.NORMAL)
                        itemBinding.imgVMenuMore.setColorFilter(ContextCompat.getColor(context, R.color.white))
                    }
                }

                val albumArt = Uri.parse("content://media/external/audio/albumart/${ this.albumId!!.toLong() }")
                Glide.with(context).load(albumArt).placeholder(R.drawable.img_boy_listening).into(itemBinding.imgVThumbnail)

                itemBinding.fileName.text = this.displayName

                itemView.setOnClickListener {
                    itemBinding.fileName.apply {
                        setTextColor(ContextCompat.getColor(context, R.color.red))
                        setTypeface(null, Typeface.BOLD)
                    }
                    itemBinding.imgVMenuMore.setColorFilter(ContextCompat.getColor(context, R.color.red))
                    notifyDataSetChanged()

                    this.isPlaying = true
                    if (this.isPlaying == true) {
                        itemBinding.fileName.apply {
                            setTextColor(ContextCompat.getColor(context, R.color.red))
                            setTypeface(null, Typeface.BOLD)
                        }
                        itemBinding.imgVMenuMore.setColorFilter(ContextCompat.getColor(context, R.color.red))
                        notifyDataSetChanged()
                    }
                    else {
                        itemBinding.fileName.apply {
                            setTextColor(ContextCompat.getColor(context, R.color.white))
                            setTypeface(null, Typeface.NORMAL)
                        }
                        itemBinding.imgVMenuMore.setColorFilter(ContextCompat.getColor(context, R.color.white))
                        notifyDataSetChanged()
                    }

                    //  Start service
                    /**context.startService(
                        Intent(context.applicationContext, PlayerService::class.java)
                    )*/

                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                        exoPlayer.seekTo(position, 0)
                    }
                    exoPlayer.prepare()

                    listener.playMusic(musicList, position)
                }
            }
        }
    }

    interface OnClick {
        fun playMusic(musicList: List<MusicDataClass>, position: Int)
    }

}