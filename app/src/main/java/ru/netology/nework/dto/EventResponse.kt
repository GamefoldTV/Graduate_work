package ru.netology.nework.dto

import ru.netology.nework.enumeration.EventType

data class EventResponse(
   val id : Long,
   val authorId : Long,
   val author: String,
   val authorAvatar: String?,
   val authorJob : String?,
   val content : String,
   val datetime : String,
   val published : String,
   val coords : Coordinates?,
   val type : EventType,
   val likeOwnerIds : List<Long>? = emptyList(),
   val likedByMe : Boolean,
   val speakerIds : List<Long>? = emptyList(),
   val participantsIds : List<Long>? = emptyList(),
   val participatedByMe : Boolean,
   val attachment: Attachment?,
   val link : String?,
   val ownedByMe : Boolean,
   val users : Map<String, UserPreview>?,
)