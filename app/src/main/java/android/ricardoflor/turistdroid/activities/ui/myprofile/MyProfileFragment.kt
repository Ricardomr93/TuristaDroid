package android.ricardoflor.turistdroid.activities.ui.myprofile

import android.content.Intent
import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.LoginActivity
import android.ricardoflor.turistdroid.activities.LoginActivity.Companion.SESSION
import android.ricardoflor.turistdroid.activities.LoginActivity.Companion.USER
import android.ricardoflor.turistdroid.bd.session.SessionController
import android.ricardoflor.turistdroid.bd.user.UserController
import android.ricardoflor.turistdroid.utils.UtilImage
import android.text.Editable
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_singin.*
import kotlinx.android.synthetic.main.fragment_my_profile.*
import kotlinx.android.synthetic.main.fragment_my_sites.*

class MyProfileFragment : Fragment() {
    val emailUser = txtEmailMyProfile.text.toString()
    val passUser = txtPassMyprofile.text.toString()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_profile, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        updateUser()
        deleteUser()
        getInformation()
    }

    private fun updateUser() {
        btnUpdateMyprofile.setOnClickListener {
            if (isMailValid(emailUser)) {
                if (!isPasswordValid(passUser)) {
                    if (!someIsEmpty()) {
                        Toast.makeText(context!!, getText(R.string.update_user), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    txtPassMyprofile.error = resources.getString(R.string.pwd_incorrecto)
                }
            } else {
                txtEmailMyProfile.error = resources.getString(R.string.email_incorrecto)

            }
        }
    }

    /**
     * Metodo para validar el EMAIL
     */
    private fun isMailValid(mail: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(mail).matches()
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
    private fun empty(txt: TextView): Boolean {
        var empty = false
        if (txt.text.isEmpty()) {
            txt.error = resources.getString(R.string.isEmpty)
            empty = true
        }
        return empty
    }

    /**
     * Método que devuelve true si alguno de los valores está vácio
     */
    private fun someIsEmpty(): Boolean {
        var valid = false
        if (empty(txtEmailMyProfile) || empty(txtNameMyProfile) || empty(txtPassMyprofile) || empty(txtUserNameMyProfile)) {
            valid = true
        }
        return valid
    }

    private fun deleteUser() {
        btnUnsubMyProfile.setOnClickListener {
            UserController.deleteUser(USER.email)
            SessionController.deleteSession(SESSION)
            startActivity(Intent(context, LoginActivity::class.java))
            Toast.makeText(context!!, "USUARIO BORRADO", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getInformation() {
        txtEmailMyProfile.setText(USER.email)
        txtNameMyProfile.setText(USER.name)
        txtUserNameMyProfile.setText(USER.nameUser)
        if (USER.image != "") {
            Log.i("util", "Carga imagen")
            imgMyprofile.setImageBitmap(UtilImage.toBitmap(USER.image))
        }
        UtilImage.redondearFoto(imgMyprofile)
    }
}

