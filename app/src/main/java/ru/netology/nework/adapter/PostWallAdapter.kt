package ru.netology.nework.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardWallPostBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.view.load

interface OnInteractionWallListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onPreviewImage(post: Post) {}
    fun onPreviewMap(post: Post) {}
}

class PostWallAdapter(
    private val OnInteractionWallListener: OnInteractionWallListener,
) : ListAdapter<Post, PostWallViewHolder>(PostWallDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostWallViewHolder {
        val binding =
            CardWallPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostWallViewHolder(binding, OnInteractionWallListener)
    }

    override fun onBindViewHolder(holder: PostWallViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostWallViewHolder(
    private val binding: CardWallPostBinding,
    private val OnInteractionWallListener: OnInteractionWallListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            content.text = post.content

            if (post.link != null) content.text = "${content.text} \n${post.link}"

            buttonLike.isChecked = post.likedByMe

            buttonMap.isVisible = post.coords != null

            if (post.mentionIds?.isEmpty() == true) {
                mentions.visibility = View.GONE
                mentionsInfo.visibility = View.GONE
            } else {
                mentions.visibility = View.VISIBLE
                mentionsInfo.visibility = View.VISIBLE
                mentions.text = post.mentionList?.joinToString(", ", "", "", 10, "...", null)
            }

            when (post.attachment?.type) {
                AttachmentType.IMAGE -> {
                    AttachmentFrame.visibility = View.VISIBLE
                    AttachmentImage.visibility = View.VISIBLE
                    AttachmentVideo.visibility = View.GONE
                    AttachmentImage.load(post.attachment.url)
                }
                AttachmentType.VIDEO -> {
                    AttachmentFrame.visibility = View.VISIBLE
                    AttachmentImage.visibility = View.GONE
                    AttachmentVideo.apply {
                        visibility = View.VISIBLE
                        setMediaController(MediaController(binding.root.context))
                        setVideoURI(Uri.parse(post.attachment.url))
                        setOnPreparedListener {
                            animate().alpha(1F)
                            seekTo(0)
                            setZOrderOnTop(false)
                        }
                        setOnCompletionListener {
                            stopPlayback()
                        }
                    }

                }
                AttachmentType.AUDIO -> {
                    AttachmentFrame.visibility = View.VISIBLE
                    AttachmentImage.visibility = View.GONE
                    AttachmentVideo.apply {
                        visibility = View.VISIBLE
                        setMediaController(MediaController(binding.root.context))
                        setVideoURI(Uri.parse(post.attachment.url))
                        setBackgroundResource(R.drawable.audio2)
                        setOnPreparedListener {
                            setZOrderOnTop(true)
                        }
                        setOnCompletionListener {
                            stopPlayback()
                        }
                    }
                }
                null -> {
                    AttachmentFrame.visibility = View.GONE
                }
            }

            menu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, post.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                OnInteractionWallListener.onRemove(post)
                                true
                            }
                            R.id.edit_content -> {
                                OnInteractionWallListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            buttonLike.setOnClickListener {
                OnInteractionWallListener.onLike(post)
            }
            buttonMap.setOnClickListener {
                OnInteractionWallListener.onPreviewMap(post)
            }
        }
    }
}

class PostWallDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
