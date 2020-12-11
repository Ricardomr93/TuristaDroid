package android.ricardoflor.turistdroid

import android.app.Application
import android.ricardoflor.turistdroid.bd.BdController
import android.ricardoflor.turistdroid.bd.session.Session
import android.ricardoflor.turistdroid.bd.user.User

class MyApplication : Application() {
    //Variable estatica

    companion object{
        var USER = User()
        var SESSION = Session()
    }

    override fun onCreate() {
        super.onCreate()
        BdController.initRealm(this)
    }
}