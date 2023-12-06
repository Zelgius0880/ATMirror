package com.smarttoolfactory.colorpicker.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttoolfactory.colorpicker.model.ColorModel
import com.smarttoolfactory.colorpicker.model.CompositeColor
import com.smarttoolfactory.extendedcolors.util.*

@Composable
fun ColorDisplayExposedSelectionMenu(compositeColor: CompositeColor, colorModel: ColorModel) {

    val options = listOf("HEX", "HSV", "HSL")
    var index by remember { mutableStateOf(0) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        ExposedSelectionMenu(
            index = index,
            options = options,
            onSelected = {
                index = it
            }
        )
    }
}

/**
 * Selection menu that displays Color's components in Hex, HSL, or HSL
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorDisplayExposedSelectionMenu(
    color: Color,
    colorModel: ColorModel,
    showAlpha: Boolean = true,
    onColorModelChange: (ColorModel) -> Unit
) {


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
            .padding(2.dp)
    ) {

        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.Start
        ) {

            when (colorModel) {
                ColorModel.RGB -> {
                    ColorText(
                        title = "R",
                        value = color.red.fractionToRGBString(),
                        modifier = Modifier.weight(1f)
                    )
                    ColorText(
                        title = "G", value = color.green.fractionToRGBString(),
                        modifier = Modifier.weight(1f)
                    )
                    ColorText(
                        title = "B", value = color.blue.fractionToRGBString(),
                        modifier = Modifier.weight(1f)
                    )

                    if (showAlpha)
                        ColorText(
                            title = "A", value = "${color.alpha.fractionToPercent()}%",
                            modifier = Modifier.weight(1f)
                        )
                }

                ColorModel.HSV -> {
                    val hsvArray = ColorUtil.colorToHSV(color)
                    ColorText(
                        title = "H", value = "${hsvArray[0].round()}°",
                        modifier = Modifier.weight(1f)
                    )
                    ColorText(
                        title = "S", value = "${hsvArray[1].fractionToPercent()}%",
                        modifier = Modifier.weight(1f)
                    )
                    ColorText(
                        title = "V", value = "${hsvArray[2].fractionToPercent()}%",
                        modifier = Modifier.weight(1f)
                    )
                    if (showAlpha)
                        ColorText(
                            title = "A", value = "${color.alpha.fractionToPercent()}%",
                            modifier = Modifier.weight(1f)
                        )
                }

                ColorModel.HSL -> {
                    val hslArray = ColorUtil.colorToHSL(color)
                    ColorText(
                        title = "H", value = "${hslArray[0].round()}°",
                        modifier = Modifier.weight(1f)
                    )
                    ColorText(
                        title = "S", value = "${hslArray[1].fractionToPercent()}%",
                        modifier = Modifier.weight(1f)
                    )
                    ColorText(
                        title = "L", value = "${hslArray[2].fractionToPercent()}%",
                        modifier = Modifier.weight(1f)
                    )
                    if (showAlpha)
                        ColorText(
                            title = "A", value = "${color.alpha.fractionToPercent()}%",
                            modifier = Modifier.weight(1f)
                        )
                }
            }
        }
    }
}

@Composable
private fun ColorText(modifier: Modifier = Modifier, title: String, value: String) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.substring(0, 1),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp

        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 14.sp)
    }
}