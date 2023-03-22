package ru.netology.nework.dto

import ru.netology.nework.enumeration.EventType

data class Event(
    val id : Long,
    val authorId : Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob : String? = null,
    val content : String,
    val datetime : String,
    val published : String,
    val coords : Coordinates? = null,
    val type : EventType,
    val likeOwnerIds : List<Long>? = emptyList(),
    val likedByMe : Boolean,
    val speakerIds : List<Long>? = emptyList(),
    val speakerList : List<String>? = emptyList(),
    val participantsIds : List<Long>? = emptyList(),
    val participantsList : List<String>? = emptyList(),
    val participatedByMe : Boolean,
    val attachment: Attachment? = null,
    val link : String? = null,
    val ownedByMe : Boolean,
)
