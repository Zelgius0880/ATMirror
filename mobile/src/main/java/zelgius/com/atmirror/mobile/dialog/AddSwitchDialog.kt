package zelgius.com.atmirror.mobile.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.databinding.DialogAddSwitchBinding
import zelgius.com.atmirror.mobile.text
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.atmirror.shared.repository.State
import zelgius.com.atmirror.shared.viewModel.PhoneNetworkViewModel
import zelgius.com.utils.ViewModelHelper
import zelgius.com.utils.observe
import zelgius.com.utils.setListeners

class AddSwitchDialog : DialogFragment() {
    private var _binding: DialogAddSwitchBinding? = null
    private val binding
        get() = _binding!!

    var listener: (Switch) -> Unit = {}
    var switch = Switch()
    private val viewModel by lazy {
        ViewModelHelper.create<PhoneNetworkViewModel>(
            this,
            requireActivity().application
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddSwitchBinding.inflate(LayoutInflater.from(requireActivity()))

        viewModel.status.observe(this) {
            binding.status.text = when (it) {
                State.NOT_WORKING -> {
                    viewModel.startDiscovery()
                    getString(R.string.waiting_mirror)
                }
                State.DISCOVERING -> getString(R.string.press_on_switch)
            }
        }

        viewModel.switch.observe(this) {
            if(it == null) {
                binding.name.text = ""
                binding.foundSwitch.text = getString(R.string.nothing_to_display)
            } else {
                switch = it
                if (binding.name.text == binding.foundSwitch.text.toString() || binding.name.text?.isEmpty() == true)
                    binding.name.text = getString(R.string.switch_name_format, switch.uid)

                binding.foundSwitch.text = getString(R.string.switch_name_format, switch.uid)
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_switch)
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
            .setListeners ({
                if (binding.name.text.isNullOrEmpty()) {
                    binding.name.error = getString(R.string.field_requied)
                    false
                } else {
                    switch.name = binding.name.text!!
                    listener(switch)
                    true
                }
            })
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}