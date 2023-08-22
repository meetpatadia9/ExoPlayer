package com.ipsmeet.exoplayer.dataclass

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

data class MusicDataClass(
    val id: String?,
    val title: String?,
    val albumId: String?,
    val displayName: String?,
    val size: String?,
    val duration: String?,
    val path: String?,
    val dateAdded: String?,
    var isPlaying: Boolean?
) : Parcelable {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readBoolean()
    ) {
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(albumId)
        parcel.writeString(displayName)
        parcel.writeString(size)
        parcel.writeString(duration)
        parcel.writeString(path)
        parcel.writeString(dateAdded)
        parcel.writeBoolean(isPlaying!!)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MusicDataClass> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): MusicDataClass {
            return MusicDataClass(parcel)
        }

        override fun newArray(size: Int): Array<MusicDataClass?> {
            return arrayOfNulls(size)
        }
    }
}

