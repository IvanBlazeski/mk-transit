package mk.fikt.mktransit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import mk.fikt.mktransit.ui.navigation.NavGraph
import mk.fikt.mktransit.ui.theme.MKTransitTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MKTransitTheme {
                NavGraph()
            }
        }
    }
}