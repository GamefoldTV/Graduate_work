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
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.model.EventFeedModel
import ru.netology.nework.model.FeedModel
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.util.SingleLiveEvent
import ru.netology.nework.util.convertDateTime2ISO_Instant
import java.io.File
import javax.inject.Inject


private val emptyPost = Post(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    published = "",
    likedByMe = false,
)

private val emptyEvent = Event(
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
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val auth: AppAuth,
) : ViewModel() {
    val dataPosts: LiveData<FeedModel> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.posts
                .map { posts ->
                    FeedModel(
                        posts.map { it.copy(ownedByMe = it.authorId == myId) },
                        posts.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

    val dataEvents: LiveData<EventFeedModel> = auth.authStateFlow
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

    private val editedPost = MutableLiveData(emptyPost)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val editedEvent = MutableLiveData(emptyEvent)

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
        loadPosts()
        loadEvents()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun savePosts() {
        editedPost.value?.let { post ->
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> {
                            var postNew = post
                            if (post.attachment != null)
                                postNew = post.copy(attachment = null)
                            repository.save(postNew)
                        }
                        else ->
                            if (_photo.value?.file != null)
                                repository.saveWithAttachment(
                                    post,
                                    MediaRequest(_photo.value?.file!!)
                                )
                            else repository.save(post)
                        //                           _photo.value?.file?.let { file ->
                        // repository.saveWithAttachment(post, MediaRequest(file))

                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        editedPost.value = emptyPost
        _photo.value = noPhoto
        _coords.value = noCoords
    }

    fun editPosts(post: Post) {
        editedPost.value = post
    }

    fun changeContentPosts(content: String) {
        val text = content.trim()
        if (editedPost.value?.content == text) {
            return
        }
        editedPost.value = editedPost.value?.copy(content = text)
    }

    fun changeLinkPosts(link: String) {
        val text = if (link.isEmpty())
            null
        else
            link.trim()

        if (editedPost.value?.link == text) {
            return
        }
        editedPost.value = editedPost.value?.copy(link = text)
    }

    fun changeCoordsPosts(lat: String?, long: String?) {
        val coords = if (lat == null && long == null || lat == "" && long == "")
            null
        else
            Coordinates(lat, long)

        if (editedPost.value?.coords == coords) {
            return
        }
        editedPost.value = editedPost.value?.copy(coords = coords)
        editedEvent.value = editedEvent.value?.copy(coords = coords)
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun removeAttachmentPosts() {
        editedPost.value = editedPost.value?.copy(attachment = null)
    }

    fun changeCoordsFromMap(lat: String, long: String){
        _coords.value = if (lat.isBlank() && long.isBlank())
            null
        else
            Coordinates(lat,long)
    }

    fun removePostById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun likePostById(id: Long) = viewModelScope.launch {
        try {
            repository.likeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getEditPost(): Post? {
        return editedPost.value
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

    fun refreshEvents() = viewModelScope.launch {
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
                        else -> {
                            if (_photo.value?.file != null)
                                repository.saveEventWithAttachment(
                                    event,
                                    MediaRequest(_photo.value?.file!!)
                                )
                            else repository.saveEvent(event)
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        editedEvent.value = emptyEvent
        _photo.value = noPhoto
        _coords.value = noCoords
    }

    fun editEvent(event: Event) {
        editedEvent.value = event
    }

    fun changeDateTimeEvent(date: String, time: String) {
        val datetime = convertDateTime2ISO_Instant(date, time)
        editedEvent.value = editedEvent.value?.copy(datetime = datetime)
    }

    fun changeContentEvent(content: String) {
        val text = content.trim()
        if (editedEvent.value?.content == text) {
            return
        }
        editedEvent.value = editedEvent.value?.copy(content = text)
    }

    fun changeLinkEvent(link: String) {
        val text = if (link.isEmpty())
            null
        else
            link.trim()

        if (editedEvent.value?.link == text) {
            return
        }
        editedEvent.value = editedEvent.value?.copy(link = text)
    }

    fun changeCoordsEvent(lat: String?, long: String?) {
        val coords = if (lat == null && long == null || lat == "" && long == "")
            null
        else
            Coordinates(lat, long)

        if (editedEvent.value?.coords == coords) {
            return
        }
        editedEvent.value = editedEvent.value?.copy(coords = coords)
    }

    fun changeSpeakersEvent(speakersStr: String) {
        if (speakersStr.isNotEmpty())
            try {
                val speakers = speakersStr.split(",").map {
                    it.trim().toLong()
                }
                editedEvent.value = editedEvent.value?.copy(speakerIds = speakers)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
    }

    fun changeTypeEvent(isOnline: Boolean) {
        if (isOnline)
            editedEvent.value = editedEvent.value?.copy(type = EventType.ONLINE)
        else
            editedEvent.value = editedEvent.value?.copy(type = EventType.OFFLINE)
    }

    fun removeEventById(id: Long) = viewModelScope.launch {
        try {
            repository.removeEventById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun likeEventById(id: Long, likedByMe: Boolean) = viewModelScope.launch {
        try {
            repository.likeEventById(id, likedByMe)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getEditEvent(): Event? {
        return editedEvent.value
    }

    fun participate(id: Long, participatedByMe: Boolean) = viewModelScope.launch {
        try {
            repository.partEventById(id, participatedByMe)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

}
