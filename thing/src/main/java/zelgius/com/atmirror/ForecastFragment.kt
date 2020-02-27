package zelgius.com.atmirror


import androidx.fragment.app.Fragment
import zelgius.com.atmirror.viewModels.MainViewModel
import zelgius.com.shared.SharedForecastFragment
import zelgius.com.utils.ViewModelHelper

/**
 * A simple [Fragment] subclass.
 */
class ForecastFragment : SharedForecastFragment() {

    override val viewModel by lazy { zelgius.com.utils.ViewModelHelper.create<MainViewModel>(requireActivity()) }


}
