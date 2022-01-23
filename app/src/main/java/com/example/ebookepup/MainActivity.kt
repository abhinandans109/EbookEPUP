package com.example.ebookepup

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import com.folioreader.Config
import com.folioreader.FolioReader
import com.folioreader.Constants
import java.io.File
import androidx.core.app.ActivityCompat

import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri

import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val s="helo"


        val button : Button = findViewById(R.id.button)
        button.setOnClickListener {
            showFileChooser()
        }

    }
    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(
                Intent.createChooser(intent, "Select a File to Upload"), 1
            )
        } catch (ex: ActivityNotFoundException) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(
                this, "Please install a File Manager.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1 -> if (resultCode === RESULT_OK) {
                // Get the Uri of the selected file
                val uri: Uri? = data!!.data
                val uriString =uri!!.path.toString()
                Log.e("filex",uriString)

                val folioReader = FolioReader.get()
                val myFile = File(uriString)
                var path = myFile.canonicalPath
                path=path.split(":")[1]
                isReadStoragePermissionGranted(path)
                var displayName: String? = null
                if (uriString.startsWith("content://")) {
                    var cursor: Cursor? = null
                    try {
                        cursor =
                            applicationContext.getContentResolver().query(uri!!, null, null, null, null)
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName =
                                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            Toast.makeText(applicationContext,displayName,Toast.LENGTH_SHORT).show()

                        }
                    } finally {
                        cursor!!.close()
                    }
                } else if (uriString.startsWith("file://")) {
                    displayName = myFile.name

                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun isReadStoragePermissionGranted(file:String?): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v("TAG", "Permission is granted1")
                val config: Config = Config()
                    .setAllowedDirection(Config.AllowedDirection.ONLY_VERTICAL)
                    .setDirection(Config.Direction.VERTICAL)
                    .setFont(Constants.FONT_LORA)
                    .setFontSize(5)
                    .setNightMode(true)
                    .setShowTts(true)

                val folioReader = FolioReader.get()
                val path = File(Environment.getExternalStorageDirectory(),file!!)
                val paths = path.path
                folioReader.openBook(paths)
                true
            } else {
                Log.v("TAG", "Permission is revoked1")
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    3)
                Toast.makeText(this,"Permission not Granted",Toast.LENGTH_SHORT).show()
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG", "Permission is granted1")
            true
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            2 -> {
                Log.d("TAG", "External storage2")
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("TAG", "Permission: " + permissions[0] + "was " + grantResults[0])
                    //resume tasks needing this permission
                    val config: Config = Config()
                        .setAllowedDirection(Config.AllowedDirection.ONLY_VERTICAL)
                        .setDirection(Config.Direction.VERTICAL)
                        .setFont(Constants.FONT_LORA)
                        .setFontSize(5)
                        .setNightMode(true)
                        .setShowTts(true)

                    val folioReader = FolioReader.get()
                    val path = File(Environment.getExternalStorageDirectory(),"qbc.epub")
                    val paths = path.path
                    folioReader.openBook(paths)

                } else {
                    Toast.makeText(this,"Permission not Granted",Toast.LENGTH_SHORT).show()
                }
            }
            3 -> {
                Log.d("TAG", "External storage1")
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("TAG", "Permission: " + permissions[0] + "was " + grantResults[0])
                    //resume tasks needing this permission

                } else {

                }
            }
        }
    }
}
