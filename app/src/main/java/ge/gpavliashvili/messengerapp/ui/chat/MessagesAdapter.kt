package ge.gpavliashvili.messenger.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ge.gpavliashvili.messenger.R
import ge.gpavliashvili.messenger.data.model.Message
import ge.gpavliashvili.messenger.databinding.ItemMessageBinding
import ge.gpavliashvili.messenger.utils.toFormattedDate
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private val currentUserId: String
) : ListAdapter<Message, MessagesAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.apply {
                tvMessage.text = message.text
                tvTimestamp.text = formatMessageTime(message.timestamp)

                val isSentByCurrentUser = message.senderId == currentUserId

                if (isSentByCurrentUser) {
                    // Sent message - align to right, blue background
                    cvMessage.setCardBackgroundColor(
                        binding.root.context.getColor(R.color.message_sent_background)
                    )
                    tvMessage.setTextColor(
                        binding.root.context.getColor(R.color.message_sent_text)
                    )
                    tvTimestamp.setTextColor(
                        binding.root.context.getColor(R.color.message_sent_text)
                    )
                    
                    // Align to right
                    val layoutParams = cvMessage.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.setMargins(64, 8, 16, 8)
                    cvMessage.layoutParams = layoutParams
                    
                    // Set layout gravity to end
                    (binding.root as ViewGroup).let { parent ->
                        val frameLayoutParams = binding.cvMessage.layoutParams as android.widget.FrameLayout.LayoutParams
                        frameLayoutParams.gravity = android.view.Gravity.END
                        binding.cvMessage.layoutParams = frameLayoutParams
                    }
                } else {
                    // Received message - align to left, gray background
                    cvMessage.setCardBackgroundColor(
                        binding.root.context.getColor(R.color.message_received_background)
                    )
                    tvMessage.setTextColor(
                        binding.root.context.getColor(R.color.message_received_text)
                    )
                    tvTimestamp.setTextColor(
                        binding.root.context.getColor(R.color.message_received_text)
                    )
                    
                    // Align to left
                    val layoutParams = cvMessage.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.setMargins(16, 8, 64, 8)
                    cvMessage.layoutParams = layoutParams
                    
                    // Set layout gravity to start
                    (binding.root as ViewGroup).let { parent ->
                        val frameLayoutParams = binding.cvMessage.layoutParams as android.widget.FrameLayout.LayoutParams
                        frameLayoutParams.gravity = android.view.Gravity.START
                        binding.cvMessage.layoutParams = frameLayoutParams
                    }
                }
            }
        }

        private fun formatMessageTime(timestamp: Long): String {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            return formatter.format(date)
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
} 