package zelgius.com.atmirror.mobile.fragment

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.zelgius.livedataextensions.observe
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.adapter.BindableViewHolder
import zelgius.com.atmirror.mobile.databinding.AdapterHeaderBinding
import zelgius.com.atmirror.mobile.databinding.AdapterLightAddBinding
import zelgius.com.atmirror.mobile.databinding.FragmentAddLightBinding
import zelgius.com.atmirror.mobile.dialog.HueConfigDialog
import zelgius.com.atmirror.mobile.dialog.LIFXConfigDialog
import zelgius.com.atmirror.mobile.viewModel.EditViewModel
import zelgius.com.atmirror.mobile.viewModel.LightViewModel
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.lights.repository.ILight
import zelgius.com.utils.ViewModelHelper
import zelgius.com.view_helper_extensions.snackBar

class AddLightFragment : Fragment() {
    private var _binding: FragmentAddLightBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val adapter by lazy { Adapter() }

    lateinit var saveMenu: MenuItem

    val list = mutableListOf<Light>()

    private val lightViewModel by lazy {
        ViewModelHelper.create<LightViewModel>(
            this,
            requireActivity().application
        )
    }

    private val editViewModel by lazy {
        ViewModelHelper.create<EditViewModel>(
            requireActivity()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddLightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.adapter = adapter

        lightViewModel.listLights.observe(this) {
            adapter.setLights(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_save, menu)
        saveMenu = menu.findItem(R.id.save)
        saveMenu.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                saveMenu.actionView = ProgressBar(requireContext())
                editViewModel.save(list).observe(this) {
                    saveMenu.actionView = null
                    findNavController().navigateUp()
                    if (it.isNotEmpty())
                        snackBar(
                            resources.getQuantityString(
                                R.plurals.light_not_saved,
                                it.size,
                                it.size
                            )
                        )
                    else
                        snackBar(getString(R.string.light_saved))
                }
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLIFXDialog() {
        LIFXConfigDialog().apply {
            listener = {
                lightViewModel.setLifxKey(it)
            }
        }.show(parentFragmentManager, "lifx_dialog")
    }
    private fun showHueDialog() {
        HueConfigDialog().apply {
        }.show(parentFragmentManager, "hue_dialog")
    }

    inner class Adapter : RecyclerView.Adapter<BindableViewHolder<*>>() {
        private val lights = mutableListOf<Light>()

        override fun getItemViewType(position: Int) =
            if (position == 0) R.layout.adapter_header
            else R.layout.adapter_light


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder<*> =
            with(LayoutInflater.from(parent.context)) {
                when (viewType) {
                    R.layout.adapter_light -> LightViewHolder(
                        AdapterLightAddBinding.inflate(this, parent, false)
                    )

                    R.layout.adapter_header -> HeaderViewHolder(
                        AdapterHeaderBinding.inflate(
                            this,
                            parent,
                            false
                        )
                    )
                    else -> error("Unhandle view type")
                }
            }

        override fun getItemCount(): Int =
            lights.size + 1

        override fun onBindViewHolder(holder: BindableViewHolder<*>, position: Int) {
            val item = if (position == 0) null else lights[position - 1]
            when (holder) {
                is HeaderViewHolder -> holder.bind(Unit)
                is LightViewHolder -> if (item != null) holder.bind(item)
            }
        }

        fun setLights(list: List<Light>) {
            lights.clear()
            lights.addAll(list)
            notifyDataSetChanged()
        }

    }

    inner class HeaderViewHolder(private val binding: AdapterHeaderBinding) :
        BindableViewHolder<Unit>(binding.root) {
        override fun bind(item: Unit) {
            binding.settingLifx.setOnClickListener {
                showLIFXDialog()
            }

            binding.settingHue.setOnClickListener {
                showHueDialog()
            }

            binding.listLifx.setOnClickListener {
                this@AddLightFragment.binding.progressBar.visibility = View.VISIBLE
                lightViewModel.fetchLIFXList().observe(this@AddLightFragment) {
                    this@AddLightFragment.binding.progressBar.visibility = View.GONE
                    if (!it)
                        snackBar(
                            text = getString(R.string.fect_lifx_failed),
                            actionText = getString(R.string.settings)
                        ) { showLIFXDialog() }
                }
            }

            binding.listHue.setOnClickListener {
                this@AddLightFragment.binding.progressBar.visibility = View.VISIBLE
                lightViewModel.fetchHueList().observe(this@AddLightFragment) {
                    this@AddLightFragment.binding.progressBar.visibility = View.GONE
                    if (!it)
                        snackBar(
                            text = getString(R.string.fect_lifx_failed),
                            actionText = getString(R.string.settings)
                        ) { showLIFXDialog() }
                }
            }
        }

    }

    inner class LightViewHolder(private val binder: AdapterLightAddBinding) :
        BindableViewHolder<Light>(binder.root) {
        override fun bind(item: Light) {
            binder.name.text = item.name
            binder.uid.text = item.productName

            binder.type.setText(
                when (item.type) {
                    ILight.Type.HUE -> R.string.hue
                    ILight.Type.LIFX -> R.string.lifx
                }
            )

            binder.card.setOnClickListener {
                binder.card.isChecked = !binder.card.isChecked
                if (binder.card.isChecked)
                    list.add(item)
                else
                    list.remove(item)

                saveMenu.isVisible = list.size > 0
            }
        }
    }


}
