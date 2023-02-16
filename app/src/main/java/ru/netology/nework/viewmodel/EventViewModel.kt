package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.MediaRequest
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.model.EventFeedModel
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.util.SingleLiveEvent
import ru.netology.nework.util.convertDateTime2ISO_Instant
import java.io.File
import javax.inject.Inject

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    datetime = "",
    published = "",
    type = EventType.OFFLINE,
    likedByMe = false,
    participatedByMe = false,
    ownedByMe = false
)

private val noPhoto = PhotoModel()
private val noCoords = Coordinates()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: PostRepository,
    auth: AppAuth,
) : ViewModel() {
    val data: LiveData<EventFeedModel> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.events
                .map { event ->
                    EventFeedModel(
                        event.map { it.copy(ownedByMe = it.authorId == myId) },
                        event.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val editedEvent = MutableLiveData(empty)

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    private val _coords = MutableLiveData(noCoords)
    val coords: LiveData<Coordinates>
        get() = _coords

    init {
        loadEvents()
    }

    fun loadEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getEvents()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getEvents()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun saveEvent() {
        editedEvent.value?.let { event ->
            _eventCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> {
                            var EventNew = event
                            if (event.attachment != null)
                                EventNew = event.copy(attachment = null)
                            repository.saveEvent(EventNew)
                        }
                        else -> _photo.value?.file?.let { file ->
                            repository.saveEventWithAttachment(event, MediaRequest(file))
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        editedEvent.value = empty
        _photo.value = noPhoto
        _coords.value = noCoords
    }

    fun edit(event: Event) {
        editedEvent.value = event
    }

    fun changeDateTime(date: String, time: String) {
        val datetime = convertDateTime2ISO_Instant(date, time)
        editedEvent.value = editedEvent.value?.copy(datetime = datetime)
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (editedEvent.value?.content == text) {
            return
        }
        editedEvent.value = editedEvent.value?.copy(content = text)
    }

    fun changeLink(link: String) {
        val text = if (link.isEmpty())
            null
        else
            link.trim()

        if (editedEvent.value?.link == text) {
            return
        }
        editedEvent.value = editedEvent.value?.copy(link = text)
    }

    fun changeCoords(lat: String?, long: String?) {
        val coords = if (lat == null && long == null)
            null
        else
            Coordinates(lat, long)

        if (editedEvent.value?.coords == coords) {
            return
        }
        editedEvent.value = editedEvent.value?.copy(coords = coords)
    }

    fun changeSpeakers(speakersStr: String) {
        try {
            val speakers = speakersStr.split(",").map {
                it.trim().toLong()
            }
            editedEvent.value = editedEvent.value?.copy(speakerIds = speakers)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun removeAttachment() {
        editedEvent.value = editedEvent.value?.copy(attachment = null)
    }

    fun changeCoordsFromMap(lat: String, long: String) {
        _coords.value = if (lat.isBlank() && long.isBlank())
            null
        else
            Coordinates(lat, long)
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeEventById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun likeById(id: Long, likedByMe: Boolean) = viewModelScope.launch {
        try {
            repository.likeEventById(id, likedByMe)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getEditEvent(): Event? {
        return editedEvent.value
    }


    fun part(id: Long, participatedByMe: Boolean) = viewModelScope.launch {
        try {
            repository.partEventById(id, participatedByMe)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }
}
