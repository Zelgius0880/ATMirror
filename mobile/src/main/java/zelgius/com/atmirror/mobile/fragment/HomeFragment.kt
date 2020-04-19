package zelgius.com.atmirror.mobile.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.adapter.GroupAdapter
import zelgius.com.atmirror.mobile.databinding.*
import zelgius.com.atmirror.mobile.viewModel.EditViewModel
import zelgius.com.atmirror.mobile.viewModel.HomeViewModel
import zelgius.com.atmirror.shared.protocol.CurrentStatus
import zelgius.com.atmirror.shared.viewModel.PhoneNetworkViewModel
import zelgius.com.utils.ViewModelHelper
import zelgius.com.utils.observe

class HomeFragment : Fragment() {

    private val editViewModel by lazy {
        ViewModelHelper.create<EditViewModel>(
            requireActivity()
        )
    }

    private val adapter by lazy {
        GroupAdapter{
            editViewModel.setGroup(it)
            navController.navigate(R.id.action_homeFragment_to_editFragment)
        }
    }
    private var _binding: FragmentHomeBinding? = null
    private val navController by lazy { findNavController() }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val homeViewModel by lazy { ViewModelHelper.create<HomeViewModel>(requireActivity()) }
    private var lastKnownStatus = CurrentStatus.Status.NOT_WORKING

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

        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<Any>() {
            // Concert details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(
                old: Any,
                new: Any
            ) = old == new

            override fun areContentsTheSame(
                old: Any,
                new: Any
            ) = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}