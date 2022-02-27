package com.example.novel

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class BookmarkActivity : AppCompatActivity() {
    var TAG = "BookmarkActivity"
    lateinit var listView: ListView
    lateinit var toolbar: MaterialToolbar
    lateinit var list: MutableList<Novel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)
        initViews()
        clickListeners()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.bookmark_menu, menu)
        return true
    }

    private fun clickListeners() {
        listView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                var des = PdfViewerActivity.launchPdfFromPath(
                    this@BookmarkActivity, "novel.pdf",
                    "novel", "assets", true, true
                )
                des.putExtra("pageNumber", list.get(position).id)
                startActivity(des)
                finish()
            }


        }
        toolbar.setNavigationOnClickListener {
            Log.e(TAG, "setNavigationOnClickListener")
            onBackPressed()
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setOverflowIcon(getDrawable(R.drawable.ic_three_dot));
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIconTint(Color.WHITE)
        listView = findViewById(R.id.listview)
        list = ArrayList<Novel>()
        setData("");
    }

    private fun setData(ss: String) {
        val gson = Gson()
        var s = PrefrenceManager.getBook(this)
        if (s != null)
            list = gson.fromJson(s, object : TypeToken<List<Novel>>() {}.type)
        val adapter: ArrayAdapter<*> = ArrayAdapter<Novel>(this, R.layout.custom_list, list)
        listView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item!!.itemId == R.id.clear_cache) {
            PrefrenceManager.clearChache(this)
            setData("clear")
            Toast.makeText(this, "Bookmark Cleared", Toast.LENGTH_LONG).show()
            return true
        }
        return false
    }

}