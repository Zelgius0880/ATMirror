package zelgius.com.lights.service

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import zelgius.com.lights.repository.LIFXService

class LIFXServiceTest {

    @Before
    fun initialize(){
        LIFXService.token = "c4b422f7f5058d57044cc42ef47b438517385862f6883c983464d612ffcf4dad"

    }

    @Test
    fun testGetLightList() {
        runBlocking {
            LIFXService.getLightList().apply {
                println(this)
                Assert.assertFalse(isEmpty())
            }
        }
    }

    @Test
    fun testTurnOnLight() {
        runBlocking {
            LIFXService.turnOnLight("d073d553c5da", true).apply {
                println(this)
                Assert.assertFalse(isEmpty())
            }

            LIFXService.turnOnLight("d073d553c5da", false).apply {
                println(this)
                Assert.assertFalse(isEmpty())
            }
        }
    }
}


