package com.ipsmeet.exoplayer.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ipsmeet.exoplayer.activity.VideoPlayActivity
import com.ipsmeet.exoplayer.databinding.RecyclerVideoFileBinding
import com.ipsmeet.exoplayer.dataclass.MediaFileDataClass
import java.io.File
import java.util.concurrent.TimeUnit

class VideoFileAdapter(private val context: Context, private val videoList: ArrayList<MediaFileDataClass>)
    : RecyclerView.Adapter<VideoFileAdapter.FileViewHolder>() {

    class FileViewHolder(val itemBinding: RecyclerVideoFileBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val itemBinding = RecyclerVideoFileBinding.inflate(LayoutInflater.from(context), parent, false)
        return FileViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.apply {
            with(videoList[position]) {
                Glide.with(context).load(File(this.path!!)).into(itemBinding.imgVThumbnail)

                itemBinding.fileName.text = this.displayName
                itemBinding.txtVideoDuration.text = displayTime(this.duration!!.toDouble())

                val bundle = Bundle()
                bundle.putParcelableArrayList("videoPlayList", videoList)
                itemView.setOnClickListener {

                    context.startActivity(
                        Intent(context, VideoPlayActivity::class.java)
                            .putExtra("position", position)
                            .putExtra("displayName", this.displayName)
                            .putExtra("bundle",bundle)
                    )
                }
            }
        }
    }

    private fun displayTime(millisecond: Double): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millisecond.toLong())
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisecond.toLong()) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisecond.toLong()))
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisecond.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisecond.toLong()))

        Log.i("hours", hours.toString())
        Log.i("minutes", minutes.toString())
        Log.i("seconds", seconds.toString())

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
