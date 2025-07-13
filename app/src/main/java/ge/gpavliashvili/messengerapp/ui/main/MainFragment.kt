package ge.gpavliashvili.messenger.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ge.gpavliashvili.messenger.R
import ge.gpavliashvili.messenger.databinding.FragmentMainBinding
import ge.gpavliashvili.messenger.utils.Constants
import ge.gpavliashvili.messenger.utils.hide
import ge.gpavliashvili.messenger.utils.show
import ge.gpavliashvili.messenger.utils.showToast

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by viewModels()
    private lateinit var conversationsAdapter: ConversationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        setupObservers()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        conversationsAdapter = ConversationsAdapter { conversation ->
            conversation.otherUser?.let { otherUser ->
                val bundle = bundleOf(Constants.KEY_USER_ID to otherUser.uid)
                findNavController().navigate(R.id.action_main_to_chat, bundle)
            }
        }
        binding.rvConversations.adapter = conversationsAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.searchConversations(text.toString())
        }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_users)
        }
    }

    private fun setupObservers() {
        viewModel.filteredConversations.observe(viewLifecycleOwner) { conversations ->
            conversationsAdapter.submitList(conversations)
            
            if (conversations.isEmpty()) {
                binding.tvEmptyState.show()
                binding.rvConversations.hide()
            } else {
                binding.tvEmptyState.hide()
                binding.rvConversations.show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.show()
            } else {
                binding.progressBar.hide()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                showToast(error)
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home, do nothing or refresh
                    viewModel.refreshConversations()
                    true
                }
                R.id.nav_profile -> {
                    findNavController().navigate(R.id.action_main_to_profile)
                    true
                }
                else -> false
            }
        }
        
        // Set home as selected
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 