package android.ricardoflor.turistdroid.activities.ui.myprofile

import android.content.Intent
import android.os.Bundle
import android.ricardoflor.turistdroid.MyApplication.Companion.SESSION
import android.ricardoflor.turistdroid.MyApplication.Companion.USER
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.LoginActivity
import android.ricardoflor.turistdroid.bd.session.SessionController
import android.ricardoflor.turistdroid.bd.user.UserController
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_my_profile.*

class MyProfileFragment : Fragment() {

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
    private fun init(){
        updateUser()
        deleteUser()
    }
    private fun updateUser(){
        btnUpdateMyprofile.setOnClickListener {
            if (anyEmpty()){
                Toast.makeText(context!!, "USUARIO ACTUALIZADO", Toast.LENGTH_SHORT).show()
            }
        }
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
        if (notEmpty(txtEmailMyProfile) && notEmpty(txtNameMyProfile) && notEmpty(txtPassMyprofile) && notEmpty(txtUserNameMyProfile)) {
            valid = false
        }
        return valid
    }
    private fun deleteUser(){
        btnUnsubMyProfile.setOnClickListener{
            UserController.deleteUser(USER.email)
            SessionController.deleteSession(SESSION)
            startActivity(Intent(context,LoginActivity::class.java))
            Toast.makeText(context!!, "USUARIO BORRADO", Toast.LENGTH_SHORT).show()
        }
    }

}

