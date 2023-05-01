package com.muhsanjaved.status_saver_app

import android.content.Context
import android.content.Intent
import android.graphics.ColorSpace.Model
import android.graphics.ColorSpace.get
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.storage.StorageManager
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class MainActivity : AppCompatActivity() {

    private lateinit var rvStatusList:RecyclerView
    private lateinit var statusList:ArrayList<ModelClass>
    private lateinit var statusAdapter: StatusAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.title="All Status"
        rvStatusList = findViewById(R.id.re_status_list)
        statusList = ArrayList()


        val result = readDataFromPrefs()
        if (result){
            val sh = getSharedPreferences("DATA_PATH", MODE_PRIVATE)
            val uriPath = sh.getString("PATH","")

            contentResolver.takePersistableUriPermission(Uri.parse(uriPath), Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (uriPath != null) {

                val fileDoc = DocumentFile.fromTreeUri(applicationContext, Uri.parse(uriPath))
                for (file:DocumentFile in fileDoc!!.listFiles()){
                    if (!file.name!!.endsWith(".nomedia")){
                        val modelClass = ModelClass(file.name!!,file.uri.toString())
                        statusList.add(modelClass)
                    }
                }

                setUpRecyclerView(statusList)
            }

        }else{
            getFolderPermission()
        }

    }

    // Only android 11-12 Code write
    private fun getFolderPermission() {
        val storageManager = application.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
        val targetDirectory = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
        var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI") as Uri
        var scheme = uri.toString()
        scheme = scheme.replace("/root/", "/tree/")
        scheme += "%3A$targetDirectory"
        uri = Uri.parse(scheme)
        intent.putExtra("android.provider.extra.INITIAL_URI", uri)
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
        startActivityForResult(intent, 1234)
    }

    // Only android 11-12 Code write
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val treeUri = data?.data

            val sharedPreferences = getSharedPreferences("DATA_PATH", MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()
            myEdit.putString("PATH",treeUri.toString())
            myEdit.apply()

            if (treeUri != null) {
                contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val fileDoc = DocumentFile.fromTreeUri(applicationContext, treeUri)
                for (file:DocumentFile in fileDoc!!.listFiles()){
                    if (!file.name!!.endsWith(".nomedia")){
                        val modelClass = ModelClass(file.name!!,file.uri.toString())
                        statusList.add(modelClass)
                    }
                }

                setUpRecyclerView(statusList)
            }

        }
    }

    private fun setUpRecyclerView(statusList: ArrayList<ModelClass>) {

        statusAdapter = applicationContext?.let {
            StatusAdapter(
                it,statusList
            )
        }!!

        rvStatusList.apply {
            setHasFixedSize(true)
            layoutManager= StaggeredGridLayoutManager(2,LinearLayoutManager.VERTICAL)
            adapter = statusAdapter
        }
    }

    private fun readDataFromPrefs(): Boolean {

        val sh = getSharedPreferences("DATA_PATH", MODE_PRIVATE)
        val uriPath = sh.getString("PATH","")
        if (uriPath != null){
            if (uriPath.isEmpty()){
                return false
            }
        }
        return true
    }
}