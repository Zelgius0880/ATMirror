package zelgius.com.lights.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert
import org.junit.Before
import org.junit.Test


internal class HueServiceTest {
    private val client = OkHttpClient()

    @Before
    fun initialize() {
        HueService.name = "QuickHue"
        HueService.test = true
        HueService.ip = "127.0.01"
    }

    @Test
    fun getIp() {
        runBlocking {
            Assert.assertNotNull(HueService.getIp())
        }
    }

    @Test
    fun connect() {
        runBlocking {
            HueService.connect().apply {
                Assert.assertNotNull(this)
                Assert.assertEquals(1, this?.type)
            } // new user, not connected yet and error with code 1
            Assert.assertNotNull(HueService.ip)
        }
    }

    @Test
    fun register() {
        runBlocking {

            val request = Request.Builder()
                .url("http://127.0.0.1:3000/linkbutton")
                .build()

            withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            HueService.register().apply {
                Assert.assertNull(this)
                println(HueService.userName)
            }
        }
    }

    @Test
    fun getLightList() {
        runBlocking {
            register()

            Assert.assertTrue(HueService.getLightList().apply {
                print(this)
            }.isNotEmpty())
        }
    }


    @Test
    fun setLightState() {
        runBlocking {
            register()

            HueService.setLightStatus("1", HueLightState(true, null, 0, 0))
                .apply {
                    Assert.assertTrue(isNotEmpty())
                    forEach {
                        println(it.success)
                        Assert.assertNotNull(it.success)
                    }
                }
        }
    }


    @Test
    fun turnOnLight() {
        runBlocking {
            register()

            HueService.turnOnLight("1",true)
                .apply {
                    Assert.assertNotNull(this)
                    Assert.assertNotNull(this?.success)
                    println(this?.success)
                    Assert.assertTrue(this?.success?.entries?.first()?.value?.toBoolean() == true)
                }

            HueService.turnOnLight("1",false)
                .apply {
                    Assert.assertNotNull(this)
                    Assert.assertNotNull(this?.success)
                    println(this?.success)
                    Assert.assertTrue(this?.success?.entries?.first()?.value?.toBoolean() == false)
                }
        }
    }
}