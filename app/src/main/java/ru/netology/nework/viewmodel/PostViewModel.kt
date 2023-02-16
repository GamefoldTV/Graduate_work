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
import ru.netology.nework.dto.*
import ru.netology.nework.dto.Post
import ru.netology.nework.model.FeedModel
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject


private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    published = "",
    likedByMe = false,
)

private val noPhoto = PhotoModel()
private val noCoords = Coordinates()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val auth: AppAuth,
) : ViewModel() {
  val data: LiveData<FeedModel> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.posts
                .map{ posts ->
                    FeedModel(
                        posts.map { it.copy(ownedByMe = it.authorId == myId) },
                        posts.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

  //  private val _authState = MutableLiveData<LoginFormState>()
  //  val authState: LiveData<LoginFormState>
  //      get() = _authState

    private val editedPost = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    private val _coords = MutableLiveData(noCoords)
    val coords: LiveData<Coordinates>
        get() = _coords

    init {
        loadPosts()
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

    fun save() {
        editedPost.value?.let { post ->
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when(_photo.value) {
                        noPhoto -> {
                            var postNew = post
                            if (post.attachment != null)
                                postNew = post.copy(attachment = null)
                            repository.save(postNew)
                        }
                        else -> _photo.value?.file?.let { file ->
                            repository.saveWithAttachment(post, MediaRequest(file))
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        editedPost.value = empty
        _photo.value = noPhoto
        _coords.value = noCoords
    }

    fun edit(post: Post) {
        editedPost.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (editedPost.value?.content == text) {
            return
        }
        editedPost.value = editedPost.value?.copy(content = text)
    }

    fun changeLink(link : String){
        val text = if (link.isEmpty())
            null
        else
            link.trim()

        if (editedPost.value?.link == text) {
            return
        }
        editedPost.value = editedPost.value?.copy(link = text)
    }

    fun changeCoords(lat: String?, long: String?){
        val coords = if (lat==null && long==null)
            null
        else
            Coordinates(lat,long)

        if (editedPost.value?.coords == coords) {
            return
        }
        editedPost.value = editedPost.value?.copy(coords = coords)
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
     //   editedPost.value = editedPost.value?.copy(attachment = Attachment(uri.toString(), AttachmentType.IMAGE))
    }

    fun removeAttachment(){
        editedPost.value = editedPost.value?.copy(attachment = null)
    }

    fun changeCoordsFromMap(lat: String, long: String){
        _coords.value = if (lat.isBlank() && long.isBlank())
            null
        else
            Coordinates(lat,long)
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun likeById(id: Long)=  viewModelScope.launch {
        try {
            repository.likeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }
    fun getEditPost() : Post? {
        return editedPost.value
    }

}
