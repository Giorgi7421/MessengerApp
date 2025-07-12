package ge.gpavliashvili.messenger.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import ge.gpavliashvili.messenger.R
import ge.gpavliashvili.messenger.databinding.FragmentChatBinding
import ge.gpavliashvili.messenger.utils.Constants
import ge.gpavliashvili.messenger.utils.hide
import ge.gpavliashvili.messenger.utils.show
import ge.gpavliashvili.messenger.utils.showToast

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var messagesAdapter: MessagesAdapter
    private var otherUserId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get arguments
        otherUserId = arguments?.getString(Constants.KEY_USER_ID) ?: ""
        if (otherUserId.isEmpty()) {
            showToast("Error: User ID not found")
            findNavController().navigateUp()
            return
        }
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        
        // Initialize chat with other user
        viewModel.initChat(otherUserId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter(viewModel.getCurrentUserId())
        binding.rvMessages.apply {
            adapter = messagesAdapter
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabSend.setOnClickListener {
            val messageText = binding.etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                viewModel.sendMessage(messageText)
                binding.etMessage.text?.clear()
                
                // Scroll to bottom after sending
                binding.rvMessages.post {
                    if (messagesAdapter.itemCount > 0) {
                        binding.rvMessages.scrollToPosition(messagesAdapter.itemCount - 1)
                    }
                }
            }
        }
        
        // Handle text input focus to ensure smooth scrolling
        binding.etMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Scroll to bottom when keyboard appears
                binding.rvMessages.post {
                    if (messagesAdapter.itemCount > 0) {
                        binding.rvMessages.scrollToPosition(messagesAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.otherUser.observe(viewLifecycleOwner) { user ->
            binding.apply {
                tvUsername.text = user.nickname
                tvProfession.text = user.profession
                collapsingToolbar.title = user.nickname

                // Load profile image
                if (user.profileImageUrl.isNotEmpty()) {
                    Glide.with(this@ChatFragment)
                        .load(user.profileImageUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(ivProfile)
                } else {
                    ivProfile.setImageResource(R.drawable.ic_person)
                }
            }
        }

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messagesAdapter.submitList(messages) {
                // Scroll to bottom when new messages arrive
                if (messages.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }
            }
            
            if (messages.isEmpty()) {
                binding.tvEmptyState.show()
                binding.rvMessages.hide()
            } else {
                binding.tvEmptyState.hide()
                binding.rvMessages.show()
            }
        }

        viewModel.sendMessageResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                // Message sent successfully, no need to do anything
                // The real-time listener will update the messages list
            }.onFailure { exception ->
                showToast(exception.message ?: getString(R.string.message_failed))
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can show/hide a loading indicator if needed
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                showToast(error)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 