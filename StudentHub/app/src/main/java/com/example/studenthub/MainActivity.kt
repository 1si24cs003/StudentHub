package com.example.studenthub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import com.example.studenthub.notification.NotificationHelper
import com.example.studenthub.ui.AcademicsScreen
import com.example.studenthub.ui.AiAssistantScreen
import com.example.studenthub.ui.DashboardScreen
import com.example.studenthub.ui.PersonalScreen
import com.example.studenthub.ui.theme.StudentHubTheme
import com.example.studenthub.viewmodel.AiViewModel
import com.example.studenthub.viewmodel.StudentHubViewModel


import androidx.navigation.NavController
import kotlinx.coroutines.delay

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Splash : Screen("splash", "Splash", Icons.Default.Dashboard)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Academics : Screen("academics", "Academics", Icons.Default.Book)
    object Personal : Screen("personal", "Personal", Icons.Default.Person)
    object AiAssistant : Screen("ai_assistant", "AI Assistant", Icons.Default.AutoAwesome)
}

val items = listOf(
    Screen.Dashboard,
    Screen.Academics,
    Screen.Personal,
    Screen.AiAssistant
)

class MainActivity : ComponentActivity() {
    private val studentHubViewModel: StudentHubViewModel by viewModels()
    private val aiViewModel: AiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        NotificationHelper.scheduleDailySummary(this)
        
        enableEdgeToEdge()
        setContent {
            StudentHubTheme {
                MainScreen(studentHubViewModel, aiViewModel)
            }
        }
    }
}

@Composable
fun MainScreen(
    studentHubViewModel: StudentHubViewModel,
    aiViewModel: AiViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val showBottomBar = currentDestination?.route != Screen.Splash.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) { SplashScreen(navController) }
            composable(Screen.Dashboard.route) { DashboardScreen(studentHubViewModel) }
            composable(Screen.Academics.route) { AcademicsScreen(studentHubViewModel) }
            composable(Screen.Personal.route) { PersonalScreen(studentHubViewModel) }
            composable(Screen.AiAssistant.route) { AiAssistantScreen(aiViewModel) }
        }
    }
}

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Student Hub", style = MaterialTheme.typography.headlineMedium)
        }
        
        Text(
            text = "Made by Abdullah",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
