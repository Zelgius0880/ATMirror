package zelgius.com.atmirror

import com.google.gson.Gson
import org.junit.Test
import zelgius.com.atmirror.entities.json.OpenWeatherMap

class OpenWeatherMapTest {

    val sample = """
    {"city":{"id":2790451,"name":"Nassogne","coord":{"lon":5.3427,"lat":50.1285},"country":"BE","population":5081,"timezone":7200},"cod":"200","message":3.7000405,"cnt":5,"list":[{"dt":1597316400,"sunrise":1597292746,"sunset":1597345289,"temp":{"day":26.01,"min":16.44,"max":26.67,"night":16.44,"eve":22.37,"morn":22.99},"feels_like":{"day":25.58,"night":16.42,"eve":23.32,"morn":24.79},"pressure":1012,"humidity":60,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":4.38,"deg":205,"clouds":89,"pop":0.8,"rain":2.27},{"dt":1597402800,"sunrise":1597379236,"sunset":1597431578,"temp":{"day":22.01,"min":15.69,"max":23,"night":15.69,"eve":19.41,"morn":16.98},"feels_like":{"day":20.87,"night":15.91,"eve":20.4,"morn":16.91},"pressure":1013,"humidity":71,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":4.75,"deg":207,"clouds":82,"pop":0.91,"rain":2.17},{"dt":1597489200,"sunrise":1597465726,"sunset":1597517866,"temp":{"day":21.55,"min":15.46,"max":22.3,"night":15.46,"eve":20.2,"morn":16.8},"feels_like":{"day":21.44,"night":16.08,"eve":22.05,"morn":17.24},"pressure":1013,"humidity":69,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":2.79,"deg":246,"clouds":87,"pop":0.2,"rain":0.57},{"dt":1597575600,"sunrise":1597552216,"sunset":1597604153,"temp":{"day":25.2,"min":16.32,"max":26.27,"night":16.32,"eve":22.3,"morn":16.69},"feels_like":{"day":24.79,"night":16.65,"eve":23.72,"morn":17.47},"pressure":1010,"humidity":50,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":2.4,"deg":203,"clouds":53,"pop":0.8,"rain":2.13},{"dt":1597662000,"sunrise":1597638706,"sunset":1597690439,"temp":{"day":21.22,"min":12.96,"max":21.22,"night":12.96,"eve":17.17,"morn":15.64},"feels_like":{"day":19.89,"night":12.01,"eve":17.44,"morn":15.65},"pressure":1010,"humidity":66,"weather":[{"id":501,"main":"Rain","description":"moderate rain","icon":"10d"}],"speed":4.01,"deg":239,"clouds":100,"pop":1,"rain":6.33}]}
"""
    @Test
    fun testParsing() {
        println(Gson().fromJson( sample, OpenWeatherMap::class.java))
    }


}