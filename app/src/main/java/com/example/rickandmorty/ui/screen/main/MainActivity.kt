package com.example.rickandmorty.ui.screen.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.rickandmorty.R
import com.example.rickandmorty.data.api.RickAndMortyAPIService
import com.example.rickandmorty.model.LocalCharacterModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /* val viewModel: MainActivityViewModel by viewModels {
         MainActivityViewModelFactory(applicationContext)
     }*/

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = hiltViewModel<MainActivityViewModel,
                    MainActivityViewModel.MainActivityViewModelFactory>() { factory ->
                factory.create(RickAndMortyAPIService())
            }
            AppUI(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterialApi
@Composable
fun AppUI(viewModel: MainActivityViewModel) {
    val appTitle = stringResource(id = R.string.app_name)
    var isAllChosen by remember { mutableStateOf(true) }
    val defaultPadding = 8.dp
    val buttonTextSize = 24f
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(appTitle) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            val items by viewModel.items.collectAsState()
            val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
            val pullRefreshState = rememberPullRefreshState(viewModel.isDataRefreshing(),
                { viewModel.refreshData() }
            )

            LaunchedEffect(errorMessage) {
                errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        .also { viewModel.clearErrorMessage() }
                }
            }

            Box(
                Modifier
                    .pullRefresh(pullRefreshState)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .weight(1f)
            ) {
                val listState = rememberLazyListState()
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(items) { character ->
                        ShowCharacterItem(character) { updatedCharacter ->
                            viewModel.updateItem(updatedCharacter)
                        }
                        if (listState.isScrolledToTheEnd()) {
                            viewModel.getCharacters(isAllChosen, isEnd = true)
                        }
                    }

                }
                PullRefreshIndicator(
                    viewModel.isDataRefreshing(),
                    pullRefreshState,
                    Modifier.align(Alignment.TopCenter)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(defaultPadding),
                horizontalArrangement = Arrangement.spacedBy(defaultPadding)
            ) {
                Button(
                    onClick = {
                        isAllChosen = true
                        viewModel.getCharacters(true, changedMode = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                ) {
                    Text(
                        text = stringResource(id = R.string.all),
                        color = getButtonColor(isAllChosen),
                        fontSize = TextUnit(buttonTextSize, TextUnitType.Sp)
                    )
                }

                VerticalDivider(thickness = 3.dp)

                Button(
                    onClick = {
                        isAllChosen = false
                        viewModel.getCharacters(false, changedMode = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.saved),
                        color = getButtonColor(!isAllChosen),
                        fontSize = TextUnit(buttonTextSize, TextUnitType.Sp)
                    )
                }
            }
        }
    }
}

fun LazyListState.isScrolledToTheEnd() =
    layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 == layoutInfo.totalItemsCount - 1

@Composable
private fun getButtonColor(isActive: Boolean): Color {
    return if (isActive) {
        colorResource(id = R.color.button_active)
    } else {
        colorResource(id = R.color.button_inactive)
    }
}

@Composable
private fun ShowCharacterItem(character: LocalCharacterModel, onToggleFavourite: (LocalCharacterModel) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val characterModel = character.character
            Text(
                characterModel.name,
                Modifier
                    .weight(1f)
                    .align(Alignment.Top)
                    .padding(top = 16.dp),
                fontSize = 20.sp
            )

            AsyncImage(
                model = characterModel.image,
                contentDescription = character.character.name,
                modifier = Modifier
                    .size(120.dp)
                    .padding(end = 8.dp)
            )

            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Star",
                tint = if (character.isFavourite) Color.Yellow else Color.Black,
                modifier = Modifier
                    .clickable {
                        onToggleFavourite(character)
                    }
                    .size(36.dp)
            )
        }
    }
}