package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.screens.AiDiscussionScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.SearchKnowledgeScreen
import com.example.ui.theme.DiskusikuTheme
import com.example.ui.viewmodel.DiscussionViewModel

enum class AppNavDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    SEARCH(
        label = "Telusur & DB",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    DISCUSSION(
        label = "Diskusi AI",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    ),
    BOOKMARKS(
        label = "Tersimpan",
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder
    )
}

class MainActivity : ComponentActivity() {
    private val discussionViewModel: DiscussionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiskusikuTheme {
                DiskusikuApp(viewModel = discussionViewModel)
            }
        }
    }
}

@Composable
fun DiskusikuApp(
    viewModel: DiscussionViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppNavDestination.SEARCH) }

    // Adaptive Navigation Suite Scaffold (M3 Expressive Core: Adapts across Phone, Tablet, & Foldable)
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppNavDestination.entries.forEach { destination ->
                val isSelected = currentDestination == destination
                item(
                    selected = isSelected,
                    onClick = { currentDestination = destination },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.label
                        )
                    },
                    label = {
                        Text(text = destination.label)
                    }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        when (currentDestination) {
            AppNavDestination.SEARCH -> {
                SearchKnowledgeScreen(
                    viewModel = viewModel,
                    onNavigateToAiDiscussion = {
                        currentDestination = AppNavDestination.DISCUSSION
                    }
                )
            }
            AppNavDestination.DISCUSSION -> {
                AiDiscussionScreen(viewModel = viewModel)
            }
            AppNavDestination.BOOKMARKS -> {
                BookmarksScreen(viewModel = viewModel)
            }
        }
    }
}
