package android.ricardoflor.turistdroid.activities.ui.closesession

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.LoginActivity
import android.ricardoflor.turistdroid.utils.UtilSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CloseSessionFragment : Fragment() {
    //autenticador
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        logout()
        return inflater.inflate(R.layout.fragment_my_sites, container, false)
    }

    /**
     * Funcion para Cerrar Sesion
     */
    private fun logout() {
        auth.signOut()
        val prefs = context!!.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE).edit()
        prefs.clear()
        startActivity(Intent(context, LoginActivity::class.java))
        activity!!.finish()
    }
}