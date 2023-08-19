package com.ugikpoenya.stickerwhatsapp.model

import android.os.Parcel
import android.os.Parcelable

class Sticker : Parcelable {
    @JvmField
    val imageFileName: String?

    @JvmField
    var imageFileUrl: String? = ""

    @JvmField
    val emojis: List<String>?

    @JvmField
    var size: Long = 0

    constructor(imageFileName: String?, emojis: List<String>?) {
        this.imageFileName = imageFileName
        this.emojis = emojis
    }

    constructor(imageFileName: String?, imageFileUrl: String?, emojis: List<String>?) {
        this.imageFileName = imageFileName
        this.imageFileUrl = imageFileUrl
        this.emojis = emojis
    }

    private constructor(`in`: Parcel) {
        imageFileName = `in`.readString()
        imageFileUrl = `in`.readString()
        emojis = `in`.createStringArrayList()
        size = `in`.readLong()
    }

    fun setSize(size: Long) {
        this.size = size
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(imageFileName)
        dest.writeString(imageFileUrl)
        dest.writeStringList(emojis)
        dest.writeLong(size)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Sticker?> = object : Parcelable.Creator<Sticker?> {
            override fun createFromParcel(`in`: Parcel): Sticker? {
                return Sticker(`in`)
            }

            override fun newArray(size: Int): Array<Sticker?> {
                return arrayOfNulls(size)
            }
        }
    }
}