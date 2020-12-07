package android.ricardoflor.turistdroid.activities.ui.mySites

import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.fragment_my_sites.*

class MySitesFragment : Fragment() {

    private lateinit var mySitesViewModel: MySitesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_sites, container, false)
        init()
        btnAddSiteFloating.setOnClickListener{addSite()}
    }

    fun init() {
        addSite()
    }

    private fun addSite() {
            Log.i("sites","nuevo sitio")
            val addSites = SiteFragment()
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.add(R.id.nav_host_fragment,addSites)
            transaction.addToBackStack(null)
            transaction.commit()
    }
}