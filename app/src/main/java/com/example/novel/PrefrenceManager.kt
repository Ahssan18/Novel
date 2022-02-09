package com.example.novel

import android.content.Context
import android.content.SharedPreferences

class PrefrenceManager {


    companion object {
        fun getPref(context: Context): SharedPreferences {
            return context.getSharedPreferences("Novel", Context.MODE_PRIVATE)
        }

        fun addBookmark(context: Context, bookmark: String?) {
            getPref(context).edit().putString("list", bookmark).apply()
        }

        fun getBook(context: Context): String? {
            return getPref(context).getString("list", null)
        }

        fun setLastPage(context: Context, position: Int) {
            getPref(context).edit().putInt("lastPage", position).apply()
        }

        fun getLastPage(context: Context): Int {
            return getPref(context).getInt("lastPage", 0)
        }

        fun clearChache(context: Context) {
            getPref(context).edit().remove("list").apply()
        }
    }

}