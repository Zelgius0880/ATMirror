package zelgius.com.shared

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import zelgius.com.shared.protocol.CurrentStatus
import zelgius.com.shared.viewModel.MirrorNetworkViewModel
import zelgius.com.shared.viewModel.PhoneNetworkViewModel
import zelgius.com.utils.toHexString
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class NetworkCommunicationTest {
    @get:Rule
    val mockitoRule = MockitoJUnit.rule()!!

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context by lazy { ApplicationProvider.getApplicationContext<Application>()!! }

    private val phoneViewModel =
        PhoneNetworkViewModel(context)
    private val mirrorViewModel =
        MirrorNetworkViewModel(context)

    @Test
    fun startDiscovery() {
        var latch = CountDownLatch(2)
        phoneViewModel.startDiscovery().observeOnce {
            assertTrue(it)
            latch.countDown()
        }

        mirrorViewModel.status.observeOnce {
            if(it == CurrentStatus.Status.SWITCH_DISCOVERING)
                latch.countDown()
        }

        latch.await(1L, TimeUnit.SECONDS)
        assert(latch.count == 0L)

        latch = CountDownLatch(1)
        phoneViewModel.getCurrentStatus()
        phoneViewModel.status.observeOnce {
            if(it == CurrentStatus.Status.NOT_WORKING)
                latch.countDown()
        }

        latch.await(1L, TimeUnit.SECONDS)
        assert(latch.count == 0L)
    }


    @Test
    fun stopDiscovery() {
        //starting discovery in order to have a consistent state before stopping it
        startDiscovery()

        var latch = CountDownLatch(2)
        phoneViewModel.stopDiscovery().observeOnce {
            assertTrue(it)
            latch.countDown()
        }

        mirrorViewModel.status.observeOnce {
            if(it == CurrentStatus.Status.NOT_WORKING)
                latch.countDown()
        }

        latch.await(1L, TimeUnit.SECONDS)

        latch = CountDownLatch(1)
        phoneViewModel.getCurrentStatus()
        phoneViewModel.status.observeOnce {
            if(it == CurrentStatus.Status.NOT_WORKING)
                latch.countDown()
        }

        latch.await(1L, TimeUnit.SECONDS)
        assert(latch.count == 0L)
    }

    @Test
    fun newSwitchDetected() {
        val switch = byteArrayOf(0x01,0x02,0x03,0x04,0x05)

        mirrorViewModel.sendSwitch(switch)

        val latch = CountDownLatch(1)
        latch.await(1L, TimeUnit.SECONDS)
        phoneViewModel.switches.observeOnce {
            if(it.isNotEmpty()) {
                assertEquals(it.first().uid, switch.toHexString())
                latch.countDown()
            }
        }

        latch.await(1L, TimeUnit.SECONDS)
        assert(latch.count == 0L)
    }


    private fun <T> wait(liveData: LiveData<T>, work: (T) -> Unit) {
        val latch = CountDownLatch(1)

        liveData.observeOnce {
            work(it)
            latch.countDown()
        }

        latch.await(10, TimeUnit.SECONDS)
        assertTrue(latch.count == 0L)
    }
}
