package com.example.gemmaprototype.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gemmaprototype.R
import com.example.gemmaprototype.data.MediaAttachment
import com.example.gemmaprototype.data.MediaType
import com.example.gemmaprototype.media.MediaUtils

/**
 * 媒體預覽適配器
 * 用於在輸入區域顯示選中的媒體文件預覽
 */
class MediaPreviewAdapter(
    private val onRemoveClick: (MediaAttachment) -> Unit
) : ListAdapter<MediaAttachment, MediaPreviewAdapter.MediaPreviewViewHolder>(MediaPreviewDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaPreviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_preview, parent, false)
        return MediaPreviewViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MediaPreviewViewHolder, position: Int) {
        holder.bind(getItem(position), onRemoveClick)
    }
    
    class MediaPreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mediaImage: ImageView = itemView.findViewById(R.id.mediaImage)
        private val mediaTypeIcon: ImageView = itemView.findViewById(R.id.mediaTypeIcon)
        private val mediaInfo: TextView = itemView.findViewById(R.id.mediaInfo)
        private val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)
        
        fun bind(attachment: MediaAttachment, onRemoveClick: (MediaAttachment) -> Unit) {
            when (attachment.type) {
                MediaType.IMAGE -> {
                    mediaImage.visibility = View.VISIBLE
                    mediaTypeIcon.visibility = View.GONE
                    
                    Glide.with(itemView.context)
                        .load(attachment.path)
                        .centerCrop()
                        .into(mediaImage)
                    
                    mediaInfo.text = MediaUtils.formatFileSize(attachment.size)
                }
                
                MediaType.VIDEO -> {
                    mediaImage.visibility = View.VISIBLE
                    mediaTypeIcon.visibility = View.VISIBLE
                    mediaTypeIcon.setImageResource(R.drawable.ic_play)
                    
                    Glide.with(itemView.context)
                        .load(attachment.path)
                        .centerCrop()
                        .into(mediaImage)
                    
                    val duration = attachment.duration?.let { MediaUtils.formatDuration(it) } ?: ""
                    val size = MediaUtils.formatFileSize(attachment.size)
                    mediaInfo.text = "$duration • $size"
                }
                
                MediaType.AUDIO -> {
                    mediaImage.visibility = View.GONE
                    mediaTypeIcon.visibility = View.VISIBLE
                    mediaTypeIcon.setImageResource(R.drawable.ic_audio)
                    
                    val duration = attachment.duration?.let { MediaUtils.formatDuration(it) } ?: ""
                    val size = MediaUtils.formatFileSize(attachment.size)
                    mediaInfo.text = "$duration • $size"
                }
                
                MediaType.DOCUMENT -> {
                    mediaImage.visibility = View.GONE
                    mediaTypeIcon.visibility = View.VISIBLE
                    mediaTypeIcon.setImageResource(R.drawable.ic_document)
                    
                    mediaInfo.text = MediaUtils.formatFileSize(attachment.size)
                }
            }
            
            removeButton.setOnClickListener {
                onRemoveClick(attachment)
            }
        }
    }
}

class MediaPreviewDiffCallback : DiffUtil.ItemCallback<MediaAttachment>() {
    override fun areItemsTheSame(oldItem: MediaAttachment, newItem: MediaAttachment): Boolean {
        return oldItem.path == newItem.path
    }
    
    override fun areContentsTheSame(oldItem: MediaAttachment, newItem: MediaAttachment): Boolean {
        return oldItem == newItem
    }
}
