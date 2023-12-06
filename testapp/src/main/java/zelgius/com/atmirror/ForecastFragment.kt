package zelgius.com.atmirror


import androidx.fragment.app.Fragment
import zelgius.com.atmirror.things.viewModels.MainViewModel
import zelgius.com.atmirror.shared.SharedForecastFragment

/**
 * A simple [Fragment] subclass.
 */
class ForecastFragment : SharedForecastFragment() {

    override val viewModel by lazy { zelgius.com.utils.ViewModelHelper.create<MainViewModel>(requireActivity()) }


}
