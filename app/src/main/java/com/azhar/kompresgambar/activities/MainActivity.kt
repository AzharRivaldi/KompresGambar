package com.azhar.kompresgambar.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azhar.kompresgambar.R
import com.azhar.kompresgambar.utils.CompressImage
import com.azhar.kompresgambar.utils.FileUtil.from
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var insertImage: File? = null
    var compressImage: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageInsert.setBackgroundColor(getRandomColor())
        setClickButton()
        setClearImage()
    }

    @SuppressLint("CheckResult")
    private fun setClickButton() {

        //insert gambar
        btnInsert.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        //kompres gambar
        btnCompress.setOnClickListener {
            if (insertImage == null) {
                Toast.makeText(this@MainActivity, "Please choose an image!",
                    Toast.LENGTH_SHORT).show()
            } else {
                CompressImage(this@MainActivity)
                    .setDestinationDirectoryPath(
                        File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM), "").absolutePath)
                    .compressToFileAsFlowable(insertImage!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ file ->
                        compressImage = file
                        val bitmapImage = BitmapFactory.decodeFile(compressImage!!.absolutePath)
                        imageCompress.setImageBitmap(bitmapImage)

                        tvSizeAfter.text = String.format("Size : %s",
                            getReadableFileSize(compressImage!!.length()))

                        Toast.makeText(this@MainActivity,
                            "Compressed image save in " + compressImage!!.path, Toast.LENGTH_LONG).show()
                    }) { throwable ->
                        throwable.printStackTrace()
                        Toast.makeText(this@MainActivity, throwable.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }

        //custom
        btnCustom.setOnClickListener {
            if (insertImage == null) {
                Toast.makeText(this@MainActivity,
                    "Please choose an image!", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    compressImage = CompressImage(this@MainActivity)
                        .setMaxWidth(640)
                        .setMaxHeight(480)
                        .setQuality(75)
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setDestinationDirectoryPath(
                            File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DCIM), "").absolutePath)
                        .compressToFile(insertImage!!)

                    val bitmapImage = BitmapFactory.decodeFile(compressImage!!.absolutePath)
                    imageCompress.setImageBitmap(bitmapImage)

                    tvSizeAfter.text = String.format("Size : %s", getReadableFileSize(compressImage!!.length()))
                    Toast.makeText(this@MainActivity,
                        "Compressed image save in " + compressImage!!.path, Toast.LENGTH_LONG).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setClearImage() {
        imageInsert.setBackgroundColor(getRandomColor())
        imageCompress.setImageDrawable(null)
        imageCompress.setBackgroundColor(getRandomColor())
        tvSizeAfter.text = "Size : -"
    }

    private fun getRandomColor(): Int {
        val random = Random()
        return Color.argb(100, random.nextInt(256),
            random.nextInt(256), random.nextInt(256))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(this@MainActivity,
                    "Failed to open picture!", Toast.LENGTH_SHORT).show()
                return
            }
            try {
                insertImage = from(this, data.data!!)
                imageInsert.setImageBitmap(BitmapFactory.decodeFile(insertImage?.absolutePath))
                tvSizeBefore.text = String.format("Size : %s", getReadableFileSize(insertImage!!.length()))

                imageInsert.setBackgroundColor(getRandomColor())
                imageCompress.setImageDrawable(null)
                imageCompress.setBackgroundColor(getRandomColor())
                tvSizeAfter.text = "Size : -"
            } catch (e: IOException) {
                Toast.makeText(this@MainActivity,
                    "Failed to read picture data!", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    fun getReadableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }

}