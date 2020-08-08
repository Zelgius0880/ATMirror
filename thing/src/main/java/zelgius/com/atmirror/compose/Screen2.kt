package zelgius.com.atmirror.compose

import android.graphics.Bitmap
import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import zelgius.com.atmirror.entities.json.DarkSky

data class Screen2(val forecast: DarkSky) {
    var bitmap: Bitmap? = null


    constructor(forecast: DarkSky, bitmap: Bitmap) : this(forecast) {
        this.bitmap = bitmap
    }
}

@Composable
fun Screen2View() {
    Column(modifier = Modifier.size(300.dp, 400.dp)) {
        Box(
            gravity = ContentGravity.TopCenter,
            modifier = Modifier.fillMaxWidth(),
            paddingTop = 18.dp
        ) {
            Text("Hello")
        }
    }
}

@Composable
@Preview
private fun Preview() {

    Screen2View()
}