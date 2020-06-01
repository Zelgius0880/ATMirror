package zelgius.com.atmirror.mobile.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import com.zelgius.livedataextensions.observe
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.adapter.GroupAdapter
import zelgius.com.atmirror.mobile.databinding.*
import zelgius.com.atmirror.mobile.dialog.AddGroupDialog
import zelgius.com.atmirror.mobile.viewModel.EditViewModel
import zelgius.com.atmirror.mobile.viewModel.HomeViewModel
import zelgius.com.atmirror.shared.entity.Group
import zelgius.com.utils.ViewModelHelper
import zelgius.com.view_helper_extensions.snackBar

class HomeFragment : Fragment() {

    private val editViewModel by lazy {
        ViewModelHelper.create<EditViewModel>(
            requireActivity()
        )
    }

    private val adapter by lazy {
        GroupAdapter(editListener = {
            editViewModel.setGroup(it)
            navController.navigate(R.id.action_homeFragment_to_editFragment)
        },
        deleteListener = {
            editViewModel.delete(it).observe(this) {
                snackBar(getString(R.string.item_deleted))
                binding.progressBar.visibility = View.VISIBLE
                fetchList()
            }
        })
    }
    private var _binding: FragmentHomeBinding? = null
    private val navController by lazy { findNavController() }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val homeViewModel by lazy { ViewModelHelper.create<HomeViewModel>(requireActivity()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.adapter = adapter

        binding.progressBar.visibility = View.VISIBLE
        fetchList()

        binding.addGroup.setOnClickListener {
            AddGroupDialog().apply {
                listener = {
                    binding.progressBar.visibility = View.VISIBLE
                    editViewModel.save(Group(name = it)).observe(this@HomeFragment) {
                        fetchList()
                    }
                }
            }.show(parentFragmentManager, "add_group")
        }
    }

    private fun fetchList() {
        homeViewModel.getGroups().observe(this) {
            binding.progressBar.visibility = View.GONE
            adapter.submitList(it)
        }
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
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}