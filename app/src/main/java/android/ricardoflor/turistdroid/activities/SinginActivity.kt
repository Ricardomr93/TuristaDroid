package android.ricardoflor.turistdroid.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.bd.BdController
import android.ricardoflor.turistdroid.bd.User
import android.ricardoflor.turistdroid.utils.Encryptor
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.TextView
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import kotlinx.android.synthetic.main.activity_singin.*

class SinginActivity : AppCompatActivity() {

    private var name = ""
    private var nameuser = ""
    private var email = ""
    private var pass = ""
    private var user = User();


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singin)
        singin()
    }

    /**
     * Metodo para registrar un usuario
     * Una vez registrado, vuelve al LoginActivity
     */
    fun singin() {
        btnSing.setOnClickListener {
            if (anyEmpty()) {
                try {
                    //Comprobar el campo password
                    if (isPasswordValid(txtPass.text.toString())) {
                        addUser()
                    } else {
                        txtPass.error = resources.getString(R.string.pwd_incorrecto)
                    }

                } catch (ex: RealmPrimaryKeyConstraintException) {
                    txtEmail.error = resources.getString(R.string.isAlreadyExist)
                }

            }
        }
    }

    private fun addUser() {
        user.name = txtName.text.toString()
        user.password = Encryptor.encrypt(txtPass.text.toString())!!
        user.nameUser = txtUserName.text.toString()
        user.email = txtEmail.text.toString()
        BdController.insertUser(user)
        Log.i("user", user.toString())
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
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
     * Método que devuelve false si alguno de los valores está vácio
     */
    private fun anyEmpty(): Boolean {
        var valid = true
        if (notEmpty(txtName) && notEmpty(txtEmail) && notEmpty(txtPass) && notEmpty(txtUserName)) {
            valid = false
        }
        return valid
    }


    /**
     * Método sobreescrito que salva el estado en el ciclo del vida
     */
    override fun onSaveInstanceState(outState: Bundle) {
        // Salvamos en un bundle estas variables o estados de la interfaz
        outState.run {
            // Actualizamos los datos o los recogemos de la interfaz
            putString("EMAIL", email)
            putString("NAME", name)
            putString("NAMEUSER", nameuser)
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
            name = getString("NAME").toString()
            email = getString("EMAIL").toString()
            nameuser = getString("NAMEUSER").toString()
            pass = getString("PASSWORD").toString()

        }
    }
}