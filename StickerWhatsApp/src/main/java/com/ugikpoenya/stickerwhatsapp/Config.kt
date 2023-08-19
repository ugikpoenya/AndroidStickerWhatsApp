package com.ugikpoenya.stickerwhatsapp

object Config {
    const val ADD_PACK = 200
    const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
    const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
    const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"
    const val EXTRA_STICKER_PACK_WEBSITE = "sticker_pack_website"
    const val EXTRA_STICKER_PACK_EMAIL = "sticker_pack_email"
    const val EXTRA_STICKER_PACK_PRIVACY_POLICY = "sticker_pack_privacy_policy"
    const val EXTRA_STICKER_PACK_LICENSE_AGREEMENT = "sticker_pack_license_agreement"
    const val EXTRA_STICKER_PACK_TRAY_ICON = "sticker_pack_tray_icon"
    const val EXTRA_SHOW_UP_BUTTON = "show_up_button"
    const val EXTRA_STICKER_PACK_DATA = "sticker_pack"
    private const val STATIC_STICKER_FILE_LIMIT_KB = 100
    private const val ANIMATED_STICKER_FILE_LIMIT_KB = 500
    const val EMOJI_MAX_LIMIT = 3
    private const val EMOJI_MIN_LIMIT = 1
    private const val IMAGE_HEIGHT = 512
    private const val IMAGE_WIDTH = 512
    private const val STICKER_SIZE_MIN = 3
    private const val STICKER_SIZE_MAX = 30
    private const val CHAR_COUNT_MAX = 128
    private const val KB_IN_BYTES: Long = 1024
    private const val TRAY_IMAGE_FILE_SIZE_MAX_KB = 50
    private const val TRAY_IMAGE_DIMENSION_MIN = 24
    private const val TRAY_IMAGE_DIMENSION_MAX = 512
    private const val ANIMATED_STICKER_FRAME_DURATION_MIN = 8
    private const val ANIMATED_STICKER_TOTAL_DURATION_MAX = 10 * 1000 //ms
    private const val PLAY_STORE_DOMAIN = "play.google.com"
    private const val APPLE_STORE_DOMAIN = "itunes.apple.com"
}