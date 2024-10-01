package com.example.rickandmorty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.rickandmorty.model.CharacterModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                AppUI()
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUI() {
    val appTitle = stringResource(id = R.string.app_name)
    var isAllChosen by remember { mutableStateOf(true) }
    val buttonTextSize = 24f
    val defaultPadding = 8.dp

    //TODO: Replace with real data
    val items = remember {
        mutableStateListOf(
            CharacterModel("Rick Sanchez", "https://rickandmortyapi.com/api/character/avatar/1.jpeg", true),
            CharacterModel("Morty Smith", "https://rickandmortyapi.com/api/character/avatar/2.jpeg", false),
            CharacterModel("Beth Smith", "https://rickandmortyapi.com/api/character/avatar/3.jpeg", true),
            CharacterModel("Jerry Smith", "https://rickandmortyapi.com/api/character/avatar/4.jpeg", true),
            CharacterModel("Summer Smith", "https://rickandmortyapi.com/api/character/avatar/5.jpeg", false)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(appTitle) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(16.dp)
            ) {
                items(items) { character ->
                    showCharacterItem(
                        character = character,
                        onToggleFavourite = { updatedCharacter ->
                            val index = items.indexOf(character)
                            if (index != -1) {
                                items[index] = updatedCharacter
                            }
                            // TODO: Update DB
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(defaultPadding)
                    .height(45.dp),
                horizontalArrangement = Arrangement.spacedBy(defaultPadding)
            ) {
                Button(
                    onClick = { isAllChosen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = stringResource(id = R.string.all),
                        color = getButtonColor(isAllChosen),
                        fontSize = TextUnit(buttonTextSize, TextUnitType.Sp)
                    )
                }

                VerticalDivider(thickness = 3.dp)

                Button(
                    onClick = { isAllChosen = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxSize()
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

@Composable
private fun getButtonColor(isActive: Boolean): Color {
    return if (isActive) {
        colorResource(id = R.color.button_active)
    } else {
        colorResource(id = R.color.button_inactive)
    }
}

@Composable
private fun showCharacterItem(character: CharacterModel, onToggleFavourite: (CharacterModel) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(text = character.name, Modifier.weight(1f).align(Alignment.Top).padding(top = 16.dp), fontSize = TextUnit(20f, TextUnitType.Sp))

            AsyncImage(
                model = character.image,
                contentDescription = character.name,
                modifier = Modifier.size(120.dp).padding(end = 8.dp)
            )

            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Star",
                tint = if (character.isFavourite) Color.Yellow else Color.Black,
                modifier = Modifier
                    .clickable { onToggleFavourite(character.copy(isFavourite = !character.isFavourite)) }
                    .size(36.dp)
            )

        }
    }
}