package zelgius.com.atmirror


import android.os.Bundle
import androidx.fragment.app.Fragment
import zelgius.com.atmirror.viewModels.MainViewModel
import zelgius.com.shared.SharedTemperatureFragment
import zelgius.com.utils.ViewModelHelper


/**
 * A simple [Fragment] subclass.
 * Use the [SharedTemperatureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TemperatureFragment : SharedTemperatureFragment() {


    override val viewModel by lazy { zelgius.com.utils.ViewModelHelper.create<MainViewModel>(requireActivity()) }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SharedTemperatureFragment.
         */
        @JvmStatic
        fun newInstance() =
            TemperatureFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
