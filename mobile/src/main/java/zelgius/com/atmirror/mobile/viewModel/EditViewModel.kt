package zelgius.com.atmirror.mobile.viewModel

import android.app.Application
import androidx.annotation.BoolRes
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import kotlinx.coroutines.launch
import zelgius.com.atmirror.shared.entity.Group
import zelgius.com.atmirror.shared.entity.GroupItem
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.atmirror.shared.repository.FirebaseRepository
import zelgius.com.atmirror.shared.repository.FirestoreGroupItemMapper
import zelgius.com.atmirror.shared.repository.GroupRepository

class EditViewModel(val app: Application) : AndroidViewModel(app) {
    private val groupRepository = GroupRepository()

    private var _group = MutableLiveData<Group>()
    val group: LiveData<Group>
        get() = _group

    val editingGroup
        get() = group.value

    val modifiedItems = mutableListOf<GroupItem>()

    private var _progress = MutableLiveData(false)
    val progress: LiveData<Boolean>
        get() = _progress

    fun getItems() =
        FirestorePagingOptions.Builder<GroupItem>()
            .setQuery(
                groupRepository.getItemsQuery(_group.value!!),
                PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setPrefetchDistance(10)
                    .setPageSize(20)
                    .build()
            ) {
                FirestoreGroupItemMapper.map(it)
            }
            .build()

    fun setGroup(group: Group) {
        _group.value = group
    }

    fun save(group: Group): LiveData<Boolean> {
        _progress.value = true
        val liveData = MutableLiveData<Boolean>()

        viewModelScope.launch {
            val copy = group.copy(items = listOf())
            groupRepository.createOrUpdate(copy)
            liveData.value = true
            this@EditViewModel.setGroup(copy)
            _progress.value = false
        }

        return liveData
    }

    fun delete(item: GroupItem): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        viewModelScope.launch {
            groupRepository.delete(
                when (item) {
                    is Switch -> item.copy(group = editingGroup)
                    is Light -> item.copy(group = editingGroup)
                    else -> error("Should not be there")
                }
            )
            liveData.postValue(true)
        }

        return liveData
    }

    fun save(item: GroupItem, update: Boolean = false): LiveData<Boolean> {
        //_progress.value = true
        val liveData = MutableLiveData<Boolean>()

        viewModelScope.launch {
            try {
                if (item is Light)
                    groupRepository.createOrUpdate(item.copy(group = editingGroup), !update)
                else
                    groupRepository.createOrUpdate(
                        (item as Switch).copy(group = editingGroup),
                        true
                    )

                liveData.value = true
                //_progress.value = false
            } catch (e: FirebaseRepository.AlreadyExistsException) {
                e.printStackTrace()
                liveData.value = false
            }
        }

        return liveData
    }
}