package android.ricardoflor.turistdroid.activities.ui.nexttome

import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders

class NextToMeFragment : Fragment() {

    private lateinit var nextToMeViewModel: NextToMeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_next_to_me, container, false)
    }
}