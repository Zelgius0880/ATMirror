package zelgius.com.atmirror


import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import zelgius.com.atmirror.viewModels.MainViewModel
import zelgius.com.shared.SharedForecastFragment
import zelgius.com.shared.viewModels.SharedMainViewModel

/**
 * A simple [Fragment] subclass.
 */
class ForecastFragment : SharedForecastFragment() {

    override val viewModel by lazy { ViewModelProviders.of(activity!!).get<MainViewModel>() }

}
