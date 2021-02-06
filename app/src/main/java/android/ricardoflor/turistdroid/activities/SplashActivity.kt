package android.ricardoflor.turistdroid.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.session.Session
import android.ricardoflor.turistdroid.bd.session.SessionDTO
import android.ricardoflor.turistdroid.bd.session.SessionMapper
import android.ricardoflor.turistdroid.utils.UtilNet
import android.ricardoflor.turistdroid.utils.UtilSession
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import kotlinx.android.synthetic.main.activity_splash.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.util.*


class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val TIME_SPLASH: Long = 4000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // Initialize Firebase Auth
        auth = Firebase.auth

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
               init()
            }
        }, TIME_SPLASH)
    }
    private fun init() {

        val currentUser = auth.currentUser
        if(currentUser!=null) {
            Log.i("fairebase", "SÍ hay sesión activa")
            toNavigation()
        } else {
            toLogin()
            Log.i("fairebase", "NO hay sesión activa")
        }

    }
    /**
     * metodo que viaja al navigation
     */
    private fun toNavigation() {
        val intent = Intent(applicationContext, NavigationActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.face_in,R.anim.face_out)
        finish()
    }

    /**
     * metodo que viaja al login y da un mensaje en Toast de lo que ha ocurrido
     */
    private fun toLogin(){
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(R.anim.face_in,R.anim.face_out)
        finish()
    }




}