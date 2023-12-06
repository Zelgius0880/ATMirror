package zelgius.com.atmirror.repositories

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import zelgius.com.atmirror.shared.repository.MeasureType
import zelgius.com.atmirror.shared.repository.NetatmoRepository

class NetatmoRepositoryTest {
    private val repository by lazy { NetatmoRepository(true) }

    @Test
    fun requestToken() {
        runBlocking {
            with(repository.requestToken()) {
                assertTrue(isSuccessful)

                body()?.let {
                    assertNotNull(it.accessToken)
                    assertNotNull(it.refreshToken)
                    assertTrue(it.isValid)
                }
            }
        }
    }

    @Test
    fun refreshToken() {
        runBlocking {
            repository.withToken {
                with(repository.refreshToken()) {
                    assertTrue(isSuccessful)

                    body()?.let {
                        assertNotNull(it.accessToken)
                        assertNotNull(it.refreshToken)
                        assertTrue(it.isValid)
                    }
                }
            }
        }
    }

    @Test
    fun getTemperatureMeasure() {
        runBlocking {
            with(repository.getMeasure(module = false, MeasureType.Temperature, MeasureType.Pressure)) {
                assertNotNull(this)
                assertFalse(this.isEmpty())
            }
        }
    }
}