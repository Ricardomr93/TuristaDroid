package android.ricardoflor.turistdroid.activities

import android.app.Application
import android.ricardoflor.turistdroid.bd.BdController

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BdController.initRealm(this)
    }
}