package android.ricardoflor.turistdroid.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.ricardoflor.turistdroid.R
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    private val TIME_SPLASH : Long  = 4000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //animaciones
        val scale = AnimationUtils.loadAnimation(this, R.anim.scale)
        val desdown = AnimationUtils.loadAnimation(this, R.anim.des_down)
        val alpha = AnimationUtils.loadAnimation(this, R.anim.aplha)

        //le damos la animacion
        pointlogo.animation = desdown
        txtlogo.animation = scale
        buildlogo.animation = alpha

        //cargamos el login con un delay
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                startActivity(Intent(this,LoginActivity::class.java))
                finish()
            }
        },TIME_SPLASH)
        //cargamos el activity
        //intent = Intent(this,LoginActivity::class.java)
        //startActivity(intent)



    }
}