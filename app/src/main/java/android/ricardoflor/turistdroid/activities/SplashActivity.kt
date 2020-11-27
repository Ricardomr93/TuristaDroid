package android.ricardoflor.turistdroid.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //animaciones
        val scale = AnimationUtils.loadAnimation(this, R.anim.scale)
        val des_down = AnimationUtils.loadAnimation(this, R.anim.des_down)
        val alpha = AnimationUtils.loadAnimation(this, R.anim.aplha)

        //le damos la animacion
        pointlogo.animation = des_down
        txtlogo.animation = scale
        buildlogo.animation = alpha


    }
}