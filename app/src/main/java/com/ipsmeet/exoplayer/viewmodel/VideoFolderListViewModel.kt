package com.ipsmeet.exoplayer.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import com.ipsmeet.exoplayer.dataclass.MediaFileDataClass
import java.io.File

class VideoFolderListViewModel: ViewModel() {

    @SuppressLint("Range")
    fun fetchMedia(context: Context, folderPath: ArrayList<String>): ArrayList<MediaFileDataClass> {
        val folderList = ArrayList<MediaFileDataClass>()
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)

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

                if (!folderPath.contains(File(path).parentFile!!.name)) {
                    folderPath.add(File(path).parentFile!!.name)
                }
                folderList.add(mediaData)
            }
        }
        return folderList
    }

}