package zelgius.com.atmirror.mobile.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.databinding.DialogLifxConfigBinding
import zelgius.com.atmirror.mobile.viewModel.LightViewModel
import zelgius.com.dialogextensions.setListeners
import zelgius.com.utils.ViewModelHelper
import zelgius.com.view_helper_extensions.text

class LIFXConfigDialog : DialogFragment() {
    private var _binding: DialogLifxConfigBinding? = null
    private val binding
        get() = _binding!!

    var listener: (String) -> Unit = {}
    private val viewModel by lazy { ViewModelHelper.create<LightViewModel>(this, requireActivity().application) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogLifxConfigBinding.inflate(LayoutInflater.from(requireActivity()))
        viewModel.lifxKey.observe(this){
            binding.key.text = it
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.lifx_key)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
            .setListeners  ({
                if (binding.key.text.isNullOrEmpty()) {
                    binding.key.error = getString(R.string.field_requied)
                    false
                } else {
                    listener(binding.key.text!!)
                    true
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}