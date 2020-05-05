package zelgius.com.atmirror.mobile.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.databinding.DialogAddGroupBinding
import zelgius.com.atmirror.mobile.databinding.DialogAddSwitchBinding
import zelgius.com.atmirror.mobile.text
import zelgius.com.atmirror.mobile.viewModel.EditViewModel
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.atmirror.shared.repository.State
import zelgius.com.atmirror.shared.viewModel.PhoneNetworkViewModel
import zelgius.com.utils.ViewModelHelper
import zelgius.com.utils.observe
import zelgius.com.utils.setListeners

class AddGroupDialog : DialogFragment() {
    private var _binding: DialogAddGroupBinding? = null
    private val binding
        get() = _binding!!

    var listener: (String) -> Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddGroupBinding.inflate(LayoutInflater.from(requireActivity()))

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_group)
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
            .setListeners ({
                if (binding.name.text.isNullOrEmpty()) {
                    binding.name.error = getString(R.string.field_requied)
                    false
                } else {
                    listener(binding.name.text!!)
                    true
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}