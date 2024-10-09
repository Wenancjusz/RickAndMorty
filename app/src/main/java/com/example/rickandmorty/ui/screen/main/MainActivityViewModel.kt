package com.example.rickandmorty.ui.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmorty.data.api.ApiResult
import com.example.rickandmorty.data.api.ApiServiceInterface
import com.example.rickandmorty.data.localdb.CharacterEntity
import com.example.rickandmorty.data.localdb.RickAndMortyDatabase
import com.example.rickandmorty.model.CharacterModel
import com.example.rickandmorty.model.LocalCharacterModel
import com.example.rickandmorty.model.Location
import com.example.rickandmorty.model.Origin
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = MainActivityViewModel.MainActivityViewModelFactory::class)
class MainActivityViewModel @AssistedInject constructor(
    @Assisted val client: ApiServiceInterface,
    private val database: RickAndMortyDatabase
) : ViewModel() {

    private var nextPage = 1
    private var isLoading = false
    private var isRefreshing = false
    private var isOnFav = false

    @AssistedFactory
    interface MainActivityViewModelFactory {
        fun create(client: ApiServiceInterface): MainActivityViewModel
    }

    private val _items = MutableStateFlow<List<LocalCharacterModel>>(emptyList())
    val items: StateFlow<List<LocalCharacterModel>> = _items.asStateFlow()

    private var localDBItems: List<LocalCharacterModel> = emptyList()

    private val _apiResult = MutableSharedFlow<ApiResult<List<LocalCharacterModel>>>()
    val apiResult: SharedFlow<ApiResult<List<LocalCharacterModel>>> = _apiResult.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String?>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        getCharacters(apiRequest = true)
    }

    private suspend fun readFromDatabase() {
        isLoading = true
        localDBItems = database.characterDao().getCharacters().first()
            .map { LocalCharacterModel(it.toCharacterModel(), true) }
        isLoading = false
    }

    fun getCharacters(
        apiRequest: Boolean = true,
        isDataRefreshing: Boolean = false,
        isEnd: Boolean = false,
        changedMode: Boolean = false
    ) {
        isOnFav = !apiRequest
        if (isLoading || isOnFav && isEnd) return
        if (changedMode) {
            nextPage = 1
            _items.value = emptyList()
        }
        viewModelScope.launch(Dispatchers.IO) {
            readFromDatabase()
            if (apiRequest) {
                if (nextPage != -1) {
                    isLoading = true
                    try {
                        val response = client.fetchData(nextPage)
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            if (response is ApiResult.Success) {
                                if (isDataRefreshing) {
                                    isRefreshing = false
                                    _items.value = emptyList()
                                }
                                val apiItems = response.data.results
                                _items.value += apiItems.map {
                                    LocalCharacterModel(it,
                                        localDBItems.firstOrNull
                                        { local ->
                                            it.id == local.character.id
                                        } != null)
                                }
                                nextPage =
                                    response.data.info.next?.split("=")?.get(1)?.toInt() ?: -1
                            } else {
                                _errorMessage.emit((response as ApiResult.Error).message)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            if (isDataRefreshing) {
                                isRefreshing = false
                            }
                            isLoading = false
                            _errorMessage.emit("Network error: ${e.message}")
                        }
                    }
                }
            } else {
                _items.value = localDBItems
            }
        }
    }

    fun updateItem(item: LocalCharacterModel) {
        viewModelScope.launch {
            val characterDao = database.characterDao()
            val characterById = characterDao.getCharacterById(item.character.id).first()

            if (item.isFavourite) {
                if (characterById != null) {
                    characterDao.deleteCharacter(characterById)
                    _items.value = _items.value.map { existingItem ->
                        if (existingItem.character.id == item.character.id) {
                            item.copy(isFavourite = false)
                        } else {
                            existingItem
                        }
                    }
                }
            } else {
                characterDao.insertCharacter(item.toCharacterEntity())
                _items.value = _items.value.map { existingItem ->
                    if (existingItem.character.id == item.character.id) {
                        item.copy(isFavourite = true)
                    } else {
                        existingItem
                    }
                }
            }
        }
    }

    suspend fun clearErrorMessage() {
        _errorMessage.emit(null)
    }

    fun isDataRefreshing() = isRefreshing

    fun refreshData() {
        isRefreshing = true
        _items.value = emptyList()
        nextPage = 1
        getCharacters(!isOnFav, true)
        isRefreshing = false
    }

    fun LocalCharacterModel.toCharacterEntity() = CharacterEntity(
        character.id,
        character.name,
        character.image
    )

    fun CharacterEntity.toCharacterModel() = CharacterModel(
        id, name, "", "", "",
        "", Origin("", ""), Location("", ""),
        imageUrl ?: "", emptyList(), imageUrl ?: "", ""
    )
}