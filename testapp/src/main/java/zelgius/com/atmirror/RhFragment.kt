package zelgius.com.atmirror


import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import zelgius.com.atmirror.viewModels.MainViewModel
import zelgius.com.atmirror.shared.SharedPressureFragment
import zelgius.com.atmirror.shared.SharedRhFragment
import zelgius.com.atmirror.shared.viewModels.SharedMainViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [SharedRhFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RhFragment : SharedRhFragment() {

    override val viewModel by lazy { ViewModelProviders.of(activity!!).get<MainViewModel>() }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SharedTemperatureFragment.
         */
        @JvmStatic
        fun newInstance() =
            RhFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
