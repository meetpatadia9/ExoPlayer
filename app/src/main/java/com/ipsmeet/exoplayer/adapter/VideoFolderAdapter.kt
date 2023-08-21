package com.ipsmeet.exoplayer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ipsmeet.exoplayer.activity.VideoListActivity
import com.ipsmeet.exoplayer.databinding.RecyclerVideoFolderBinding
import com.ipsmeet.exoplayer.dataclass.MediaFileDataClass

class VideoFolderAdapter(private val context: Context, private val folderList: ArrayList<String>)
    : RecyclerView.Adapter<VideoFolderAdapter.FolderViewHolder>() {

    class FolderViewHolder(val itemBinding: RecyclerVideoFolderBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val itemBinding = RecyclerVideoFolderBinding.inflate(LayoutInflater.from(context), parent, false)
        return FolderViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return folderList.size
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.apply {
            itemBinding.folderName.text = folderList[position]

            itemBinding.imgVMenuMore.setOnClickListener {
                Toast.makeText(context, "more menu", Toast.LENGTH_SHORT).show()
            }
            
            itemView.setOnClickListener {
                context.startActivity(
                    Intent(context, VideoListActivity::class.java)
                        .putExtra("folderName", folderList[position])
                )
            }
        }
    }
}