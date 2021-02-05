package android.ricardoflor.turistdroid.activities


import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.utils.UtilEncryptor
import android.ricardoflor.turistdroid.utils.UtilNet
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_singin.*
import java.util.*


class LoginActivity : AppCompatActivity() {
    //autenticador
    private lateinit var auth: FirebaseAuth
    var email: String = ""
    var pass: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        login()
        SingIn()
    }

    /**
     * Método que cuando pulsa en en el boton si lo campos son correctos
     * logea al usuario
     */
    private fun login() {
        buttonLoginLogin.setOnClickListener {
            email = editTextLoginMail.text.toString()
            pass = UtilEncryptor.encrypt(editTextLoginPassword.text.toString())!!

            if (anyEmpty()) {
                if (UtilNet.hasInternetConnection(this)) {
                    userExists(email, pass)
                } else {//muestra una barra para pedir conexion a internet
                    val snackbar = Snackbar.make(
                        findViewById(android.R.id.content),
                        R.string.no_net,
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackbar.setActionTextColor(getColor(R.color.accent))
                    snackbar.setAction("Conectar") {
                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                        startActivity(intent)
                        finish()
                    }
                    snackbar.show()
                }
                Log.i("realm", "usuario logeado")
            }
        }
    }

    /**
     * Método que devuelve false si alguno de los valores está vácio
     */
    private fun anyEmpty(): Boolean {
        var valid = true
        if (notEmpty(editTextLoginMail) && notEmpty(editTextLoginPassword)) {
            valid = false
        }
        return valid
    }

    /**
     * Método que comprueba si el campo esta vacio y lanza un mensaje
     * @param txt TextView
     */
    private fun notEmpty(txt: TextView): Boolean {
        var empty = false
        if (txt.text.isEmpty()) {
            txt.error = resources.getString(R.string.isEmpty)
            empty = true
        }
        return empty
    }

    /**
     * Funcion onClick del botón Singin para llevarlo a la actividad
     */
    private fun SingIn() {
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
    private fun userExists(email: String, password: String) {

        //loginProgressBar.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.i("fairbase", "signInWithEmail:success")
                    val user = auth.currentUser
                    Log.i("fairbase", user.toString())
                    //Toast.makeText(baseContext, "Auth: Usuario autentificado con éxito", Toast.LENGTH_SHORT).show()
                    toNavigation()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("fairbase", "signInWithEmail:failure", task.exception)
                    editTextLoginMail.error = resources.getString(R.string.userNotCorrect)
                }

            }
        //loginProgressBar.visibility = View.INVISIBLE
    }

    /**
     * Metodo que para ir al navigation
     */
    private fun toNavigation() {
        val intent = Intent(applicationContext, NavigationActivity::class.java)
        startActivity(intent)
        finish()
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