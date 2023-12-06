package zelgius.com.atmirror.mobile.viewModel

import android.app.Application
import androidx.annotation.BoolRes
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.*
import androidx.paging.PagedList
import androidx.paging.PagingConfig
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import kotlinx.coroutines.launch
import zelgius.com.atmirror.mobile.convert
import zelgius.com.atmirror.shared.entity.Group
import zelgius.com.atmirror.shared.entity.GroupItem
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.atmirror.shared.repository.FirebaseRepository
import zelgius.com.atmirror.shared.repository.FirestoreGroupItemMapper
import zelgius.com.atmirror.shared.repository.GroupRepository
import zelgius.com.atmirror.shared.repository.LightRepository
import zelgius.com.lights.repository.ILight
import kotlin.math.roundToInt

class EditViewModel(val app: Application) : AndroidViewModel(app) {
    private val groupRepository = GroupRepository()
    private val lightRepository = LightRepository()

    private var _group = MutableLiveData<Group>()
    val group: LiveData<Group>
        get() = _group

    val editingGroup
        get() = group.value

    val modifiedItems = mutableListOf<GroupItem>()

    private var _progress = MutableLiveData(false)
    val progress: LiveData<Boolean>
        get() = _progress

    private val _scenes = MutableLiveData<Map<Color?, List<Light>>>()
    val scenes: LiveData<Map<Color?, List<Light>>>
        get() = _scenes


    fun setGroup(group: Group) {
        _group.value = group
        _scenes.value = group.items.mapNotNull { it as? Light }
            .filter { it.type == ILight.Type.HUE }
            .groupBy {
                val hue = it.hue
                val saturation = it.saturation
                val brightness = it.brightness


                if (hue != null && saturation != null && brightness != null)
                    arrayOf(hue, saturation, brightness).hsl()
                else null
            }
    }

    private fun Array<Int>.hsl() = Color.hsl(
        hue = (0..65535).convert(this[0], (0..360)).toFloat(),
        saturation = (0..254).convert(this[1].toFloat(), (0..1)),
        lightness = (0..254).convert(this[2].toFloat(), (0..1)),
        alpha = 1f
    )

    fun getItems(lifecycleOwner: LifecycleOwner) =
        FirestorePagingOptions.Builder<GroupItem>()
            .setLifecycleOwner(lifecycleOwner)
            .setQuery(
                groupRepository.getItemsQuery(_group.value!!),
                PagingConfig(20, 10, false)
            ) {
                FirestoreGroupItemMapper.map(it)
            }
            .build()

    fun save(group: Group): LiveData<Boolean> {
        _progress.value = true
        return liveData {
            val copy = group.copy(items = listOf())
            groupRepository.createOrUpdate(copy)
            this@EditViewModel.setGroup(copy)
            _progress.value = false

            refresh()
            emit(true)
        }
    }

    fun delete(item: GroupItem) = liveData {
        groupRepository.delete(
            when (item) {
                is Switch -> item.copy(group = editingGroup)
                is Light -> item.copy(group = editingGroup)
                else -> error("Should not be there")
            }
        )
        emit(true)
    }

    fun delete(item: Group) =
        liveData {
            groupRepository.delete(item)
            emit(true)
        }

    /**
     * the LiveData will contains a list a GroupItem that could not be updated.
     * When update is true, the list should be empty, or else there is a strange problem
     * When update is false, the list should contains the list of elements that are already present in the group (and so, cannot be added)
     */
    fun save(list: List<GroupItem>, update: Boolean = false): LiveData<List<GroupItem>> {
        val liveData = MutableLiveData<List<GroupItem>>()
        val listNotSaved = mutableListOf<GroupItem>()

        viewModelScope.launch {
            list.forEach {
                if (!saveItem(it, update))
                    listNotSaved.add(it)
            }

            liveData.value = listNotSaved
        }

        return liveData
    }

    fun save(item: GroupItem, update: Boolean = false): LiveData<Boolean> {
        //_progress.value = true
        val liveData = MutableLiveData<Boolean>()

        viewModelScope.launch {
            liveData.value = saveItem(item, update)
        }

        return liveData
    }

    private suspend fun saveItem(item: GroupItem, update: Boolean = false): Boolean =
        try {
            if (item is Light)
                groupRepository.createOrUpdate(item.copy(group = editingGroup), !update)
            else
                groupRepository.createOrUpdate(
                    (item as Switch).copy(group = editingGroup),
                    true
                )
            true
            //_progress.value = false
        } catch (e: FirebaseRepository.AlreadyExistsException) {
            e.printStackTrace()
            false
        }

    fun previewColor(hsl: FloatArray, lights: List<Light>) {
        viewModelScope.launch {
            try {

                lightRepository.setState(*lights.filter { it.state == Light.State.ON || it.state == Light.State.TOGGLE }
                    .map {
                        it.copy(
                            hue = (0..360).convert(hsl[0], (0..65535)).roundToInt(),
                            saturation = (0..1).convert(hsl[1], (0..254)).roundToInt(),
                            brightness = (0..1).convert(hsl[2], (0..254)).roundToInt()
                        )
                    }.toTypedArray(), state = Light.State.ON)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setScene(hsl: FloatArray, initialLights: List<Light>, selected: List<Light>) {
        _progress.value = true
        viewModelScope.launch {
            try {
                val selectedKeys = selected.map { it.key }
                val initialKeys = initialLights.map { it.key }
                group.value?.let { group ->
                    groupRepository.createOrUpdate(group, lights = group.lights.map {
                        if (selectedKeys.contains(it.key))
                            it.copy(
                                hue = (0..360).convert(hsl[0], (0..65535)).roundToInt(),
                                saturation = (0..1).convert(hsl[1], (0..254)).roundToInt(),
                                brightness = (0..1).convert(hsl[2], (0..254)).roundToInt()
                            )
                        else if (initialKeys.contains(it.key))
                            it.copy(
                                hue = null,
                                saturation = null,
                                brightness = null,
                            )
                        else it
                    })

                    refresh()
                    _progress.value = false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _progress.value = false
            }
        }
    }

    private suspend fun refresh() {
        group.value?.let { group ->
            groupRepository.getGroup(group.key ?: "")?.let {
                setGroup(it)
            }
        }
    }

}