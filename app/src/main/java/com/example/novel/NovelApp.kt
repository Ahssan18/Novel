package com.example.novel

import android.app.Application
import com.google.android.gms.ads.MobileAds


class NovelApp:Application (){
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(
            this
        ) { }
    }
}