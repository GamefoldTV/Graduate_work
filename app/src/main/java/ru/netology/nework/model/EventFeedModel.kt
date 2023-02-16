package ru.netology.nework.model

import ru.netology.nework.dto.Event

data class EventFeedModel(
    val events: List<Event> = emptyList(),
    val empty: Boolean = false,
)
