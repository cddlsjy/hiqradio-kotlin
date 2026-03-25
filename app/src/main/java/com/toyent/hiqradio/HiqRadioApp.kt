package com.toyent.hiqradio

import android.app.Application

class HiqRadioApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: HiqRadioApp
            private set
    }
}
