package android.ricardoflor.turistdroid.activities.ui.mySites

import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

class MySitesFragment : Fragment() {

    private lateinit var mySitesViewModel: MySitesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mySitesViewModel =
            ViewModelProviders.of(this).get(MySitesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_my_sites, container, false)
//        val textView: TextView = root.findViewById(R.id.text_home)
//        mySitesViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }
}