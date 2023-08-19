package com.ugikpoenya.sampleappstickerwhatsapp

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import com.facebook.drawee.view.SimpleDraweeView
import com.ugikpoenya.sampleappstickerwhatsapp.databinding.StickerPacksListItemBinding
import com.ugikpoenya.stickerwhatsapp.WhitelistCheck
import com.ugikpoenya.stickerwhatsapp.model.StickerPack
import com.xwray.groupie.viewbinding.BindableItem

class StickerPackViewHolder(private val activity: Activity, val stickerPack: StickerPack?) : BindableItem<StickerPacksListItemBinding>() {
    override fun getLayout(): Int {
        return R.layout.sticker_packs_list_item
    }

    override fun initializeViewBinding(view: View): StickerPacksListItemBinding {
        return StickerPacksListItemBinding.bind(view)
    }

    override fun bind(binding: StickerPacksListItemBinding, position: Int) {
        binding.trayImage.setImageURI(Uri.parse(stickerPack?.trayImageUrl), null)
        binding.stickerPackPublisher.text = stickerPack?.publisher
        binding.stickerPackTitle.text = stickerPack?.name
        binding.stickerPacksListItemImageList.removeAllViews()
        binding.stickerPackFilesize.text = stickerPack?.stickers!!.size.toString() + " stickers"
        var index = 0
        for (stiker in stickerPack.stickers!!) {
            if (index < 5) {
                index++
                val rowImage = LayoutInflater.from(activity).inflate(R.layout.sticker_packs_list_image_item, binding.stickerPacksListItemImageList, false) as SimpleDraweeView
                rowImage.setImageURI(stiker!!.imageFileUrl)
                val lp = rowImage.layoutParams as LinearLayout.LayoutParams
                lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin + 10, lp.bottomMargin)
                rowImage.layoutParams = lp
                binding.stickerPacksListItemImageList.addView(rowImage)
            }
        }
        binding.stickerPackAnimationIndicator.visibility = if (stickerPack.animatedStickerPack) VISIBLE else GONE


        if (WhitelistCheck.isWhitelisted(activity, stickerPack.identifier.toString())) {
            binding.stickerPackIconStatus.visibility = VISIBLE
        } else {
            binding.stickerPackIconStatus.visibility = GONE

        }
    }
}