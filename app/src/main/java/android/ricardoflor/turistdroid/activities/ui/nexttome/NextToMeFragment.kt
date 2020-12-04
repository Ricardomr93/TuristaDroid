package android.ricardoflor.turistdroid.activities.ui.nexttome

import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

class NextToMeFragment : Fragment() {

    private lateinit var nextToMeViewModel: NextToMeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        nextToMeViewModel =
            ViewModelProviders.of(this).get(NextToMeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_next_to_me, container, false)
//        val textView: TextView = root.findViewById(R.id.text_gallery)
//        nextToMeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }
}