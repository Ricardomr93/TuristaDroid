package android.ricardoflor.turistdroid

import android.app.Application
import android.ricardoflor.turistdroid.bd.BdController
import android.ricardoflor.turistdroid.bd.session.Session
import android.ricardoflor.turistdroid.bd.session.SessionController
import android.ricardoflor.turistdroid.bd.user.User
import android.ricardoflor.turistdroid.bd.user.UserController
import android.ricardoflor.turistdroid.utils.UtilSession
import android.util.Log

class MyApplication : Application() {
    //Variable estatica

    companion object{
        var USER = User()
        var SESSION = Session()
    }

    override fun onCreate() {
        super.onCreate()
        BdController.initRealm(this)
        sessionExist()
    }
    fun sessionExist(){
        try {
            SESSION = SessionController.selectSession()!!
            if (SESSION.useremail != ""){
                USER = UserController.selectByEmail(SESSION.useremail)!!
                Log.i("util", "Usuario existe $USER")
            }else{
                Log.i("util", "otro")
            }
        }catch (ex: IllegalArgumentException){
        }

    }
}