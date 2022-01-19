package com.example.ebookepup

import android.R.attr
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.ActivityNotFoundException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.folioreader.FolioReader
import android.widget.Toast

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import android.R.attr.data
import android.util.Log
import android.provider.MediaStore
import androidx.loader.content.CursorLoader


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        showFileChooser()


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
                val uriString ="file:/" +uri!!.path.toString()
                Log.e("filex",uriString)
                val folioReader = FolioReader.get()
                val myFile = File(uriString)
                val pathf =getRealPathFromUri(uri!!)
                val path = myFile.absolutePath
                folioReader.openBook(path)
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
    private fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursorLoader = CursorLoader(applicationContext, uri, projection, null, null, null)
        val cursor: Cursor = cursorLoader.loadInBackground()!!
        val column = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val result = cursor.getString(column)
        cursor.close()
        return result
    }
}
