package android.ricardoflor.turistdroid.activities

import android.content.Intent
import android.os.Bundle
import android.ricardoflor.turistdroid.MyApplication.Companion.SESSION
import android.ricardoflor.turistdroid.MyApplication.Companion.USER
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.session.SessionController
import android.ricardoflor.turistdroid.bd.user.User
import android.ricardoflor.turistdroid.bd.user.UserController
import android.ricardoflor.turistdroid.bd.user.UserDTO
import android.ricardoflor.turistdroid.bd.user.UserMapper
import android.ricardoflor.turistdroid.utils.UtilEncryptor
import android.ricardoflor.turistdroid.utils.UtilSession
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_singin.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
            //pass = editTextLoginPassword.text.toString()

            if (anyEmpty()) {
                userExists()
                Log.i("realm", "usuario logeado")
            } else {
                Log.i("realm", "usuario erroneo")
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
    private fun userExists() {
        val turistREST = TuristAPI.service
        val call = turistREST.userGetByEmail(email)
        call.enqueue((object : Callback<List<UserDTO>> {

            override fun onResponse(call: Call<List<UserDTO>>, response: Response<List<UserDTO>>) {
                Log.i("REST", "Entra en onResponse")
                if (response.isSuccessful && response.body()!!.isNotEmpty()) {
                    Log.i("REST", "Entra en isSuccessful")

                    Log.i("REST", "usuario existe")
                    val user = UserMapper.fromDTO(response.body()!![0])//saca el primer resultado
                    Log.i("rest", pass + " pass2: " + user.password)
                    if (user.password == pass) {
                        UtilSession.comprobarIDSession(user.id, applicationContext)
                        Log.i("Session: ", SESSION.toString())
                        USER = user
                        toNavigation()
                    }
                } else {
                    editTextLoginMail.error = getString(R.string.userNotCorrect)//manda mensaje de que no son correctos
                    Log.i("REST", "Error: usuario no existe")
                }

            }

            override fun onFailure(call: Call<List<UserDTO>>, t: Throwable) {
                Log.i("REST", "salta error")
                Toast.makeText(
                    applicationContext,
                    getText(R.string.service_error).toString() + t.localizedMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }))

    }

    private fun toNavigation() {
        val intent = Intent(applicationContext, NavigationActivity::class.java)
        //Elimina la pila trasera para que el boton no vuelva a esta actividad
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
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