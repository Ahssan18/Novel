package com.example.novel

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.joanzapata.pdfview.listener.OnPageChangeListener
import java.io.*


class MainActivity : AppCompatActivity() {
    private var pageNumber = 0;
    lateinit var spinner: Spinner
    val TAG = "MainActivity"
    lateinit var ivForward: ImageView
    lateinit var iv_book_mark: ImageView
    lateinit var ivBackword: ImageView
    var num = 0
    val gson = Gson()
    var checkVal = true
    lateinit var list: ArrayList<Novel>
    lateinit var list2: ArrayList<Novel>
    lateinit var pdfView: com.joanzapata.pdfview.PDFView;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWidgets()
        setSpiinner()
        clickListener()

        try {
            pdfView.fromAsset("novel.pdf")
                .enableSwipe(true).defaultPage(0)
                .onPageChange(object : OnPageChangeListener {
                    override fun onPageChanged(page: Int, pageCount: Int) {
                        Log.e(TAG, "addOnPageChangeListener => onPageSelected" + page + "num=> "+num)
                        try {
                            if (page < num) {
                                pageNumber = page
                                spinner.setSelection(page)
                            }
                        } catch (e: Exception) {
                        }
                    }

                })
                .load()
        } catch (e: Exception) {
        }


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

    private fun clickListener() {
        try {
            ivForward.setOnClickListener {
                var item = pdfView.currentPage + 1
                if (item < num) {
                    pdfView.jumpTo(item)
                    spinner.setSelection(item)
                }
            }
            ivBackword.setOnClickListener {
                var item = pdfView.currentPage - 1
                pdfView.jumpTo(item)
                spinner.setSelection(item)

            }
            var add: Boolean
            iv_book_mark.setOnClickListener {
                Log.e(TAG, "iv_book_mark" + pdfView.currentPage)
                if (iv_book_mark.contentDescription.equals("like")) {
                    iv_book_mark.setImageResource(R.drawable.heart_gray)
                    iv_book_mark.contentDescription = "unlike"
                    add = false
                } else {
                    iv_book_mark.setImageResource(R.drawable.heart_pink)
                    iv_book_mark.contentDescription = "like"
                    add = true
                }
                updateBookmark(pdfView.currentPage + 1, add)
            }
            spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    Log.e(TAG, "setOnItemSelectedListener" + position);
                    setSelecttion(position + 1)
                    if (checkVal) {
                        val mIntent = intent
                        val page = mIntent.getIntExtra("pageNumber", 0)
                        Log.e(TAG, "pageNumber" + page);

                        try {
                            if (page != 0) {
                                spinner.setSelection(page - 1)
                                setSelecttion(page)
                                pdfView.jumpTo(page - 1)
                            } else {
                                var page = PrefrenceManager.getLastPage(this@MainActivity)
                                setSelecttion(page)
                                try {
                                    spinner.setSelection(page)
                                    pdfView.jumpTo(page)
                                } catch (e: Exception) {


                                }
//                                setSelecttion(page)
                            }
                        } catch (e: Exception) {

                        }
                        Log.e(TAG, "setOnItemSelectedListener => checkVal " + checkVal);
                        checkVal = false
                    } else {
                        Log.e(TAG, "setOnItemSelectedListener => checkVal " + checkVal);
                        try {
                            pdfView.jumpTo(position + 1)
                        } catch (e: Exception) {
                            pdfView.jumpTo(position)
                        }
                        spinner.setSelection(position)

                    }
                    PrefrenceManager.setLastPage(this@MainActivity, position)


                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {
                    // your code here
                }
            })

        } catch (e: Exception) {
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

    private fun initWidgets() {
        try {
            pdfView = findViewById(R.id.pdfview);
            iv_book_mark = findViewById(R.id.iv_book_mark)
            spinner = findViewById(R.id.spinner_total)
            ivForward = findViewById(R.id.iv_forward)
            ivBackword = findViewById(R.id.iv_back)
            list = ArrayList<Novel>()
            list2 = ArrayList<Novel>()
        } catch (e: Exception) {
        }

    }

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File = File(
        context.cacheDir,
        fileName
    )
        .also {
            if (!it.exists()) {
                it.outputStream().use { cache ->
                    context.assets.open(fileName).use { inputStream ->
                        inputStream.copyTo(cache)
                    }
                }
            }
        }

    @Throws(IOException::class)
    private fun countPages(pdfFile: File): Int {
        try {
            val parcelFileDescriptor =
                ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            var pdfRenderer: PdfRenderer? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pdfRenderer = PdfRenderer(parcelFileDescriptor)
                return pdfRenderer.pageCount
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()

        }
        return 0
    }

    private fun setSpiinner() {
        val filePath = getFileFromAssets(this, "novel.pdf").absolutePath
        var file = File(filePath)
        try {
            Log.e(TAG, "setSpiinner" + pdfView.pageCount)
            num = countPages(file)
            var list = ArrayList<Int>()
            for (i in 1..num) {
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

    override fun onDestroy() {
        super.onDestroy()
    }

}