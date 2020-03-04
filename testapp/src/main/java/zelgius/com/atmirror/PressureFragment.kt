package zelgius.com.atmirror


import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import zelgius.com.atmirror.viewModels.MainViewModel
import zelgius.com.atmirror.shared.SharedPressureFragment
import zelgius.com.atmirror.shared.viewModels.SharedMainViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [SharedPressureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PressureFragment : SharedPressureFragment() {


    override val viewModel by lazy { ViewModelProviders.of(activity!!).get<MainViewModel>() }

    companion object {

        private val BAROMETER_RANGE_LOW = 965f
        private val BAROMETER_RANGE_HIGH = 1035f
        private val BAROMETER_RANGE_SUNNY = 1010f
        private val BAROMETER_RANGE_RAINY = 990f

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SharedPressureFragment.
         */
        @JvmStatic
        fun newInstance() =
            PressureFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
