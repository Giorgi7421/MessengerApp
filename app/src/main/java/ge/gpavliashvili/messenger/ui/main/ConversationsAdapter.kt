package ge.gpavliashvili.messenger.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ge.gpavliashvili.messenger.R
import ge.gpavliashvili.messenger.data.model.Conversation
import ge.gpavliashvili.messenger.databinding.ItemConversationBinding
import ge.gpavliashvili.messenger.utils.toFormattedDate

class ConversationsAdapter(
    private val onConversationClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationsAdapter.ConversationViewHolder>(ConversationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConversationViewHolder(
        private val binding: ItemConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            binding.apply {
                tvUsername.text = conversation.otherUser?.nickname ?: "Unknown User"
                tvLastMessage.text = conversation.lastMessage.ifEmpty { "No messages" }
                tvTimestamp.text = conversation.lastMessageTimestamp.toFormattedDate()

                // Load profile image
                conversation.otherUser?.profileImageUrl?.let { imageUrl ->
                    if (imageUrl.isNotEmpty()) {
                        Glide.with(binding.root.context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(ivProfile)
                    } else {
                        ivProfile.setImageResource(R.drawable.ic_person)
                    }
                } ?: run {
                    ivProfile.setImageResource(R.drawable.ic_person)
                }

                root.setOnClickListener {
                    onConversationClick(conversation)
                }
            }
        }
    }

    private class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem == newItem
        }
    }
} 