package android.ricardoflor.turistdroid.activities

import android.content.Intent
import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.bd.BdController
import android.ricardoflor.turistdroid.bd.Session
import android.ricardoflor.turistdroid.bd.User
import android.ricardoflor.turistdroid.bd.UserController
import android.ricardoflor.turistdroid.utils.Encryptor
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    var email: String = ""
    var pass: String = ""
    var user2 = User();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login()
        registrarse()
    }

    /**
     * Método
     */
    fun login() {
        buttonLoginLogin.setOnClickListener {
            email = editTextLoginMail.text.toString()
            pass = Encryptor.encrypt(editTextLoginPassword.text.toString())!!
            if (userExists()) {
                Log.i("user", "usuario logeado")
                //crea la session con el email del usuario logeado
                //val session = Session(email)
                //SessionController.insertSession(session)
                val intent = Intent(this,NavigationActivity::class.java)
                startActivity(intent)
            } else {
                editTextLoginMail.error = getString(R.string.userNotCorrect)
                Log.i("user", "usuario erroneo")
            }
        }
    }

    /**
     * Funcion onClick del botón Singin
     */
    fun registrarse() {
        buttonLoginSingin.setOnClickListener {
            val intent = Intent(this, SinginActivity::class.java).apply {
            }
            startActivity(intent)
        }
    }

    /**
     * Método que busca por email y si lo compara con la contraseña
     */
    private fun userExists(): Boolean {
        try{
            user2 = UserController.selectByEmail(email)!!
        }catch (ex : IllegalArgumentException){
         Log.i("user","usuario no existe en la bd")
        }
        return pass == user2.password
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