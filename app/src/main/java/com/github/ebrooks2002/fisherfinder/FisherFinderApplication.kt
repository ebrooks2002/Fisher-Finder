package com.github.ebrooks2002.fisherfinder

import android.app.Application
import com.github.ebrooks2002.fisherfinder.di.AppContainer
import com.github.ebrooks2002.fisherfinder.di.DefaultAppContainer


class FisherFinderApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
