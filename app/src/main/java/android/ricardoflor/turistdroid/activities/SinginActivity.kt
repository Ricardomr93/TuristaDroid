package android.ricardoflor.turistdroid.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.util.Patterns
import android.view.View
import kotlinx.android.synthetic.main.activity_singin.*

class SinginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singin)

    }

    /**
     * Metodo para registrar un usuario
     * Una vez registrado, vuelve al LoginActivity
     */
    fun singin(view: View){

        //Comprobar si el campo nombre no esta vacio
        if (!editTextSinginName.text.equals("")){

            //Comprobar que el campo nombre de usuario no esta vacio
            if (!editTextSinginUserName.text.equals("")){

                //Comprobar el campo mail
                if (!isMailValid(editTextSinginEmail.text.toString())){

                    //Comprobar el campo password
                    if (isPasswordValid(editTextSinginPassword.text.toString())){

                        val intent = Intent(this, LoginActivity::class.java).apply {  }
                        startActivity(intent)

                     }else {
                        editTextSinginPassword.error = resources.getString(R.string.pwd_incorrecto)
                    }

                } else {
                    editTextSinginEmail.error = resources.getString(R.string.email_incorrecto)
                }

            } else {
                editTextSinginUserName.error = resources.getString(R.string.name_incorrecto)
            }

        } else {
            editTextSinginName.error = resources.getString(R.string.name_incorrecto)
        }


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