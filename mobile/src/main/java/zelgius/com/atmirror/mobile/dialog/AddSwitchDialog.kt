package zelgius.com.atmirror.mobile.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.databinding.DialogAddSwitchBinding
import zelgius.com.atmirror.mobile.viewModel.SwitchViewModel
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.atmirror.shared.entity.State
import zelgius.com.dialogextensions.setListeners
import zelgius.com.utils.ViewModelHelper
import zelgius.com.view_helper_extensions.text

class AddSwitchDialog : DialogFragment() {
    private var _binding: DialogAddSwitchBinding? = null
    private val binding
        get() = _binding!!

    var listener: (Switch) -> Unit = {}
    var switch = Switch()
    private val viewModel by lazy {
        ViewModelHelper.create<SwitchViewModel>(
            this,
            requireActivity().application
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddSwitchBinding.inflate(LayoutInflater.from(requireActivity()))

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.status.observe(viewLifecycleOwner) {
            binding.status.text = when (it) {
                State.NOT_WORKING -> {
                    viewModel.startDiscovery()
                    getString(R.string.waiting_mirror)
                }
                State.DISCOVERING -> getString(R.string.press_on_switch)
            }
        }

        viewModel.switch.observe(viewLifecycleOwner) {
            if(it == null) {
                binding.name.text = ""
                binding.foundSwitch.text = getString(R.string.nothing_to_display)
            } else {
                switch = it
                if (binding.name.text == binding.foundSwitch.text.toString() || binding.name.text?.isEmpty() == true)
                    binding.name.text = getString(R.string.switch_name_format, switch.uid)

                binding.foundSwitch.text = getString(R.string.switch_name_format, switch.uid)

                viewModel.getGroupFromSwitch(it.uid).observe(viewLifecycleOwner){ groups ->
                    binding.error.isVisible = groups.isNotEmpty()
                    binding.error.text = resources.getQuantityString(R.plurals.switch_already_present, groups.size, groups.size)
                }
            }
        }
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