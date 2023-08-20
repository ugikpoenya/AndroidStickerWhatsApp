package com.ugikpoenya.sampleappstickerwhatsapp

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.ugikpoenya.appmanager.ServerManager
import com.ugikpoenya.sampleappstickerwhatsapp.databinding.ActivityMainBinding
import com.ugikpoenya.stickerwhatsapp.AddStickerPackActivity
import com.ugikpoenya.stickerwhatsapp.model.StickerBook
import com.xwray.groupie.GroupieAdapter


class MainActivity : AddStickerPackActivity() {
    private lateinit var binding: ActivityMainBinding

    private val serverManager: ServerManager = ServerManager()
    var groupieAdapter: GroupieAdapter = GroupieAdapter()

    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Fresco.initialize(this)

        val serverManager = ServerManager()
        serverManager.setBaseUrl(this, "https://master.ugikpoenya.net/api/")
        serverManager.setApiKey(this, "34489a78de193457ab5b2dee83c2a748")
        StickerBook().setPublisher(this, "Sticker WA Sample")


        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, linearLayoutManager.orientation))
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = groupieAdapter

        getJsonData()
        binding.swipeRefresh.setOnRefreshListener { getJsonData() }

        progressDialog = ProgressDialog(this);
        progressDialog?.setMessage("Loading...")
        progressDialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog?.setCancelable(false)

        groupieAdapter.setOnItemClickListener { item, view ->
            when (item) {
                is StickerPackViewHolder -> {
                    val stickerPack = item.stickerPack
                    if (StickerBook().isAssetDownloaded(this, stickerPack)) {
                        addStickerPackToWhatsApp(stickerPack)
                    } else {
                        progressDialog?.show();
                        StickerBook().downloadAsset(this, stickerPack) { finish, total ->
                            runOnUiThread {
                                var persen = ((finish.toFloat() / total.toFloat()) * 100).toInt()
                                progressDialog!!.setMessage("Downloading $persen%")
                                if (finish >= total) {
                                    progressDialog!!.dismiss()
                                    Log.d("LOG", "Item Download finsih : " + stickerPack?.name)
                                    addStickerPackToWhatsApp(stickerPack)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getJsonData() {
        binding.swipeRefresh.isRefreshing = true
        serverManager.getAssetFolders(this) { response ->
            val list = StickerBook().getStickerPackList(this, response)
            groupieAdapter.clear()
            list.forEach {
                groupieAdapter.add(StickerPackViewHolder(this, it))
            }
            binding.swipeRefresh.isRefreshing = false
            null
        }
    }

}