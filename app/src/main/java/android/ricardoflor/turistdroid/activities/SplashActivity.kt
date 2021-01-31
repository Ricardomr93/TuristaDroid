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
import kotlinx.android.synthetic.main.activity_splash.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.util.*


class SplashActivity : AppCompatActivity() {

    private val TIME_SPLASH: Long = 4000
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
                   init()
            }
        }, TIME_SPLASH)
    }
    private fun init() {
        if (UtilSession.sessionExist(this)) {//si existe sesion comprueba
            Log.i("Rest","hay sesion")
            checkLogin()
        }else{//si no hay sesion va directo al login
            Log.i("Rest","no hay sesion")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * metodo que comprueba que tod está correcto
     */
    private fun checkLogin() {
        if (!UtilNet.hasInternetConnection(this)) {//si no tiene internet borra pref y va al login
            UtilSession.deleteSessionPref(this)
            toLogin(R.string.no_net_back_login.toString())
        } else {// comprueba si el tiempo a expirado
            timeExpired()
        }
    }

    /**
     * Metodo que comprueba si el tiempo de sesion es menor que el limite
     * si es asi modifica la fecha de la sesion a la de hoy
     * en caso contrario vuelve al login y borra sesion
     */
    private fun timeExpired() {
        if (UtilSession.timeExpired(this)) {
            UtilSession.deleteSessionPref(this)
            toLogin(R.string.limite_expired.toString())
        } else {
            updatetimeSession()
        }
    }

    /**
     * Modifica la sesion actual por el tiempo de hoy
     */
    private fun updatetimeSession() {
        val preferences = getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        val id = preferences.getString("sessionUID", "")!!.toString()
        val session = Session(
            id = id,
            time = Instant.now().toString(),
            token = UUID.randomUUID().toString()
        )
        val clientREST = TuristAPI.service
        val call: Call<SessionDTO> = clientREST.sesionUpdate(id, SessionMapper.toDTO(session))
        call.enqueue((object : Callback<SessionDTO> {

            override fun onResponse(call: Call<SessionDTO>, response: Response<SessionDTO>) {
                if (response.isSuccessful) {
                    toNavigation()
                }
            }
            override fun onFailure(call: Call<SessionDTO>, t: Throwable) {
                toLogin(R.string.service_error.toString())
            }
        }))
    }

    /**
     * metodo que viaja al navigation
     */
    private fun toNavigation() {
        val intent = Intent(applicationContext, NavigationActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * metodo que viaja al login y da un mensaje en Toast de lo que ha ocurrido
     */
    private fun toLogin(string: String){
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        Toast.makeText(
            applicationContext,
            string,
            Toast.LENGTH_LONG
        )
            .show()
    }


}