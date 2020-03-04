package zelgius.com.atmirror

import android.os.Bundle
import zelgius.com.atmirror.viewModels.MainViewModel
import zelgius.com.atmirror.shared.SharedMainActivity
import zelgius.com.atmirror.shared.viewModels.SharedMainViewModel
import zelgius.com.utils.ViewModelHelper


private val TAG = MainActivity::class.java.simpleName

class MainActivity : SharedMainActivity() {
    override val viewModel by lazy { ViewModelHelper.create<MainViewModel>(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
