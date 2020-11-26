package android.ricardoflor.turistdroid

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    val mail: String = ""
    val pwd: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

    }

    /**
     * Funcion onClick del botón Login
     */
    fun entrar(view: View){

        //Condicion que comprueba el correo
        if (isMailValid(editTextLoginMail.text.toString())){
            editTextLoginMail.error = resources.getString(R.string.email_incorrecto)
        }

        if (!isPasswordValid(editTextLoginPassword.text.toString())){
            editTextLoginPassword.error = resources.getString(R.string.pwd_incorrecto)
        }

//        val intent = Intent(this, MainActivity::class.java).apply {
//        }
//        intent.putExtra("Email", user.email)
//        startActivity(intent)

    }

    /**
     * Metodo para validar el EMAIL
      */
    private fun isMailValid(mail: String): Boolean {
        return if (mail.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(mail).matches()
        } else {
            mail.isNotBlank()
        }
    }

    /**
     * Metodo para validar la PASSWORD
     */
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}