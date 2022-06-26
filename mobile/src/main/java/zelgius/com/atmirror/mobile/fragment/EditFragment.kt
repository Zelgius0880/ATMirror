package zelgius.com.atmirror.mobile.fragment

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import com.zelgius.livedataextensions.observe
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.adapter.EditGroupAdapter
import zelgius.com.atmirror.mobile.databinding.FragmentEditBinding
import zelgius.com.atmirror.mobile.dialog.AddSwitchDialog
import zelgius.com.atmirror.mobile.viewModel.EditViewModel
import zelgius.com.atmirror.shared.entity.GroupItem
import zelgius.com.utils.ViewModelHelper
import zelgius.com.view_helper_extensions.hideKeyboard
import zelgius.com.view_helper_extensions.text

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

        adapter = EditGroupAdapter(
            editViewModel.getItems(viewLifecycleOwner),
            itemChangedListener = {
                editViewModel.save(it, true).observe(this@EditFragment) {
                    Snackbar.make(binding.root, R.string.item_saved, Snackbar.LENGTH_SHORT)
                        .show()

                    //adapter.refresh()
                }
            },
            itemRemovedListener = {
                editViewModel.delete(it).observe(viewLifecycleOwner) { _ ->
                    adapter.refresh()
                    showUndoSnackBar(it)
                }
            }
        )
        adapter.loadingStatus.observe(viewLifecycleOwner) {
            when (it) {
                is LoadState.NotLoading -> binding.progressBarList.isVisible = false
                LoadState.Loading -> binding.progressBarList.isVisible = true
                is LoadState.Error -> binding.progressBarList.isVisible = false
            }
        }

        binding.recyclerView.adapter = adapter

        editViewModel.group.observe(viewLifecycleOwner) {
            binding.groupName.editText?.setText(it.name)
        }

        editViewModel.progress.observe(viewLifecycleOwner) {
            if (it) {
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

        //binding.menu.menuLayouts = arrayOf(binding.addLightLayout, binding.addSwitchLayout)
        //binding.menu.rotationAnimation = 45f
        binding.addSwitch.setOnClickListener {
            AddSwitchDialog().apply {
                listener = {
                    editViewModel.save(it).observe(this@EditFragment) { saved ->
                        if (!saved)
                            Snackbar.make(
                                binding.root,
                                R.string.switch_already_exists,
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                        else {
                            Snackbar.make(binding.root, R.string.item_saved, Snackbar.LENGTH_SHORT)
                                .show()
                            adapter.refresh()
                        }
                    }
                }
            }.show(parentFragmentManager, "add_switch")
        }


        binding.addLight.setOnClickListener {
            findNavController().navigate(R.id.action_editFragment_to_addLightFragment)
        }
    }

    private fun showUndoSnackBar(item: GroupItem) {
        Snackbar.make(binding.root, R.string.item_deleted, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                item.key = null
                editViewModel.save(item).observe(viewLifecycleOwner) {
                    adapter.refresh()
                }
            }
            .show()
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