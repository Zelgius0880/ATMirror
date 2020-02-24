package zelgius.com.atmirror

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import zelgius.com.atmirror.viewModels.MainViewModel
import zelgius.com.shared.SharedMainActivity
import zelgius.com.shared.viewModels.SharedMainViewModel


private val TAG = MainActivity::class.java.simpleName

class MainActivity : SharedMainActivity() {
    override val viewModel by lazy { ViewModelProviders.of(this).get<MainViewModel>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
