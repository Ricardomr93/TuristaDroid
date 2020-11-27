package android.ricardoflor.turistdroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.ricardoflor.turistdroid.bd.BdController
import android.ricardoflor.turistdroid.bd.User
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}