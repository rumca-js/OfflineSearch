package com.example.index.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.index.data.AppConfigManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Link
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.index.ui.SearchViewModel
import coil.compose.AsyncImage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class Place(
    val title: String? = null,
    val description: String? = null,
    val link: String? = null,
    val tags: List<String>? = null,
    val page_rating_votes: Int? = 0,
    val page_rating: Int? = 0,
    val thumbnail: String? = null,
    val date_created: String? = null,
    val date_published: String? = null
)

private val jsonConfig = Json { 
    ignoreUnknownKeys = true 
    coerceInputValues = true
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: SearchViewModel = viewModel(),
    onNavigateToDetail: (Place) -> Unit = {}
) {
    val context = LocalContext.current

    // Load data once
    LaunchedEffect(Unit) {
        viewModel.loadDataIfNeeded(context)
    }

    val searchQuery = viewModel.searchQuery
    val isLoading = viewModel.isLoading
    val filteredData = viewModel.filteredData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            label = { Text("Search Places") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Type at least 2 characters...") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredData) { place ->
                    PlaceItem(place, onNavigateToDetail)
                }
                if (searchQuery.length >= 2 && filteredData.isEmpty()) {
                    item {
                        Text("No results found for \"$searchQuery\"")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaceItem(place: Place, onClick: (Place) -> Unit) {
    val uriHandler = LocalUriHandler.current
    val config by AppConfigManager.config.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = place.link != null || !config.directLinks) {
                if (config.directLinks) {
                    place.link?.let { uriHandler.openUri(it) }
                } else {
                    onClick(place)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    if (config.showIcons) {
                        if (place.thumbnail != null) {
                            AsyncImage(
                                model = place.thumbnail,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp)
                            )
                        } else if (place.link != null) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = place.title ?: "No Title",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                place.page_rating_votes?.let { votes ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Text(
                            text = "⭐ $votes",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            place.description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            place.link?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            place.tags?.let { tags ->
                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tags.forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun loadPlacesFromAssets(context: Context): List<Place> = withContext(Dispatchers.IO) {
    val assets = listOf("places_0.json",
        "places_1.json",
        "places_2.json",
        "places_3.json",
        "places_4.json",
        "places_5.json",
        "places_6.json",
        "places_7.json",
        "places_8.json",
        "places_9.json",
        "places_10.json",
        )
    val allPlaces = mutableListOf<Place>()
    
    assets.forEach { fileName ->
        try {
            context.assets.open(fileName).bufferedReader().use { reader ->
                val jsonString = reader.readText()
                val places: List<Place> = jsonConfig.decodeFromString(jsonString)
                allPlaces.addAll(places)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    allPlaces
}
