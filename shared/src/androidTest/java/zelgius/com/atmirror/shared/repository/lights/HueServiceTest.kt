package zelgius.com.atmirror.shared.repository.lights

import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.lights.repository.ILight
import java.util.*


@RunWith(JUnit4::class)
class HueServiceTest {
    //Please use the hue-bridge-simulator for the testing. Using the real bridge will add un necessary users

    private val repository = HueService

    @Before
    fun setUp() {

        runBlocking {
            withContext(Dispatchers.IO) {
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                    .build().newCall(
                        Request.Builder()
                    .url(" http://10.0.2.2:3000/linkbutton")
                    .build()).execute().apply {
                        println(body?.string())
                    }
            }
            for(i in (1..3)){
                if(repository.connect(name) != null){
                    repository.register(name)
                    delay(1000)
                } else break
            }

            assertNull(repository.connect(name))
        }
    }

    private val name by lazy { UUID.randomUUID().toString() }

    @Test
    fun getLightList() {
        runBlocking {
            assertFalse(repository.getLightList(name).isNullOrEmpty())
        }
    }


    @Test
    fun getLightState() {
        runBlocking {
            assertTrue(repository.getLightState(name, "1") != null)
        }
    }


    @Test
    fun setLightState_whenOn(){
        val light = Light(
            name = "Test",
            productName = "Test",
            id = "1",
            uid = "dsg;jhfkl",
            state = Light.State.OFF,
            type = ILight.Type.HUE
        )
        runBlocking {
            assertNotNull(repository.setLightState(light = light, name = name, state = Light.State.ON))
            assertTrue(repository.getLightState(name, light.id)?.on == true)
        }
    }


    @Test
    fun setLightState_whenOff(){
        val light = Light(
            name = "Test",
            productName = "Test",
            id = "1",
            uid = "dsg;jhfkl",
            state = Light.State.ON,
            type = ILight.Type.HUE
        )
        runBlocking {
            assertNotNull(repository.setLightState(light = light, name = name, state = Light.State.OFF))
            assertTrue(repository.getLightState(name, light.id)?.on == false)
        }
    }


    @Test
    fun setLightState_whenToggle(){
        val light = Light(
            name = "Test",
            productName = "Test",
            id = "1",
            uid = "dsg;jhfkl",
            state = Light.State.OFF,
            type = ILight.Type.HUE
        )
        runBlocking {
            val state =  repository.getLightState(name, light.id)?.on == true

            assertNotNull(repository.setLightState(light = light, name = name, state = Light.State.TOGGLE))
            with(repository.getLightState(name, light.id)?.on) {
                assertNotNull(this)
                assertTrue(state != this)
            }

            assertNotNull(repository.setLightState(light = light, name = name, state = Light.State.TOGGLE))
            with(repository.getLightState(name, light.id)?.on) {
                assertNotNull(this)
                assertTrue(state == this)
            }
        }
    }
}