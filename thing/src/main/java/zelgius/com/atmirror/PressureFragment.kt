package zelgius.com.atmirror


import android.os.Bundle
import androidx.fragment.app.Fragment
import zelgius.com.atmirror.viewModels.MainViewModel
import zelgius.com.shared.SharedPressureFragment
import zelgius.com.utils.ViewModelHelper

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [SharedPressureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PressureFragment : SharedPressureFragment() {


    override val viewModel by lazy { zelgius.com.utils.ViewModelHelper.create<MainViewModel>(requireActivity()) }


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
