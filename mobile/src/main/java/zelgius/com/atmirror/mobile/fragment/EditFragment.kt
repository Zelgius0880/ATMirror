package zelgius.com.atmirror.mobile.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_edit.view.*
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.adapter.EditGroupAdapter
import zelgius.com.atmirror.mobile.databinding.FragmentEditBinding
import zelgius.com.atmirror.mobile.hideKeyboard
import zelgius.com.atmirror.mobile.text
import zelgius.com.atmirror.mobile.viewModel.EditViewModel
import zelgius.com.utils.ViewModelHelper
import zelgius.com.utils.observe

class EditFragment : Fragment() {

    private lateinit var adapter: EditGroupAdapter
    private var _binding: FragmentEditBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val editViewModel by lazy { ViewModelHelper.create<EditViewModel>(requireActivity()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter =  EditGroupAdapter( editViewModel.getItems()) {
            editViewModel.save(it).observe(this@EditFragment) {
                Snackbar.make(binding.root, R.string.name_save, Snackbar.LENGTH_SHORT)
                    .show()

                //adapter.refresh()
            }
        }

        binding.recyclerView.adapter = adapter

        editViewModel.group.observe(this){
            binding.groupName.editText?.setText(it.name)
        }

        editViewModel.progress.observe(this){
            if(it) {
                binding.progressBar.visibility = View.VISIBLE
                binding.saveName.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.saveName.visibility = View.VISIBLE
            }
        }

        binding.saveName.setOnClickListener {
            hideKeyboard()
            binding.groupName.clearFocus()

            editViewModel.editingGroup?.apply {
                name = binding.groupName.text!!
                editViewModel.save(this).observe(this@EditFragment) {
                    Snackbar.make(binding.root, R.string.name_save, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.menu.menuLayouts = arrayOf(binding.addLightLayout, binding.addSwitchLayout)
        binding.menu.rotationAnimation = 45f
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_edit, menu)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment HomeFragment.
         */
        @JvmStatic
        fun newInstance() =
            EditFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}