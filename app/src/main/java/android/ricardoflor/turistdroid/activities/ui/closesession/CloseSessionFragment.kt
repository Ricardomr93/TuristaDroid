package android.ricardoflor.turistdroid.activities.ui.closesession

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.LoginActivity
import android.ricardoflor.turistdroid.utils.IOnBackPressed
import android.ricardoflor.turistdroid.utils.UtilSession

class CloseSessionFragment : Fragment(), IOnBackPressed {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        logout()
        return inflater.inflate(R.layout.fragment_my_sites, container, false)
    }

    /**
     * Funcion para Cerrar Sesion
     */
    private fun logout() {
        UtilSession.closeSession()
        startActivity(Intent(context, LoginActivity::class.java))
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}