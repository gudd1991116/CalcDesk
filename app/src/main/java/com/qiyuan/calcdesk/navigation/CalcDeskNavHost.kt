package com.qiyuan.calcdesk.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qiyuan.calcdesk.ui.home.HomeScreen
import com.qiyuan.calcdesk.ui.pages.AboutScreen
import com.qiyuan.calcdesk.ui.pages.ContactScreen
import com.qiyuan.calcdesk.ui.pages.PrivacyScreen
import com.qiyuan.calcdesk.ui.tools.TimestampSnippetsScreen
import com.qiyuan.calcdesk.ui.tools.ToolScreen

private const val ENTER_MS = 220
private const val SLIDE_MS = 280

@Composable
fun CalcDeskNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            fadeIn(tween(ENTER_MS)) + slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth / 12 },
                animationSpec = tween(SLIDE_MS),
            )
        },
        exitTransition = {
            fadeOut(tween(ENTER_MS)) + slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 12 },
                animationSpec = tween(SLIDE_MS),
            )
        },
        popEnterTransition = {
            fadeIn(tween(ENTER_MS)) + slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 12 },
                animationSpec = tween(SLIDE_MS),
            )
        },
        popExitTransition = {
            fadeOut(tween(ENTER_MS)) + slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth / 12 },
                animationSpec = tween(SLIDE_MS),
            )
        },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenTool = { id -> navController.navigate(Routes.tool(id)) },
                onAbout = { navController.navigate(Routes.ABOUT) },
                onPrivacy = { navController.navigate(Routes.PRIVACY) },
                onContact = { navController.navigate(Routes.CONTACT) },
            )
        }
        composable(
            route = Routes.TOOL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            ToolScreen(
                toolId = id,
                onBack = { navController.popBackStack() },
                onOpenTimestampSnippets = {
                    navController.navigate(Routes.TIMESTAMP_SNIPPETS)
                },
            )
        }
        composable(Routes.TIMESTAMP_SNIPPETS) {
            TimestampSnippetsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PRIVACY) {
            PrivacyScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CONTACT) {
            ContactScreen(onBack = { navController.popBackStack() })
        }
    }
}
