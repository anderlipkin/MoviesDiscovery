package com.example.moviesdiscovery.features.movies.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeMoviesScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeMoviesViewModel = koinViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val tabsContent = HomeMoviesTab.entries.map {
        when (it) {
            HomeMoviesTab.Movies -> it.moviesScreen()
            HomeMoviesTab.Favorites -> it.favoriteScreen(
                onGoToLibraryClick = { viewModel.updateSelectedTab(HomeMoviesTab.Movies) }
            )
        }
    }
    HomeMoviesScreen(
        selectedTab = selectedTab,
        tabsContent = tabsContent,
        onTabSelect = viewModel::updateSelectedTab,
        modifier = modifier
    )
}

@Composable
private fun HomeMoviesScreen(
    selectedTab: HomeMoviesTab,
    tabsContent: List<HomeMoviesTabContent>,
    onTabSelect: (HomeMoviesTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            HomeMoviesTabRowContent(selectedTab.ordinal, onTabSelect, tabsContent)
        }
        Box(modifier = Modifier.weight(1f)) {
            tabsContent[selectedTab.ordinal].content()
        }
    }
}

@Composable
private fun HomeMoviesTabRowContent(
    selectedTabIndex: Int,
    onTabClick: (HomeMoviesTab) -> Unit,
    tabsContent: List<HomeMoviesTabContent>
) {
    tabsContent.forEachIndexed { index, tabContent ->
        Tab(
            selected = selectedTabIndex == index,
            unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            onClick = { onTabClick(tabContent.tab) },
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(
                text = stringResource(id = tabContent.tab.titleResId),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
