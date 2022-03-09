package com.example.novel

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.novel.util.FileUtils
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * Created by Rajat on 11,July,2020
 */

class PdfViewerActivity : AppCompatActivity() {
    private var fileUrl: String? = null
    private lateinit var btnNext: ImageView
    private lateinit var btnLeft: ImageView
    lateinit var iv_book_mark: ImageView
    lateinit var spinner: Spinner
    var checkVal = true
    var clickCount: Int = 0
    private lateinit var prf: PdfRendererView
    lateinit var mAdView: AdView
    val gson = Gson()
    private var mInterstitialAd: InterstitialAd? = null
    lateinit var list: ArrayList<Novel>
    lateinit var list2: ArrayList<Novel>
    private val TAG: String = "PdfViewerActivity"
    var pageBroadcst: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val page: Int = intent!!.getIntExtra("page", 0)
                Log.e(TAG, "BroadcastReceiver")
                if (page > 0 && page < prf.totalPageCount) {
                    spinner.setSelection(page)
                }
            } catch (e: Exception) {
            }
        }


    }

    companion object {
        const val FILE_URL = "pdf_file_url"
        const val FILE_DIRECTORY = "pdf_file_directory"
        const val FILE_TITLE = "pdf_file_title"
        const val ENABLE_FILE_DOWNLOAD = "enable_download"
        const val FROM_ASSETS = "from_assests"
        var isPDFFromPath = false
        var isFromAssets = false
        fun launchPdfFromPath(
            context: Context?,
            path: String?,
            pdfTitle: String?,
            directoryName: String?,
            enableDownload: Boolean = true,
            fromAssets: Boolean = false
        ): Intent {
            val intent = Intent(context, PdfViewerActivity::class.java)
            intent.putExtra(FILE_URL, path)
            intent.putExtra(FILE_TITLE, pdfTitle)
            intent.putExtra(FILE_DIRECTORY, directoryName)
            intent.putExtra(ENABLE_FILE_DOWNLOAD, enableDownload)
            intent.putExtra(FROM_ASSETS, fromAssets)
            isPDFFromPath = true
            return intent
        }

    }

    override fun onResume() {
        var filter = IntentFilter()
        filter.addAction("com.app.pdf")
        registerReceiver(pageBroadcst, filter)
        super.onResume()
    }

    override fun onStop() {
        unregisterReceiver(pageBroadcst)
        super.onStop()
    }

    private fun setSpiinner() {
        try {
            var list = ArrayList<Int>()
            Log.e(TAG, "totalpages: ${prf.totalPageCount}")
            for (i in 1..prf.totalPageCount) {
                list.add(i)
            }
            val ad = ArrayAdapter<Int>(
                this,
                android.R.layout.simple_spinner_item,
                list
            )
            ad.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            spinner.setAdapter(ad)
        } catch (e: Exception) {
        }

    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)
        initVies()
        addIntegration()
        intersTitialAddIntegration()
        init()
        setSpiinner()
        clickListener()

    }

    private fun intersTitialAddIntegration() {
        var adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            getString(R.string.interstitialAdmob),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError?.message)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mInterstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                intersTitialAddIntegration()
                                super.onAdDismissedFullScreenContent()
                            }
                        }
                }
            })
    }

    private fun addIntegration() {
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }


    private fun initVies() {
        iv_book_mark = findViewById(R.id.iv_book_mark)
        isFromAssets = intent.extras!!.getBoolean(
            FROM_ASSETS,
            false
        )
        prf = findViewById(R.id.pdfView)
        btnNext = findViewById(R.id.iv_forward)
        btnLeft = findViewById(R.id.iv_back)
        spinner = findViewById(R.id.spinner_total)
        list = ArrayList<Novel>()
        list2 = ArrayList<Novel>()

    }

    private fun setSelecttion(position: Int) {
        var s = PrefrenceManager.getBook(this)
        if (s != null)
            list2 = gson.fromJson(s, object : TypeToken<List<Novel>>() {}.type)
        for (n in list2) {
            Log.e(TAG, "setSelecttion" + list.size)
            Log.e(TAG, "setSelecttion  id =" + n.id + "position =" + position)
            if (n.id == position) {
                iv_book_mark.setImageResource(R.drawable.heart_pink)
                iv_book_mark.contentDescription = "like"
                break
                Log.e(TAG, "Selected " + position)
            } else {
                iv_book_mark.setImageResource(R.drawable.heart_gray)
                iv_book_mark.contentDescription = "unlike"

                Log.e(TAG, "Not Selected " + position)

            }
        }

    }

    private fun updateBookmark(position: Int, b: Boolean) {
        var s = PrefrenceManager.getBook(this)
        if (s != null)
            list = gson.fromJson(s, object : TypeToken<List<Novel>>() {}.type)
        if (b) {
            list.add(Novel("Page:", position))
        } else {
            for (novel: Novel in list) {
                if (novel.id == position) {
                    list.remove(novel)
                    break

                }
            }
        }
        val arrayData = gson.toJson(list)
        PrefrenceManager.addBookmark(this, arrayData)
    }

    private fun clickListener() {
        var add: Boolean
        iv_book_mark.setOnClickListener {
            funAddClick();
            Log.e(TAG, "iv_book_mark" + prf.currentPage)
            if (iv_book_mark.contentDescription.equals("like")) {
                iv_book_mark.setImageResource(R.drawable.heart_gray)
                iv_book_mark.contentDescription = "unlike"
                add = false
            } else {
                iv_book_mark.setImageResource(R.drawable.heart_pink)
                iv_book_mark.contentDescription = "like"
                add = true
            }
            updateBookmark(prf.currentPage, add)
        }
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                /**/
                funAddClick()
                if (checkVal) {
                    val mIntent = intent
                    val page = mIntent.getIntExtra("pageNumber", 0)
                    Log.e(TAG, "pageNumber" + page);
                    try {
                        if (page != 0) {
                            spinner.setSelection(page + 1)
                            setSelecttion(page + 2)
                            prf.moveToSpecificPosition(page - 1)
                        } else {
                            var page = PrefrenceManager.getLastPage(this@PdfViewerActivity)
                            setSelecttion(page)
                            try {
                                spinner.setSelection(page)
                                prf.moveToSpecificPosition(page - 1)
                            } catch (e: Exception) {
                            }
                        }
                    } catch (e: Exception) {

                    }
                    Log.e(TAG, "setOnItemSelectedListener => checkVal " + checkVal);
                    checkVal = false
                } else {
                    Log.e(TAG, "setOnItemSelectedListener => checkVal " + checkVal);
                    try {
                        prf.moveToSpecificPosition(position)
                    } catch (e: Exception) {
                        prf.moveToSpecificPosition(position)
                    }
                    spinner.setSelection(position)

                }
                PrefrenceManager.setLastPage(this@PdfViewerActivity, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        })

        btnLeft.setOnClickListener {
            Log.e(TAG, "page=> currentpage= ${prf.currentPage}")
            prf.moveToSpecificPosition(prf.currentPage - 2)
            spinner.setSelection(prf.currentPage - 2)
            setSelecttion(prf.currentPage - 1)
//            funAddClick();


        }
        btnNext.setOnClickListener {
            prf.moveToSpecificPosition(prf.currentPage)
            setSelecttion(prf.currentPage + 1)
            if (prf.totalPageCount > prf.currentPage)
                spinner.setSelection(prf.currentPage)
//            funAddClick();
        }
    }

    private fun funAddClick() {
        Log.d("TAG", "funAddClick.")
        clickCount += 1
        if (clickCount == 3) {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this)
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.")
            }
            clickCount = 0
        }
    }

    private fun init() {
        if (intent.extras!!.containsKey(FILE_URL)) {
            fileUrl = intent.extras!!.getString(FILE_URL)
            if (isPDFFromPath) {
                initPdfViewerWithPath(this.fileUrl)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish() // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initPdfViewerWithPath(filePath: String?) {
        if (TextUtils.isEmpty(filePath)) onPdfError()

        try {

            val file = if (isFromAssets)
                FileUtils.fileFromAsset(this, filePath!!)
            else File(filePath!!)

            prf.initWithFile(
                file,
                PdfQuality.NORMAL
            )

        } catch (e: Exception) {
            onPdfError()
        }

    }

    private fun onPdfError() {
        Toast.makeText(this, "Pdf has been corrupted", Toast.LENGTH_SHORT).show()
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        prf.closePdfRender()
    }


}