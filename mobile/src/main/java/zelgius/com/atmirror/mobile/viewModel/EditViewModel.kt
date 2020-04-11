package zelgius.com.atmirror.mobile.viewModel
import android.app.Application
import androidx.lifecycle.*
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import kotlinx.coroutines.launch
import zelgius.com.atmirror.shared.entity.Group
import zelgius.com.atmirror.shared.entity.GroupItem
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.entity.Switch
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

    fun save(item: GroupItem): LiveData<Boolean>  {
        //_progress.value = true
        val liveData = MutableLiveData<Boolean>()

        viewModelScope.launch {

            if(item is Light)
                groupRepository.createOrUpdate(item.copy(group = editingGroup))
            else
                groupRepository.createOrUpdate((item as Switch).copy(group = editingGroup))

            liveData.value = true
            //_progress.value = false
        }

        return liveData
    }
}