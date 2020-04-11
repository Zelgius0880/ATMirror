package zelgius.com.atmirror.mobile.viewModel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.toLiveData
import zelgius.com.atmirror.shared.repository.GroupRepository


class HomeViewModel(val app: Application) : AndroidViewModel(app) {
    private val groupRepository = GroupRepository()

    fun getGroups() =
        groupRepository.getFlattedGroupDataSource().
                toLiveData(50)
}