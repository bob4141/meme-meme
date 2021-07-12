package com.example.memememe

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {
    private var currentImageUrl: String? = null
    private var image: Bitmap? = null
    private var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        loadMeme()
        Glide.with(this).load(currentImageUrl).into(memeImageView)


        shareButton.setOnClickListener {
            val image: Bitmap? = getBitmapfromView(memeImageView)
            this.image = image
            checkPermissions()
            shareMeme(memeImageView)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            when {
                grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                -> {
                    val share = Intent(Intent.ACTION_SEND)
                    share.type = "Image/*"
                    share.putExtra(Intent.EXTRA_STREAM, getImageUri(this, image!!))
                    startActivity(Intent.createChooser(share, "Share via . . ."))
                }
            }
            return
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun checkPermissions() {
        var result: Int
        var listPermissionNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionNeeded.add(p)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionNeeded.toTypedArray(),
                100
            )
        }
    }

    private fun getBitmapfromView(view: ImageView?): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(memeImageView.width, memeImageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        memeImageView.draw(canvas)
        return bitmap
    }

    private fun loadMeme() {

        progressBar.visibility = View.VISIBLE

        // Instantiating the Request Queue
        val url = "https://meme-api.herokuapp.com/gimme"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                currentImageUrl = response.getString("url")
                Glide.with(this).load(currentImageUrl).listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }
                }).into(memeImageView)
            },
            {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            })

        // Access the RequestQueue through your singleton class
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    fun shareMeme(view: View) {
//        val intent = Intent(Intent.ACTION_SEND)
//        intent.type = "text/plain"
//        intent.putExtra(Intent.EXTRA_TEXT, "Check out this meme...  $currentImageUrl")
//        val chooser = Intent.createChooser(intent, "Share this meme using")
//        startActivity(chooser)
//
        val share = Intent(Intent.ACTION_SEND)
        share.type = "Image/*"
        share.putExtra(Intent.EXTRA_STREAM, getImageUri(this, image!!))
        startActivity(Intent.createChooser(share, "Share via ..."))
    }

    fun nextMeme(view: View) {
        loadMeme()
    }
}
