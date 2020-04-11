package zelgius.com.atmirror.mobile

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import zelgius.com.atmirror.shared.entity.Group
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.lights.repository.ILight
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val g = createSample()

        assertEquals(g.copy(), g)
    }

    private fun createSample(name: String = "Test", switches: Int = 3, lights: Int = 3) =
        Group(name).apply {

            val s = (1..switches).map {
                Switch(
                    name = "Switch $name $it",
                    uid = UUID.randomUUID().toString()
                )
            }

            val l = (1..lights).map {
                Light(
                    name = "Light $name $it",
                    uid = UUID.randomUUID().toString(),
                    type = if (it % 2 == 0) ILight.Type.HUE else ILight.Type.LIFX
                ).apply {
                }
            }

            items = listOf(*s.toTypedArray(), *l.toTypedArray())
        }


}