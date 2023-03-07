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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import ru.netology.nework.R
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.repository.PostRepositoryImpl
import ru.netology.nework.util.convertString2Date2String
import ru.netology.nework.view.load
import ru.netology.nework.view.loadCircleCrop

interface OnInteractionEventListener {
    fun onLike(event: Event) {}
    fun onEdit(event: Event) {}
    fun onRemove(event: Event) {}
    fun onPreviewMap(event: Event) {}
    fun onParticipate(event: Event) {}
}

class EventAdapter(
    private val OnInteractionEventListener: OnInteractionEventListener,
) : ListAdapter<Event, EventViewHolder>(EventDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, OnInteractionEventListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val OnInteractionEventListener: OnInteractionEventListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {
        binding.apply {
            author.text = event.author

            if (event.authorJob.isNullOrBlank()) {
                authorJob.visibility = View.GONE
            } else {
                authorJob.text = event.authorJob
                authorJob.visibility = View.VISIBLE
            }
            published.text = convertString2Date2String(event.published)

            content.text = event.content

            eventType.text = event.type.toString()

            datetime.text = convertString2Date2String(event.datetime)

            if (event.link != null) content.text = "${content.text} \n${event.link}"

            if (event.authorAvatar != null)
                avatar.loadCircleCrop(event.authorAvatar)
            else avatar.setImageResource(R.mipmap.ic_avatar_1_round)

            val speakersIds = event.speakerIds?.map {
             //   repository.users.map {

            //    }
            }

            speakers.text =  event.speakerIds?.map {
                it.toString()
            }.toString()
                //speakersIds.toString()

            buttonLike.isChecked = event.likedByMe
            buttonParticipate.isChecked = event.participatedByMe
            if (event.participatedByMe) buttonParticipate.setText(R.string.button_part)
            else buttonParticipate.setText(R.string.button_nonpart)

            buttonMap.isVisible = event.coords != null

            when (event.attachment?.type) {
                AttachmentType.IMAGE -> {
                    AttachmentFrame.visibility = View.VISIBLE
                    AttachmentImage.visibility = View.VISIBLE
                    AttachmentVideo.visibility = View.GONE
                    AttachmentImage.load(event.attachment.url)
                }
                AttachmentType.VIDEO -> {
                    AttachmentFrame.visibility = View.VISIBLE
                    AttachmentImage.visibility = View.GONE
                    AttachmentVideo.apply {
                        visibility = View.VISIBLE
                        setMediaController(MediaController(binding.root.context))
                        setVideoURI(Uri.parse(event.attachment.url))
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
                        setVideoURI(Uri.parse(event.attachment.url))
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

            menu.visibility = if (event.ownedByMe) View.VISIBLE else View.INVISIBLE

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, event.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                OnInteractionEventListener.onRemove(event)
                                true
                            }
                            R.id.edit_content -> {
                                OnInteractionEventListener.onEdit(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            buttonLike.setOnClickListener {
                OnInteractionEventListener.onLike(event)
            }
            buttonMap.setOnClickListener {
                OnInteractionEventListener.onPreviewMap(event)
            }

            buttonParticipate.setOnClickListener {
                OnInteractionEventListener.onParticipate(event)
            }

        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}
