package com.example.rickandmorty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rickandmorty.data.api.ApiResult
import com.example.rickandmorty.data.api.ApiServiceInterface
import com.example.rickandmorty.data.api.RickAndMortyAPIService
import com.example.rickandmorty.model.LocalCharacterModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class MainActivityViewModel(
    private val client: ApiServiceInterface,
    private val requestDispatcher: CoroutineContext = Dispatchers.IO
) : ViewModel() {

    private var nextPage = 1
    private var isLoading = false
    private var isRefreshing = false

    private val _items = MutableStateFlow<List<LocalCharacterModel>>(emptyList())
    val items: StateFlow<List<LocalCharacterModel>> = _items.asStateFlow()

    private val _apiResult = MutableSharedFlow<ApiResult<List<LocalCharacterModel>>>()
    val apiResult: SharedFlow<ApiResult<List<LocalCharacterModel>>> = _apiResult.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String?>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        getCharacters(true)
    }

    private fun readFromDatabase() {
        val favourites = _items.value.filter { it.isFavourite }
        _items.value = favourites
        nextPage = 1
    }

    fun getCharacters(apiRequest: Boolean = true, isDataRefreshing: Boolean = false) {
        if (isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
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
                                _items.value = if (_items.value.isEmpty()) {
                                    apiItems.map { LocalCharacterModel(it, false) }
                                } else {
                                    _items.value + apiItems.map { LocalCharacterModel(it, false) }
                                }
                                nextPage = response.data.info.next?.split("=")?.get(1)?.toInt() ?: -1
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
                readFromDatabase()
            }
        }
    }

    fun updateItem(item: LocalCharacterModel) {
        _items.value = _items.value.map { existingItem ->
            if (existingItem.character.id == item.character.id) {
                item
            } else {
                existingItem
            }
        }
    }

    fun refreshData() {
        isRefreshing = true
        nextPage = 1
        getCharacters(true, true)
    }

    suspend fun clearErrorMessage() {
        _errorMessage.emit(null)
        isRefreshing = false
    }

    fun isDataRefreshing() = isRefreshing
}

class MainActivityViewModelFactory: ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(RickAndMortyAPIService()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}