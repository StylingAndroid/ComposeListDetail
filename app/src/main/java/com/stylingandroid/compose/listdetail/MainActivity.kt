package com.stylingandroid.compose.listdetail

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.window.FoldingFeature
import androidx.window.WindowLayoutInfo
import androidx.window.WindowManager
import com.stylingandroid.compose.listdetail.ui.theme.ComposeListDetailTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect

class MainActivity : ComponentActivity() {

    private lateinit var windowStateJob: Job

    @OptIn(ExperimentalCoroutinesApi::class)
    fun windowStateFlow(): Flow<WindowLayoutInfo> =
        callbackFlow<WindowLayoutInfo> {
            val windowManager = WindowManager(this@MainActivity)
            val consumer = Consumer<WindowLayoutInfo> { newLayoutInfo ->
                sendBlocking(newLayoutInfo)
            }
            windowManager.registerLayoutChangeCallback(
                executor = ContextCompat.getMainExecutor(this@MainActivity),
                callback = consumer
            )
            awaitClose {
                windowManager.unregisterLayoutChangeCallback(consumer)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var windowState by mutableStateOf(WindowLayoutInfo.Builder().build())

        windowStateJob = lifecycleScope.launchWhenStarted {
            windowStateFlow()
                .collect { windowLayoutInfo ->
                    windowState = windowLayoutInfo
                }
        }

        setContent {
            ComposeListDetailTheme {
                Surface(color = MaterialTheme.colors.background) {
                    @Suppress("MagicNumber")
                    ListDetailLayout(
                        (1..10).map { index -> "Item $index" },
                        LocalConfiguration.current,
                        windowState
                    ) {
                        List { list, onSelectionChange ->
                            MyList(list, onSelectionChange)
                        }
                        Detail { text ->
                            Text(text = text)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        windowStateJob.cancel()
        super.onStop()
    }
}

private object NavGraph {
    sealed class Route(val route: String) {
        object Detail : Route("detail/{selected}") {
            fun navigateRoute(selected: String?) = "detail/$selected"
        }
    }
}

@Composable
@Suppress("MagicNumber")
fun ListDetailLayout(
    list: List<String>,
    configuration: Configuration,
    windowLayoutInfo: WindowLayoutInfo,
    scope: @Composable TwoPaneScope<String>.() -> Unit
) {
    val isSmallScreen = configuration.smallestScreenWidthDp < 580
    val navController = rememberNavController()
    val twoPaneScope = TwoPaneScopeImpl(list).apply { scope() }

    NavHost(navController = navController, startDestination = NavGraph.Route.Detail.route) {
        composable(NavGraph.Route.Detail.route) { navBackStackEntry ->
            val selected = navBackStackEntry.arguments?.getString("selected")
            if (isSmallScreen) {
                TwoPageLayout(twoPaneScope, selected) { selection ->
                    navController.navigate(route = NavGraph.Route.Detail.navigateRoute(selection)) {
                        popUpTo(NavGraph.Route.Detail.navigateRoute(null)) {
                            inclusive = true
                        }
                    }
                }
                BackHandler(true) {
                    navController.popBackStack()
                }
            } else {
                SplitLayout(twoPaneScope, windowLayoutInfo, selected) { selection ->
                    navController.navigate(route = NavGraph.Route.Detail.navigateRoute(selection)) {
                        popUpTo(NavGraph.Route.Detail.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TwoPageLayout(
    twoPaneScope: TwoPaneScopeImpl<String>,
    selected: String?,
    onSelectionChange: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (selected == null) {
            twoPaneScope.list(twoPaneScope.items, onSelectionChange)
        } else {
            twoPaneScope.detail(selected)
        }
    }
}

@Composable
private fun SplitLayout(
    twoPaneScope: TwoPaneScopeImpl<String>,
    windowLayoutInfo: WindowLayoutInfo,
    selected: String?,
    onSelectionChange: (String) -> Unit
) {
    Row(Modifier.fillMaxWidth()) {
        val displayFeatureOffset = displayFeatureOffsetDp(LocalDensity.current, windowLayoutInfo)
        Box(modifier = displayFeatureOffset?.let { Modifier.width(it) } ?: Modifier.weight(1f)) {
            twoPaneScope.list(twoPaneScope.items, onSelectionChange)
        }
        Box(modifier = Modifier.weight(1f)) {
            twoPaneScope.detail(selected ?: "Nothing selected")
        }
    }
}

private fun displayFeatureOffsetDp(
    density: Density,
    windowLayoutInfo: WindowLayoutInfo
): Dp? {
    val displayFeatureOffset: Int? = windowLayoutInfo.displayFeatures
        .filter { (it as? FoldingFeature)?.orientation == FoldingFeature.ORIENTATION_VERTICAL }
        .map { it.bounds.left }
        .firstOrNull()
    return density.run {
        displayFeatureOffset?.toDp()
    }
}

@Composable
private fun MyList(
    list: List<String>,
    onSelectionChange: (String) -> Unit
) {
    LazyColumn {
        for (entry in list) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSelectionChange(entry) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = entry)
                }
            }
        }
    }
}

@Immutable
interface TwoPaneScope<T> {
    val list: @Composable (List<T>, (T) -> Unit) -> Unit
    val detail: @Composable (T) -> Unit

    @Composable
    fun List(newList: @Composable (List<T>, (T) -> Unit) -> Unit)

    @Composable
    fun Detail(newDetail: @Composable (T) -> Unit)
}

private class TwoPaneScopeImpl<T>(
    val items: List<T>
) : TwoPaneScope<T> {
    override var list: @Composable (List<T>, (T) -> Unit) -> Unit = { _, _ -> }
        private set

    override var detail: @Composable (T) -> Unit = {}
        private set

    @Composable
    override fun List(newList: @Composable (List<T>, (T) -> Unit) -> Unit) {
        list = newList
    }

    @Composable
    override fun Detail(newDetail: @Composable (T) -> Unit) {
        detail = newDetail
    }
}
