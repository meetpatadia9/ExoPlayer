package com.ipsmeet.exoplayer.dataclass

import android.os.Parcel
import android.os.Parcelable

data class MediaFileDataClass(
    val id: String?,
    val title: String?,
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
        parcel.readString()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(title)
        dest.writeString(displayName)
        dest.writeString(size)
        dest.writeString(duration)
        dest.writeString(path)
        dest.writeString(dateAdded)
    }

    companion object CREATOR : Parcelable.Creator<MediaFileDataClass> {
        override fun createFromParcel(parcel: Parcel): MediaFileDataClass {
            return MediaFileDataClass(parcel)
        }

        override fun newArray(size: Int): Array<MediaFileDataClass?> {
            return arrayOfNulls(size)
        }
    }
}
