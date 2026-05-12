package mk.fikt.mktransit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import dagger.hilt.android.AndroidEntryPoint
import mk.fikt.mktransit.ui.navigation.NavGraph
import mk.fikt.mktransit.ui.theme.MKTransitTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MKTransitTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val isTablet = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
                NavGraph(isTablet = isTablet)
            }
        }
    }
}