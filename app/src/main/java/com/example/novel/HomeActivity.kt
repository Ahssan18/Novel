package com.example.novel

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    lateinit var btnBookmark: ImageView
    lateinit var btnShareApp: ImageView
    lateinit var btnReading: ImageView
    lateinit var btn5star: ImageView
    lateinit var btnMoreApps: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initViews()
        clickListener()
    }

    private fun launchPdf() {
        startActivity(
            PdfViewerActivity.launchPdfFromPath(
                this, "novel.pdf",
                "quote", "assets", true, true
            )
        )
    }

    private fun clickListener() {
        btnBookmark.setOnClickListener {
            startActivity(Intent(this, BookmarkActivity::class.java))
        }
        btnReading.setOnClickListener {
            launchPdf()
        }
        btnShareApp.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
            )
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
        }
        btn5star.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                    )
                )
            } catch (e: Exception) {
            }
        }
        btnMoreApps.setOnClickListener {
            try {
                //replace &quot;Unified+Apps&quot; with your developer name
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                    )
                )


            } catch (e: java.lang.Exception) {
                e.printStackTrace()

            }

        }
    }

    private fun initViews() {
        btnBookmark = findViewById(R.id.btn_bookmark)
        btnReading = findViewById(R.id.btn_reading)
        btnShareApp = findViewById(R.id.btn_share)
        btn5star = findViewById(R.id.btn_5satr)
        btnMoreApps = findViewById(R.id.more_apps)
    }
}