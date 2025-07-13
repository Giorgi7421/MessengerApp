package ge.gpavliashvili.messenger.ui.users

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
import ge.gpavliashvili.messenger.databinding.FragmentUsersBinding
import ge.gpavliashvili.messenger.utils.Constants
import ge.gpavliashvili.messenger.utils.hide
import ge.gpavliashvili.messenger.utils.show
import ge.gpavliashvili.messenger.utils.showToast

class UsersFragment : Fragment() {
    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: UsersViewModel by viewModels()
    private lateinit var usersAdapter: UsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupSwipeRefresh()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        usersAdapter = UsersAdapter { user ->
            // Navigate to chat with selected user
            val bundle = bundleOf(Constants.KEY_USER_ID to user.uid)
            findNavController().navigate(R.id.action_users_to_chat, bundle)
        }
        binding.rvUsers.adapter = usersAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString().trim()
            viewModel.searchUsers(query)
            
            updateUIForSearch(query)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshUsers()
            binding.etSearch.text?.clear()
        }
    }

    private fun setupObservers() {
        // Observe users for initial load (no search)
        viewModel.users.observe(viewLifecycleOwner) { users ->
            if (binding.etSearch.text.isNullOrEmpty()) {
                usersAdapter.submitList(users)
                updateEmptyState(users.isEmpty(), isSearching = false)
            }
        }

        // Observe search results
        viewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
            if (!binding.etSearch.text.isNullOrEmpty()) {
                usersAdapter.submitList(searchResults)
                updateEmptyState(searchResults.isEmpty(), isSearching = true)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            if (isLoading) {
                binding.progressBar.show()
            } else {
                binding.progressBar.hide()
            }
        }

        viewModel.isSearching.observe(viewLifecycleOwner) { isSearching ->
            if (isSearching) {
                binding.progressBarSearch.show()
            } else {
                binding.progressBarSearch.hide()
            }
        }

        viewModel.hasSearchResults.observe(viewLifecycleOwner) { hasResults ->
            // This will be handled by other observers
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                showToast(error)
            }
        }
    }

    private fun updateUIForSearch(query: String) {
        when {
            query.isEmpty() -> {
                // Show all users
                binding.tvSearchHint.hide()
                binding.tvEmptyState.hide()
                binding.rvUsers.show()
            }
            query.length < Constants.MIN_SEARCH_QUERY_LENGTH -> {
                // Show search hint
                binding.tvSearchHint.show()
                binding.tvEmptyState.hide()
                binding.rvUsers.hide()
            }
            else -> {
                // Searching with valid length
                binding.tvSearchHint.hide()
                binding.rvUsers.show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean, isSearching: Boolean) {
        if (isEmpty) {
            binding.rvUsers.hide()
            if (isSearching) {
                binding.tvEmptyState.text = getString(R.string.no_users_found)
            } else {
                binding.tvEmptyState.text = getString(R.string.no_users_found)
            }
            binding.tvEmptyState.show()
        } else {
            binding.rvUsers.show()
            binding.tvEmptyState.hide()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 