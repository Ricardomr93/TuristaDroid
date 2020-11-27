package android.ricardoflor.turistdroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.ricardoflor.turistdroid.bd.BdController
import android.ricardoflor.turistdroid.bd.*
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*
        var user = User()
        user.email ="ric@gmail.com"
        var site = Site()
        site.id = 2

        //BdController.insertUser(user)
        //BdController.insertSite(site)

        //user.places.add(site)
       // BdController.updateUser(user)
        val sele = BdController.selectByEmail("ric@gmail.com")!!
        val usere = BdController.selectAllUser()
        Log.i("total",sele.places.toString())*/

    }
}