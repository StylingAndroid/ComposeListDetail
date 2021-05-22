package com.stylingandroid.compose.listdetail

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stylingandroid.compose.listdetail.ui.theme.ComposeListDetailTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeListDetailTheme {
                Surface(color = MaterialTheme.colors.background) {
                    @Suppress("MagicNumber")
                    DynamicLayout(
                        (1..10).map { index -> "Item $index" },
                        LocalConfiguration.current
                    )
                }
            }
        }
    }
}

@Composable
@Suppress("MagicNumber")
fun DynamicLayout(
    list: List<String>,
    configuration: Configuration
) {
    if (configuration.smallestScreenWidthDp < 580) {
        TwoPageLayout(list)
    } else {
        SplitLayout(list)
    }
}

@Composable
fun SplitLayout(list: List<String>) {
    var selected by rememberSaveable { mutableStateOf("") }

    Row(Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f)) {
            List(list = list) { newSelection ->
                selected = newSelection
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            Detail(text = selected)
        }
    }
}

@Composable
fun TwoPageLayout(list: List<String>) {
    val navController = rememberNavController()
    val detailRoute = NavGraph.Route.Detail
    NavHost(navController = navController, startDestination = "list") {
        composable(route = NavGraph.Route.List.route) {
            List(list = list) { selected ->
                navController.navigate(route = detailRoute.navigateRoute(selected))
            }
        }
        composable(route = detailRoute.route) { backStackEntry ->
            Detail(text = backStackEntry.arguments?.getString("selected") ?: "")
        }
    }
}

private object NavGraph {
    sealed class Route(val route: String) {
        object List : Route("list")
        object Detail : Route("detail/{selected}") {
            fun navigateRoute(selected: String) = "detail/$selected"
        }
    }
}

@Composable
fun List(list: List<String>, onSelectionChange: (String) -> Unit) {
    LazyColumn() {
        for (entry in list) {
            item() {
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

@Composable
fun Detail(text: String) {
    Text(text = text)
}
