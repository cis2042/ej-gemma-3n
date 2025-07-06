package com.example.gemmaprototype.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gemmaprototype.R
import com.example.gemmaprototype.data.ChatMessage
import com.example.gemmaprototype.data.MessageType
import java.text.SimpleDateFormat
import java.util.*

/**
 * 聊天消息適配器
 */
class ChatAdapter(
    private val onMediaClick: (String) -> Unit = {}
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatDiffCallback()) {
    
    companion object {
        private const val VIEW_TYPE_USER_TEXT = 1
        private const val VIEW_TYPE_AI_TEXT = 2
        private const val VIEW_TYPE_USER_MEDIA = 3
        private const val VIEW_TYPE_AI_MEDIA = 4
    }
    
    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return when {
            message.isFromUser && message.messageType == MessageType.TEXT -> VIEW_TYPE_USER_TEXT
            !message.isFromUser && message.messageType == MessageType.TEXT -> VIEW_TYPE_AI_TEXT
            message.isFromUser -> VIEW_TYPE_USER_MEDIA
            else -> VIEW_TYPE_AI_MEDIA
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER_TEXT -> {
                val view = inflater.inflate(R.layout.item_message_user_text, parent, false)
                UserTextViewHolder(view)
            }
            VIEW_TYPE_AI_TEXT -> {
                val view = inflater.inflate(R.layout.item_message_ai_text, parent, false)
                AiTextViewHolder(view)
            }
            VIEW_TYPE_USER_MEDIA -> {
                val view = inflater.inflate(R.layout.item_message_user_media, parent, false)
                UserMediaViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_message_ai_media, parent, false)
                AiMediaViewHolder(view)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserTextViewHolder -> holder.bind(message)
            is AiTextViewHolder -> holder.bind(message)
            is UserMediaViewHolder -> holder.bind(message, onMediaClick)
            is AiMediaViewHolder -> holder.bind(message, onMediaClick)
        }
    }
    
    /**
     * 用戶文字消息ViewHolder
     */
    class UserTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        
        fun bind(message: ChatMessage) {
            messageText.text = message.content
            timeText.text = formatTime(message.timestamp)
        }
    }
    
    /**
     * AI文字消息ViewHolder
     */
    class AiTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val processingIndicator: View = itemView.findViewById(R.id.processingIndicator)
        
        fun bind(message: ChatMessage) {
            messageText.text = message.content
            timeText.text = formatTime(message.timestamp)
            processingIndicator.visibility = if (message.isProcessing) View.VISIBLE else View.GONE
        }
    }
    
    /**
     * 用戶媒體消息ViewHolder
     */
    class UserMediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val mediaImage: ImageView = itemView.findViewById(R.id.mediaImage)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val mediaTypeIcon: ImageView = itemView.findViewById(R.id.mediaTypeIcon)
        
        fun bind(message: ChatMessage, onMediaClick: (String) -> Unit) {
            messageText.text = message.content
            timeText.text = formatTime(message.timestamp)
            
            message.mediaPath?.let { path ->
                when (message.messageType) {
                    MessageType.IMAGE -> {
                        mediaImage.visibility = View.VISIBLE
                        mediaTypeIcon.visibility = View.GONE
                        Glide.with(itemView.context)
                            .load(path)
                            .centerCrop()
                            .into(mediaImage)
                        mediaImage.setOnClickListener { onMediaClick(path) }
                    }
                    MessageType.AUDIO -> {
                        mediaImage.visibility = View.GONE
                        mediaTypeIcon.visibility = View.VISIBLE
                        mediaTypeIcon.setImageResource(R.drawable.ic_audio)
                        mediaTypeIcon.setOnClickListener { onMediaClick(path) }
                    }
                    MessageType.VIDEO -> {
                        mediaImage.visibility = View.VISIBLE
                        mediaTypeIcon.visibility = View.VISIBLE
                        mediaTypeIcon.setImageResource(R.drawable.ic_play)
                        Glide.with(itemView.context)
                            .load(path)
                            .centerCrop()
                            .into(mediaImage)
                        mediaImage.setOnClickListener { onMediaClick(path) }
                    }
                    MessageType.DOCUMENT -> {
                        mediaImage.visibility = View.GONE
                        mediaTypeIcon.visibility = View.VISIBLE
                        mediaTypeIcon.setImageResource(R.drawable.ic_document)
                        mediaTypeIcon.setOnClickListener { onMediaClick(path) }
                    }
                    else -> {
                        mediaImage.visibility = View.GONE
                        mediaTypeIcon.visibility = View.GONE
                    }
                }
            }
        }
    }
    
    /**
     * AI媒體消息ViewHolder
     */
    class AiMediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val mediaImage: ImageView = itemView.findViewById(R.id.mediaImage)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val mediaTypeIcon: ImageView = itemView.findViewById(R.id.mediaTypeIcon)
        private val processingIndicator: View = itemView.findViewById(R.id.processingIndicator)
        
        fun bind(message: ChatMessage, onMediaClick: (String) -> Unit) {
            messageText.text = message.content
            timeText.text = formatTime(message.timestamp)
            processingIndicator.visibility = if (message.isProcessing) View.VISIBLE else View.GONE
            
            // AI消息的媒體處理邏輯與用戶消息類似
            message.mediaPath?.let { path ->
                when (message.messageType) {
                    MessageType.IMAGE -> {
                        mediaImage.visibility = View.VISIBLE
                        mediaTypeIcon.visibility = View.GONE
                        Glide.with(itemView.context)
                            .load(path)
                            .centerCrop()
                            .into(mediaImage)
                        mediaImage.setOnClickListener { onMediaClick(path) }
                    }
                    else -> {
                        mediaImage.visibility = View.GONE
                        mediaTypeIcon.visibility = View.GONE
                    }
                }
            }
        }
    }
    
    companion object {
        private fun formatTime(date: Date): String {
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            return formatter.format(date)
        }
    }
}

/**
 * DiffUtil回調
 */
class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
    override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem == newItem
    }
}
