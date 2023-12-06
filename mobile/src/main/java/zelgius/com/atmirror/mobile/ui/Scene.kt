package zelgius.com.atmirror.mobile.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.green
import com.smarttoolfactory.colorpicker.picker.ColorPickerRectSaturationLightnessHSL
import kotlinx.coroutines.launch
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.convert
import zelgius.com.atmirror.mobile.viewModel.EditViewModel
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.lights.repository.ILight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneGroup(editViewModel: EditViewModel) {
    val scenes by editViewModel.scenes.observeAsState(emptyMap())
    val coroutineScope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true) {
        it != SheetValue.PartiallyExpanded
    }
    var selectedScene: Pair<Color, List<Light>>? by remember {
        mutableStateOf(null)
    }

    SceneGroupView(scenes = scenes,
        onClick = {
            selectedScene = it
            showBottomSheet = true
        },
        onAddClicked = {
            selectedScene = Color.White to emptyList()
            showBottomSheet = true
        }
    )

    if (showBottomSheet && selectedScene != null) {
        selectedScene?.let { (color, lights) ->
            ColorPickerBottomSheet(lights = lights.map { true to it } + (scenes[null]?.map { false to it }
                ?: emptyList()),
                initialColor = color,
                sheetState,
                onColorSelected = { c, selectedLights ->
                    editViewModel.previewColor(hsl = c.toHsl(), selectedLights)
                },
                onDismissRequest = {
                    showBottomSheet = false
                },
                onValidate = { c, selected ->
                    selectedScene?.let {(_, initial) ->
                        editViewModel.setScene(
                            c.toHsl(),
                            initial,
                            selected.filter { it.first }.map { it.second },
                        )
                    }

                    showBottomSheet = false
                })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SceneGroupView(
    scenes: Map<Color?, List<Light>>,
    onClick: (Pair<Color, List<Light>>) -> Unit = {},
    onAddClicked: () -> Unit = {}
) {
    FlowRow(modifier = Modifier.padding(8.dp)) {
        scenes.forEach { (color, lights) ->
            if (color != null) {
                ChipItem(
                    text = pluralStringResource(
                        id = R.plurals.lights_plurals,
                        lights.size,
                        lights.size,
                    ), background = color
                ) {
                    onClick(color to lights)
                }
            }
        }

        ElevatedAssistChip(
            onClick = onAddClicked,
            label = { Text(stringResource(id = R.string.add_group)) },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Add,
                    ""
                )
            })
    }
}



private fun Color.toHsl() = with(this.toArgb().let {
    android.graphics.Color.argb(
        alpha, red, green, blue
    )
}) {
    val hsl = floatArrayOf(0f, 0f, 0f)
    ColorUtils.RGBToHSL(
        android.graphics.Color.red(this),
        android.graphics.Color.green(this),
        android.graphics.Color.blue(this),
        hsl
    )

    hsl
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipItem(
    text: String,
    background: Color = MaterialTheme.colorScheme.primaryContainer,
    selected: Boolean = true,
    onClick: () -> Unit = {}
) {
    InputChip(
        modifier = Modifier.padding(end = 4.dp),
        onClick = onClick,
        selected = selected,
        leadingIcon = {},
        colors = InputChipDefaults.inputChipColors(selectedContainerColor = background),
        border = InputChipDefaults.inputChipBorder(
            borderWidth = 1.dp,
            borderColor = Color.Black,
            selectedBorderColor = Color.Black
        ),
        label = {
            Text(text)
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerBottomSheet(
    lights: List<Pair<Boolean, Light>>,
    initialColor: Color,
    sheetState: SheetState,
    onValidate: (Color, List<Pair<Boolean, Light>>) -> Unit = { _, _ -> },
    onColorSelected: (Color, List<Light>) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        LightColorSelector(
            lights,
            initialColor,
            onColorSelected = onColorSelected,
            onValidate = onValidate
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun LightColorSelector(
    lights: List<Pair<Boolean, Light>>,
    initialColor: Color,
    onValidate: (Color, List<Pair<Boolean, Light>>) -> Unit = { _, _ -> },
    onColorSelected: (Color, List<Light>) -> Unit
) {
    var selectedColor by remember {
        mutableStateOf(initialColor)
    }

    var selectedLights by remember {
        mutableStateOf(lights)
    }

    Column(Modifier.verticalScroll(rememberScrollState())) {
        FlowRow(modifier = Modifier.padding(8.dp)) {
            selectedLights.forEach { (selected, light) ->
                ChipItem(text = light.name, selected = selected) {
                    selectedLights = selectedLights.map { (b, l) ->
                        if (l == light) !b to l
                        else b to l
                    }
                }
            }
        }
        ColorPickerRectSaturationLightnessHSL(
            modifier = Modifier.padding(horizontal = 16.dp),
            initialColor = initialColor,
            showAlpha = false,
            onColorChange = { color, _ ->
                selectedColor = color
                onColorSelected(
                    color,
                    selectedLights.mapNotNull { if (it.first) it.second else null })
            }
        )

        Button(modifier = Modifier
            .align(Alignment.End)
            .padding(end = 8.dp, bottom = 8.dp), onClick = {
            onValidate(selectedColor, selectedLights)
        }) {
            Text(text = stringResource(id = R.string.save))
        }
    }
}

@Preview
@Composable
fun SceneGroupViewPreview() {
    MaterialTheme {
        SceneGroupView(
            scenes = mapOf(
               Color.Yellow to
                        listOf("Lamp 1", "Lamp 2", "Lamp 3", "Lamp 4").map {
                            Light(
                                name = it,
                                uid = "",
                                type = ILight.Type.HUE
                            )
                        },
                Color.Red to
                        listOf("Lamp 1", "Lamp 2", "Lamp 3", "Lamp 4").map {
                            Light(
                                name = it,
                                uid = "",
                                type = ILight.Type.HUE
                            )
                        },
                Color.Blue to
                        listOf("Lamp 1", "Lamp 2", "Lamp 3", "Lamp 4").map {
                            Light(
                                name = it,
                                uid = "",
                                type = ILight.Type.HUE
                            )
                        }
            ))
    }
}

@Preview
@Composable
fun LightColorSelectorPreview() {
    MaterialTheme {
        LightColorSelector(
            listOf("Lamp 1", "" + "Lamp 2", "Lamp 3", "Lamp 4").mapIndexed { i, s ->
                (i % 2 == 0) to Light(
                    name = s,
                    uid = "",
                    type = ILight.Type.HUE
                )
            }, initialColor = Color.Green
        ) { _, _ ->

        }
    }
}