package com.ipsmeet.exoplayer.dataclass

import android.os.Parcel
import android.os.Parcelable

data class MusicDataClass(
    val id: String?,
    val title: String?,
    val albumId: String?,
    val displayName: String?,
    val size: String?,
    val duration: String?,
    val path: String?,
    val dateAdded: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(albumId)
        parcel.writeString(displayName)
        parcel.writeString(size)
        parcel.writeString(duration)
        parcel.writeString(path)
        parcel.writeString(dateAdded)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MusicDataClass> {
        override fun createFromParcel(parcel: Parcel): MusicDataClass {
            return MusicDataClass(parcel)
        }

        override fun newArray(size: Int): Array<MusicDataClass?> {
            return arrayOfNulls(size)
        }
    }
}

