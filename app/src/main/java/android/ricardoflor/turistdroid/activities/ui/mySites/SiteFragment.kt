package android.ricardoflor.turistdroid.activities.ui.mySites

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.ricardoflor.turistdroid.R

class SiteFragment : Fragment() {

    companion object {
        fun newInstance() = SiteFragment()
    }

    private lateinit var viewModel: AddSiteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.site_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AddSiteViewModel::class.java)
        // TODO: Use the ViewModel
    }

}