package zelgius.com.atmirror.mobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.coroutines.launch
import zelgius.com.atmirror.mobile.databinding.ActivityMainBinding
import zelgius.com.atmirror.shared.repository.NetatmoRepository
import zelgius.com.atmirror.shared.repository.NetworkRepository

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val navController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController
    }

    private val netatmoRepository by lazy {
        NetatmoRepository()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupActionBarWithNavController(navController)

    }

    override fun onSupportNavigateUp() =
        findNavController(R.id.nav_host_fragment).navigateUp()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val code = intent?.data?.getQueryParameter("code")
        val state = intent?.data?.getQueryParameter("state")
        if(code != null && state != null) {
            lifecycleScope.launch {
                netatmoRepository.requestAndSaveToken(code)
            }
        }
    }
}
