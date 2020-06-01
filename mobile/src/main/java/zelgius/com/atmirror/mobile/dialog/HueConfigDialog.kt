package zelgius.com.atmirror.mobile.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zelgius.livedataextensions.observe
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.databinding.DialogHueConfigBinding
import zelgius.com.atmirror.mobile.viewModel.HueViewModel
import zelgius.com.utils.ViewModelHelper
import zelgius.com.view_helper_extensions.applyOn
import zelgius.com.view_helper_extensions.context
import zelgius.com.view_helper_extensions.snackBar

class HueConfigDialog : DialogFragment() {
    private var _binding: DialogHueConfigBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel by lazy {
        ViewModelHelper.create<HueViewModel>(
            this,
            requireActivity().application
        )
    }


    val ctx by lazy { binding.context }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogHueConfigBinding.inflate(LayoutInflater.from(requireActivity()))

        setWaitingState()
        checkStatus()

        binding.check.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.check.isEnabled = false
            viewModel.register().observe(this){
                binding.progressBar.visibility = View.GONE
                binding.check.isEnabled = true
                if(!it) snackBar(v = parentFragment?.view, text = R.string.register_failed)
                else {
                    setWaitingState()
                    checkStatus()
                }
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.configure_hue)
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    private fun checkStatus() {
        viewModel.checkRegistering().observe(this){
            if(!it){
                setRegisteringState()
                binding.title.setText(R.string.hue_not_configured)
            } else
                setOkState()
        }
    }

    private fun setRegisteringState() {
        with(binding) {
            View.GONE.applyOn(
                progressBar,
                ok
            )

            View.VISIBLE.applyOn(
                hueBridge,
                hueBridgeButton,
                comment,
                check,
                title,
                shapeRipple
            )
        }
    }

    private fun setWaitingState() {
        with(binding) {
            progressBar.visibility = View.VISIBLE

            View.GONE.applyOn(
                shapeRipple,
                comment,
                check,
                title.apply{
                    setTextColor(ctx.getColor(R.color.colorKo))
                }
            )
        }
    }

    private fun setOkState() {
        with(binding) {
            View.GONE.applyOn(
                hueBridge,
                hueBridgeButton,
                comment,
                check,
                title,
                shapeRipple,
                progressBar
            )

            View.VISIBLE.applyOn(
                title.apply {
                    setTextColor(context.getColor(R.color.colorOk))
                    setText(R.string.hue_all_devices_registered)
                },
                ok
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

