package com.ugikpoenya.stickerwhatsapp.model

import android.os.Parcel
import android.os.Parcelable

class StickerPack : Parcelable {
    @JvmField
    val identifier: String?

    @JvmField
    var name: String?

    @JvmField
    var publisher: String?

    @JvmField
    val trayImageFile: String?

    @JvmField
    var trayImageUrl: String? = ""

    @JvmField
    val publisherEmail: String?

    @JvmField
    val publisherWebsite: String?

    @JvmField
    val privacyPolicyWebsite: String?

    @JvmField
    val licenseAgreementWebsite: String?

    @JvmField
    val imageDataVersion: String?

    @JvmField
    val avoidCache: Boolean

    @JvmField
    val animatedStickerPack: Boolean

    @JvmField
    var iosAppStoreLink: String? = null

    @JvmField
    var stickers: List<Sticker?>? = null

    @JvmField
    var totalSize: Long = 0

    @JvmField
    var androidPlayStoreLink: String? = null

    @JvmField
    var isWhitelisted = false

    @JvmField
    var createdAt = 0.toLong()

    constructor(identifier: String?, name: String?, publisher: String?, trayImageFile: String?, publisherEmail: String?, publisherWebsite: String?, privacyPolicyWebsite: String?, licenseAgreementWebsite: String?, imageDataVersion: String?, avoidCache: Boolean, animatedStickerPack: Boolean) {
        this.identifier = identifier
        this.name = name
        this.publisher = publisher
        this.trayImageFile = trayImageFile
        this.publisherEmail = publisherEmail
        this.publisherWebsite = publisherWebsite
        this.privacyPolicyWebsite = privacyPolicyWebsite
        this.licenseAgreementWebsite = licenseAgreementWebsite
        this.imageDataVersion = imageDataVersion
        this.avoidCache = avoidCache
        this.animatedStickerPack = animatedStickerPack
    }

    constructor(identifier: String?, name: String?, publisher: String?, trayImageFile: String?, trayImageUrl: String?, publisherEmail: String?, publisherWebsite: String?, privacyPolicyWebsite: String?, licenseAgreementWebsite: String?, imageDataVersion: String?, avoidCache: Boolean, animatedStickerPack: Boolean) {
        this.identifier = identifier
        this.name = name
        this.publisher = publisher
        this.trayImageFile = trayImageFile
        this.trayImageUrl = trayImageUrl
        this.publisherEmail = publisherEmail
        this.publisherWebsite = publisherWebsite
        this.privacyPolicyWebsite = privacyPolicyWebsite
        this.licenseAgreementWebsite = licenseAgreementWebsite
        this.imageDataVersion = imageDataVersion
        this.avoidCache = avoidCache
        this.animatedStickerPack = animatedStickerPack
    }

    private constructor(`in`: Parcel) {
        identifier = `in`.readString()
        name = `in`.readString()
        publisher = `in`.readString()
        trayImageFile = `in`.readString()
        trayImageUrl = `in`.readString()
        publisherEmail = `in`.readString()
        publisherWebsite = `in`.readString()
        privacyPolicyWebsite = `in`.readString()
        licenseAgreementWebsite = `in`.readString()
        iosAppStoreLink = `in`.readString()
        stickers = `in`.createTypedArrayList(Sticker.CREATOR)
        totalSize = `in`.readLong()
        androidPlayStoreLink = `in`.readString()
        isWhitelisted = `in`.readByte().toInt() != 0
        imageDataVersion = `in`.readString()
        avoidCache = `in`.readByte().toInt() != 0
        animatedStickerPack = `in`.readByte().toInt() != 0
    }

    fun setStickers(stickers: List<Sticker?>) {
        this.stickers = stickers
        totalSize = 0
        for (sticker in stickers) {
            totalSize += sticker!!.size
        }
    }

    fun setAndroidPlayStoreLink(androidPlayStoreLink: String?) {
        this.androidPlayStoreLink = androidPlayStoreLink
    }

    fun setIosAppStoreLink(iosAppStoreLink: String?) {
        this.iosAppStoreLink = iosAppStoreLink
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(identifier)
        dest.writeString(name)
        dest.writeString(publisher)
        dest.writeString(trayImageFile)
        dest.writeString(trayImageUrl)
        dest.writeString(publisherEmail)
        dest.writeString(publisherWebsite)
        dest.writeString(privacyPolicyWebsite)
        dest.writeString(licenseAgreementWebsite)
        dest.writeString(iosAppStoreLink)
        dest.writeTypedList(stickers)
        dest.writeLong(totalSize)
        dest.writeString(androidPlayStoreLink)
        dest.writeByte((if (isWhitelisted) 1 else 0).toByte())
        dest.writeString(imageDataVersion)
        dest.writeByte((if (avoidCache) 1 else 0).toByte())
        dest.writeByte((if (animatedStickerPack) 1 else 0).toByte())
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StickerPack?> = object : Parcelable.Creator<StickerPack?> {
            override fun createFromParcel(`in`: Parcel): StickerPack? {
                return StickerPack(`in`)
            }

            override fun newArray(size: Int): Array<StickerPack?> {
                return arrayOfNulls(size)
            }
        }
    }
}