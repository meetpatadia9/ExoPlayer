package com.ipsmeet.exoplayer.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import com.ipsmeet.exoplayer.dataclass.MediaFileDataClass

class VideoListViewModel: ViewModel() {

    @SuppressLint("Range")
    fun fetchMedia(context: Context, folderName: String): ArrayList<MediaFileDataClass> {
        val videoFiles = arrayListOf<MediaFileDataClass>()
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val selection = "${ MediaStore.Video.Media.DATA } LIKE ?"
        val cursor = context.contentResolver.query(uri, null, selection, arrayOf("%/$folderName/%"), null)

        cursor?.use {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                val displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
                val size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                val duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                val dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))

                val mediaData = MediaFileDataClass(id, title, displayName, size, duration, path, dateAdded)
                videoFiles.add(mediaData)
            }
        }
        return videoFiles
    }

}