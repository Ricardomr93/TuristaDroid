package android.ricardoflor.turistdroid.activities

import android.content.Intent
import android.os.Bundle
import android.ricardoflor.turistdroid.MyApplication.Companion.SESSION
import android.ricardoflor.turistdroid.MyApplication.Companion.USER
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.bd.session.SessionController
import android.ricardoflor.turistdroid.bd.user.UserController
import android.ricardoflor.turistdroid.utils.UtilEncryptor
import android.ricardoflor.turistdroid.utils.UtilSession
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    var email: String = ""
    var pass: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login()
        SingIn()
    }

    /**
     * Método que cuando pulsa en en el boton si lo campos son correctos
     * logea al usuario
     */
    fun login() {
        buttonLoginLogin.setOnClickListener {
            email = editTextLoginMail.text.toString()
            pass = UtilEncryptor.encrypt(editTextLoginPassword.text.toString())!!
            if (userExists()) {
                Log.i("realm", "usuario logeado")
                UtilSession.createSession(email)
                SESSION = SessionController.selectSession()!!
                val intent = Intent(this,NavigationActivity::class.java)
                //Elimina la pila trasera para que el boton no vuelva a esta actividad
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                editTextLoginMail.error = getString(R.string.userNotCorrect)
                Log.i("realm", "usuario erroneo")
            }
        }
    }

    /**
     * Funcion onClick del botón Singin para llevarlo a la actividad
     */
    fun SingIn() {
        buttonLoginSingin.setOnClickListener {
            val intent = Intent(this, SinginActivity::class.java).apply {
            }
            startActivity(intent)
        }
    }

    /**
     * Método que busca por email y si lo encuentra
     * lo compara con la contraseña
     */
    private fun userExists(): Boolean {
        try{
            USER = UserController.selectByEmail(email)!!
        }catch (ex : IllegalArgumentException){
         Log.i("realm","usuario"+USER+"no existe en la bd")
        }
        return pass == USER.password
    }


    /**
     * Método sobreescrito que salva el estado en el ciclo del vida
     */
    override fun onSaveInstanceState(outState: Bundle) {
        // Salvamos en un bundle estas variables o estados de la interfaz
        outState.run {
            // Actualizamos los datos o los recogemos de la interfaz
            putString("EMAIL", email)
            putString("PASSWORD", pass)
        }
        // llama a la clase padre para salvar los datos
        super.onSaveInstanceState(outState)
    }

    /**
     * Metodo sobreescrito para recuperar el estado del ciclo de vida
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // Recuperamos las variables y los estados
        super.onRestoreInstanceState(savedInstanceState)
        // Recuperamos del Bundle
        savedInstanceState.run {
            email = getString("EMAIL").toString()
            pass = getString("PASSWORD").toString()
        }
    }
}