package zelgius.com.shared


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import kotlinx.android.synthetic.main.fragment_rh.*
import zelgius.com.shared.viewModels.SharedMainViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [SharedRhFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
abstract class SharedRhFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }
    protected abstract val viewModel : SharedMainViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rh, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.sht21Record.observe(this, Observer {
            rh.text = String.format("%.0f %%", it.humidity)
        })
    }
}
