package zelgius.com.atmirror.mobile.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import zelgius.com.atmirror.shared.repository.GroupRepository
import zelgius.com.atmirror.shared.viewModel.PhoneNetworkViewModel

class SwitchViewModel(app: Application) : PhoneNetworkViewModel(app) {

    private val groupRepository = GroupRepository()
    fun getGroupFromSwitch(switchUid: String) = liveData {  emit(groupRepository.getGroupFromSwitch(switchUid))}
}