package com.ugikpoenya.stickerwhatsapp.model

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import com.ugikpoenya.stickerwhatsapp.Config
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.util.Locale
import java.util.Objects


class StickerBook {
    fun getPublisher(context: Context): String {
        val sharedPref = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val publisher = sharedPref.getString("publisher", "").toString()
        return if (publisher.isNullOrEmpty()) "Sticker Wa"
        else publisher
    }

    fun setPublisher(context: Context, publisher: String) {
        val sharedPref = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("publisher", publisher)
        editor.apply()
    }


    fun getStickerPackInfo(context: Context, uri: Uri, stickerPackList: List<StickerPack>? = null): Cursor {
        Log.d("LOG", "getStickerPackInfo ")
        val cursor = MatrixCursor(arrayOf(STICKER_PACK_IDENTIFIER_IN_QUERY, STICKER_PACK_NAME_IN_QUERY, STICKER_PACK_PUBLISHER_IN_QUERY, STICKER_PACK_ICON_IN_QUERY, ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, IOS_APP_DOWNLOAD_LINK_IN_QUERY, PUBLISHER_EMAIL, PUBLISHER_WEBSITE, PRIVACY_POLICY_WEBSITE, LICENSE_AGREENMENT_WEBSITE, IMAGE_DATA_VERSION, AVOID_CACHE, ANIMATED_STICKER_PACK))
        var packList = stickerPackList
        if (packList == null) {
            packList = readContentFile(context)
        }

        for (stickerPack in packList) {
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

    fun getCursorForSingleStickerPack(context: Context, uri: Uri): Cursor {
        val identifier = uri.lastPathSegment
        Log.d("LOG", "getCursorForSingleStickerPack " + identifier)
        for (stickerPack in readContentFile(context)) {
            if (identifier == stickerPack.identifier) {
                return getStickerPackInfo(context, uri, listOf(stickerPack))
            }
        }
        return getStickerPackInfo(context, uri, ArrayList())
    }

    fun getStickersForAStickerPack(context: Context, uri: Uri): Cursor {
        val stickerPack = StickerBook().getStickerPackFile(context, uri.lastPathSegment)
        val cursor = MatrixCursor(arrayOf(STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY))
        for (sticker in stickerPack?.stickers!!) {
            cursor.addRow(arrayOf<Any?>(sticker!!.imageFileName, TextUtils.join(",", sticker.emojis!!)))
        }
        cursor.setNotificationUri(Objects.requireNonNull(context)!!.contentResolver, uri)
        return cursor
    }

    fun getStickerPackFile(context: Context, identifier: String?): StickerPack? {
        Log.d("LOG", "getStickerPackFile " + identifier)
        val publisher = getPublisher(context)
        val folder = context.getExternalFilesDir(identifier)
        val files = folder?.listFiles()
        val fileName: MutableList<String> = ArrayList()
        if (files?.size!! > 0) {
            for (f in files.indices) {
                fileName.add(files[f].name)
            }
        }
        return stickerPackParser(identifier.toString(), fileName, publisher)
    }

    fun deletePack(context: Context, identifier: String?) {
        Log.d("LOG", "deletePack " + identifier)
        val directory = context.getExternalFilesDir(identifier)
        if (directory!!.isDirectory) {
            for (file in directory.listFiles()) {
                file.delete()
            }
            directory.delete()
        }
    }

    fun readContentFile(context: Context): List<StickerPack> {
        Log.d("LOG", "readContentFile StickerBook")
        val publisher = getPublisher(context)
        val fl = context.getExternalFilesDir("")
        val folders = fl!!.listFiles()
        return if (folders.size > 0) {
            val stickerPacks: MutableList<StickerPack> = ArrayList()
            for (i in folders.indices) {
                val folder = folders[i].name
                val files = folders[i].listFiles()
                val fileName: MutableList<String> = ArrayList()
                if (files.size > 0) {
                    for (f in files.indices) {
                        fileName.add(files[f].name)
                    }
                }
                val stickerPack = stickerPackParser(folder, fileName, publisher)
                if (stickerPack != null) {
                    if (stickerPack.name == "name") {
                        val fileName = File(context.getExternalFilesDir(stickerPack.identifier), "name.txt")
                        if (fileName.exists()) {
                            stickerPack.name = fileName.readText()
                        }
                    }
                    stickerPacks.add(stickerPack)
                }
            }
            stickerPacks
        } else {
            ArrayList()
        }
    }

    fun iniStickerPackListParser(context: Context, folders: Map<String, ArrayList<String>>?): ArrayList<StickerPack> {
        val publisher = getPublisher(context)
        val stickerPackList: ArrayList<StickerPack> = ArrayList()
        folders?.forEach { (key, value) ->
            val stickerPack = StickerBook().stickerPackParser(key, value, publisher)
            if (stickerPack?.stickers?.size!! > 3) {
                stickerPackList.add(stickerPack)
            }
        }
        return stickerPackList
    }

    fun stickerPackParser(folder: String, files: List<String>, publisher: String): StickerPack? {
        val identifier = folder.replace("/", "")
        var name: String? = null
        var trayImageFile: String? = null
        var trayImageUrl: String? = null
        val publisherEmail: String? = null
        val publisherWebsite: String? = null
        val privacyPolicyWebsite: String? = null
        val licenseAgreementWebsite: String? = null
        val imageDataVersion = "1"
        val avoidCache = false
        var animatedStickerPack = false
        val stickerList: MutableList<Sticker?> = ArrayList()

        for (file in files) {
            val filename = file.substring(file.lastIndexOf('/') + 1).trim { it <= ' ' }
            val extension = file.substring(file.lastIndexOf(".") + 1).trim { it <= ' ' }
            val imageExt = arrayOf("jpg", "jpeg", "png")
            if (listOf(*imageExt).contains(extension.lowercase(Locale.getDefault()))) {
                trayImageFile = filename
                trayImageUrl = file
            }

            if (filename.lowercase() == "animated.txt") {
                animatedStickerPack = true
            }

            if (extension.lowercase(Locale.getDefault()) == "txt") {
                val nameExclude = arrayOf("animated.txt", "author.txt", "title.txt")
                if (!listOf(*nameExclude).contains(filename.lowercase(Locale.getDefault()))) {
                    name = filename.substring(0, filename.lastIndexOf('.'));
                }
            }

            if (extension.lowercase(Locale.getDefault()) == "webp") {
                val emojis: List<String> = ArrayList(Config.EMOJI_MAX_LIMIT)
                val sticker = Sticker(filename, file, emojis)
                if (stickerList.size < 30) {
                    stickerList.add(sticker)
                }
                if (trayImageFile == null) {
                    trayImageFile = filename
                    trayImageUrl = file
                }
            }
        }

        return if (stickerList.size > 0) {
            if (name == null) {
                name = trayImageFile?.substring(0, trayImageFile.lastIndexOf('.'))
            }
            try {
                name = URI(name).path
            } catch (e: IOException) {
                Log.d("LOG", "Error name " + name)
            }

            Log.d("LOG", "Parser " + identifier + " / " + stickerList.size)
            val stickerPack = StickerPack(identifier, name, publisher, trayImageFile, trayImageUrl, publisherEmail, publisherWebsite, privacyPolicyWebsite, licenseAgreementWebsite, imageDataVersion, avoidCache, animatedStickerPack)
            stickerPack.setStickers(stickerList)
            stickerPack
        } else {
            null
        }
    }

    fun isAssetDownloaded(context: Context, stickerPack: StickerPack?): Boolean {
        val folderAssets = context.getExternalFilesDir(stickerPack?.identifier)
        if (!folderAssets!!.isDirectory) {
            return false
        }
        val files = folderAssets.listFiles()
        var count = 0
        for (file in files) {
            if (file.extension.lowercase() == "webp") {
                count++
            }
        }
        return count >= 3
    }

    var TOTAL_DOWNLOAD = 0
    var FINISH_DOWNLOAD = 1

    fun downloadAsset(context: Context, stickerPack: StickerPack?, function: (finish: Int, total: Int) -> (Unit)) {
        if (stickerPack != null) {
            TOTAL_DOWNLOAD = 0
            FINISH_DOWNLOAD = 1
            val folderAssets = context.getExternalFilesDir(stickerPack?.identifier)
            if (!folderAssets!!.isDirectory) {
                folderAssets.mkdirs()
            }

            try {
                if (stickerPack.animatedStickerPack) {
                    val file = File(context.getExternalFilesDir(stickerPack.identifier), "animated.txt")
                    Log.d("LOG", "Create animated.txt")
                    file.createNewFile()
                }

                val fileName = File(context.getExternalFilesDir(stickerPack.identifier), "name.txt")
                fileName.createNewFile()
                fileName.writeText(stickerPack.name.toString())
                Log.d("LOG", "Create name.txt")

            } catch (e: Exception) {
                Log.d("LOG", "Erorr txt " + e.message)
            }

            val extension = stickerPack.trayImageUrl!!.substring(stickerPack.trayImageUrl!!.lastIndexOf(".") + 1)
            if (extension.lowercase(Locale.getDefault()) != "webp") {
                downloadImage(context, stickerPack.identifier, stickerPack.trayImageUrl, function)
            }
            for (sticker in stickerPack.stickers!!) {
                downloadImage(context, stickerPack.identifier, sticker?.imageFileUrl, function)
            }
        }
    }

    fun downloadImage(context: Context, identifier: String?, url: String?, function: (finish: Int, total: Int) -> (Unit)) {
        TOTAL_DOWNLOAD++
        val request = InputStreamVolleyRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val filename = url?.substring(url.lastIndexOf('/') + 1)?.trim { it <= ' ' }
                    val file = File(context.getExternalFilesDir(identifier), filename)
                    val fos = FileOutputStream(file)
                    fos.write(response)
                    fos.close()
                } catch (e: IOException) {
                    Log.d("LOG", "Download error " + e.message)
                }
                function(FINISH_DOWNLOAD++, TOTAL_DOWNLOAD)
            },
            { error ->
                Log.d("LOG", "onErrorResponse " + error.message)
                function(FINISH_DOWNLOAD++, TOTAL_DOWNLOAD)
            }
        )
        val mRequestQueue = Volley.newRequestQueue(context, HurlStack())
        mRequestQueue.add(request)
    }

    class InputStreamVolleyRequest(
        method: Int, mUrl: String?, listener: Response.Listener<ByteArray>,
        errorListener: Response.ErrorListener?
    ) : Request<ByteArray?>(method, mUrl, errorListener) {
        private var mListener: Response.Listener<ByteArray>? = null
        var responseHeaders: Map<String, String>? = null

        init {
            setShouldCache(false)
            mListener = listener
        }

        override fun parseNetworkResponse(response: NetworkResponse?): Response<ByteArray?> {
            responseHeaders = response?.headers
            return Response.success(response?.data, HttpHeaderParser.parseCacheHeaders(response))
        }

        override fun deliverResponse(response: ByteArray?) {
            mListener?.onResponse(response)
        }
    }

    companion object {
        /**
         * Do not change the strings listed below, as these are used by WhatsApp. And changing these will break the interface between sticker app and WhatsApp.
         */
        const val STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier"
        const val STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name"
        const val STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher"
        const val STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon"
        const val ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link"
        const val IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link"
        const val PUBLISHER_EMAIL = "sticker_pack_publisher_email"
        const val PUBLISHER_WEBSITE = "sticker_pack_publisher_website"
        const val PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website"
        const val LICENSE_AGREENMENT_WEBSITE = "sticker_pack_license_agreement_website"
        const val IMAGE_DATA_VERSION = "image_data_version"
        const val AVOID_CACHE = "whatsapp_will_not_cache_stickers"
        const val ANIMATED_STICKER_PACK = "animated_sticker_pack"
        const val STICKER_FILE_NAME_IN_QUERY = "sticker_file_name"
        const val STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji"
    }
}