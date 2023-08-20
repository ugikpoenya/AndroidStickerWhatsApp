package com.ugikpoenya.stickerwhatsapp.model

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import java.util.Objects

class StickerCursor {

    fun getStickersForAStickerPack(context: Context, uri: Uri): Cursor {
        val identifier = uri.lastPathSegment
        Log.d("LOG", "Cursor getStickersForAStickerPack " + identifier)
        val stickerPack = StickerBook().getStickerPack(context, identifier)
        val cursor = MatrixCursor(arrayOf(StickerBook.STICKER_FILE_NAME_IN_QUERY, StickerBook.STICKER_FILE_EMOJI_IN_QUERY))
        for (sticker in stickerPack?.stickers!!) {
            cursor.addRow(arrayOf<Any?>(sticker!!.imageFileName, TextUtils.join(",", sticker.emojis!!)))
        }
        cursor.setNotificationUri(Objects.requireNonNull(context)!!.contentResolver, uri)
        return cursor
    }

    fun getCursorForSingleStickerPack(context: Context, uri: Uri, stickerPackList: List<StickerPack>): Cursor {
        val identifier = uri.lastPathSegment
        Log.d("LOG", "Cursor getCursorForSingleStickerPack " + identifier)
        for (stickerPack in stickerPackList) {
            if (identifier == stickerPack.identifier) {
                return getStickerPackInfo(context, uri, listOf(stickerPack))
            }
        }
        return getStickerPackInfo(context, uri, ArrayList())
    }

    fun getStickerPackInfo(context: Context, uri: Uri, stickerPackList: List<StickerPack>): Cursor {
        Log.d("LOG", "Cursor getStickerPackInfo ")
        val cursor = MatrixCursor(
            arrayOf(
                StickerBook.STICKER_PACK_IDENTIFIER_IN_QUERY,
                StickerBook.STICKER_PACK_NAME_IN_QUERY,
                StickerBook.STICKER_PACK_PUBLISHER_IN_QUERY,
                StickerBook.STICKER_PACK_ICON_IN_QUERY,
                StickerBook.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
                StickerBook.IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                StickerBook.PUBLISHER_EMAIL,
                StickerBook.PUBLISHER_WEBSITE,
                StickerBook.PRIVACY_POLICY_WEBSITE,
                StickerBook.LICENSE_AGREENMENT_WEBSITE,
                StickerBook.IMAGE_DATA_VERSION,
                StickerBook.AVOID_CACHE,
                StickerBook.ANIMATED_STICKER_PACK
            )
        )

        for (stickerPack in stickerPackList) {
            val builder = cursor.newRow()
            builder.add(stickerPack.identifier)
            builder.add(stickerPack.name)
            builder.add(stickerPack.publisher)
            builder.add(stickerPack.trayImageFile)
            builder.add(stickerPack.androidPlayStoreLink)
            builder.add(stickerPack.iosAppStoreLink)
            builder.add(stickerPack.publisherEmail)
            builder.add(stickerPack.publisherWebsite)
            builder.add(stickerPack.privacyPolicyWebsite)
            builder.add(stickerPack.licenseAgreementWebsite)
            builder.add(stickerPack.imageDataVersion)
            builder.add(if (stickerPack.avoidCache) 1 else 0)
            builder.add(if (stickerPack.animatedStickerPack) 1 else 0)
        }
        cursor.setNotificationUri(Objects.requireNonNull(context)!!.contentResolver, uri)
        return cursor
    }
}