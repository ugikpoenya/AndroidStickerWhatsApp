package com.ugikpoenya.sampleappstickerwhatsapp

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.util.Log
import com.ugikpoenya.stickerwhatsapp.model.StickerBook
import com.ugikpoenya.stickerwhatsapp.model.StickerCursor
import java.io.File
import java.io.IOException
import java.util.Objects

/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */   class StickerContentProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val authority = context?.packageName + ".stickercontentprovider"
        check(authority.startsWith(Objects.requireNonNull(context)!!.packageName)) { "your authority (" + authority + ") for the content provider should start with your package name: " + context!!.packageName }

        //the call to get the metadata for the sticker packs.
        MATCHER.addURI(authority, METADATA, METADATA_CODE)

        //the call to get the metadata for single sticker pack. * represent the identifier
        MATCHER.addURI(authority, METADATA + "/*", METADATA_CODE_FOR_SINGLE_PACK)

        //gets the list of stickers for a sticker pack, * respresent the identifier.
        MATCHER.addURI(authority, STICKERS + "/*", STICKERS_CODE)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val code = MATCHER.match(uri)
        Log.d("LOG", "ContentProvider query " + code)

        val stickerPackList = StickerBook().getStickerPackList(context!!)

        val authority = context?.packageName + ".stickercontentprovider"
        for (stickerPack in stickerPackList) {
            MATCHER.addURI(authority, STICKERS_ASSET + "/" + stickerPack.identifier + "/" + stickerPack.trayImageFile, STICKER_PACK_TRAY_ICON_CODE)
            for (sticker in stickerPack.stickers!!) {
                MATCHER.addURI(authority, STICKERS_ASSET + "/" + stickerPack.identifier + "/" + sticker!!.imageFileName, STICKERS_ASSET_CODE)
            }
        }

        return when (code) {
            METADATA_CODE -> {
                StickerCursor().getStickerPackInfo(context!!, uri, stickerPackList)
            }

            METADATA_CODE_FOR_SINGLE_PACK -> {
                StickerCursor().getCursorForSingleStickerPack(context!!, uri, stickerPackList)
            }

            STICKERS_CODE -> {
                StickerCursor().getStickersForAStickerPack(context!!, uri)
            }

            else -> {
                throw IllegalArgumentException("Unknown URI: $uri")
            }
        }
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val matchCode = MATCHER.match(uri)
        return if (matchCode == STICKERS_ASSET_CODE || matchCode == STICKER_PACK_TRAY_ICON_CODE) {
            getImageAsset(uri)
        } else null
    }

    override fun getType(uri: Uri): String? {
        val matchCode = MATCHER.match(uri)
        return when (matchCode) {
            METADATA_CODE -> "vnd.android.cursor.dir/vnd." + context?.packageName + ".stickercontentprovider." + METADATA
            METADATA_CODE_FOR_SINGLE_PACK -> "vnd.android.cursor.item/vnd." + context?.packageName + ".stickercontentprovider." + METADATA
            STICKERS_CODE -> "vnd.android.cursor.dir/vnd." + context?.packageName + ".stickercontentprovider." + STICKERS
            STICKERS_ASSET_CODE -> "image/webp"
            STICKER_PACK_TRAY_ICON_CODE -> "image/png"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun getImageAsset(uri: Uri): AssetFileDescriptor? {
        val am = Objects.requireNonNull(context)!!.assets
        val pathSegments = uri.pathSegments
        require(pathSegments.size == 3) { "path segments should be 3, uri is: $uri" }
        val fileName = pathSegments[pathSegments.size - 1]
        val identifier = pathSegments[pathSegments.size - 2]
        require(!TextUtils.isEmpty(identifier)) { "identifier is empty, uri: $uri" }
        require(!TextUtils.isEmpty(fileName)) { "file name is empty, uri: $uri" }
        return fetchFile(uri, am, fileName, identifier)
    }

    private fun fetchFile(uri: Uri, am: AssetManager, fileName: String, identifier: String): AssetFileDescriptor? {
        return try {
            val cacheFile = context!!.getExternalFilesDir(identifier)
            val file = File(cacheFile, fileName)
            //The code above is basically copying the assets to storage, and servering the file off of the storage.
            //If you have the files already downloaded/fetched, you could simply replace above part, and initialize the file parameter with your own file which points to the desired file.
            //The key here is you can use ParcelFileDescriptor to create an AssetFileDescriptor.
            AssetFileDescriptor(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY), 0, AssetFileDescriptor.UNKNOWN_LENGTH)
        } catch (e: IOException) {
            Log.d("LOG", "fetchFile Error " + e.message)
            Log.e(Objects.requireNonNull(context)!!.packageName, "IOException when getting asset file, uri:$uri", e)
            null
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Not supported")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Not supported")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Not supported")
    }

    companion object {
        /**
         * Do not change the values in the UriMatcher because otherwise, WhatsApp will not be able to fetch the stickers from the ContentProvider.
         */
        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH)
        private const val METADATA = "metadata"
        private const val METADATA_CODE = 1
        private const val METADATA_CODE_FOR_SINGLE_PACK = 2
        const val STICKERS = "stickers"
        private const val STICKERS_CODE = 3
        const val STICKERS_ASSET = "stickers_asset"
        private const val STICKERS_ASSET_CODE = 4
        private const val STICKER_PACK_TRAY_ICON_CODE = 5
    }
}