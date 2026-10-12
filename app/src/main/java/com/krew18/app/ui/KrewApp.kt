package com.krew18.app.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.krew18.app.domain.model.ThemeMode
import com.krew18.app.domain.model.UserRole
import com.krew18.app.ui.components.LoadingBox
import com.krew18.app.ui.employer.account.EmployerAccountScreen
import com.krew18.app.ui.employer.applicants.ApplicantsScreen
import com.krew18.app.ui.employer.gigs.EmployerGigsScreen
import com.krew18.app.ui.employer.post.PostGigScreen
import com.krew18.app.ui.navigation.Routes
import com.krew18.app.ui.navigation.TopLevelDestination
import com.krew18.app.ui.onboarding.OnboardingScreen
import com.krew18.app.ui.talent.applications.MyApplicationsScreen
import com.krew18.app.ui.talent.detail.JobDetailScreen
import com.krew18.app.ui.talent.feed.JobFeedScreen
import com.krew18.app.ui.talent.profile.ProfileScreen

/** Root composable: onboarding until a role is chosen, then the role-specific main UI. */
@Composable
fun KrewApp(
    state: AppUiState,
    onRoleSelected: (UserRole) -> Unit,
    onSwitchRole: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    when (state) {
        AppUiState.Loading -> LoadingBox()
        is AppUiState.Ready -> {
            val role = state.role
            if (role == null) {
                OnboardingScreen(onRoleSelected = onRoleSelected)
            } else {
                // A fresh nav controller per role, so switching roles resets the back stack.
                key(role) {
                    MainScaffold(
                        role = role,
                        themeMode = state.themeMode,
                        onSwitchRole = onSwitchRole,
                        onThemeModeChange = onThemeModeChange,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MainScaffold(
    role: UserRole,
    themeMode: ThemeMode,
    onSwitchRole: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val navController = rememberNavController()
    val destinations = TopLevelDestination.forRole(role)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = destinations.any { it.route == currentRoute }

    Scaffold(
        // Inner screens draw their own top app bars and handle status bar insets.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navController.navigateToTopLevel(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = destinations.first().route,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            talentGraph(navController, themeMode, onSwitchRole, onThemeModeChange)
            employerGraph(navController, themeMode, onSwitchRole, onThemeModeChange)
        }
    }
}

private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private val gigIdArgument = listOf(navArgument(Routes.ARG_GIG_ID) { type = NavType.StringType })

private fun NavGraphBuilder.talentGraph(
    navController: NavHostController,
    themeMode: ThemeMode,
    onSwitchRole: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    composable(Routes.JOBS) {
        JobFeedScreen(onGigClick = { navController.navigate(Routes.jobDetail(it)) })
    }
    composable(Routes.JOB_DETAIL, arguments = gigIdArgument) {
        JobDetailScreen(onBack = { navController.popBackStack() })
    }
    composable(Routes.MY_APPLICATIONS) {
        MyApplicationsScreen(
            onGigClick = { navController.navigate(Routes.jobDetail(it)) },
            onBrowseGigs = { navController.navigateToTopLevel(Routes.JOBS) },
        )
    }
    composable(Routes.PROFILE) {
        ProfileScreen(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            onSwitchRole = onSwitchRole,
        )
    }
}

private fun NavGraphBuilder.employerGraph(
    navController: NavHostController,
    themeMode: ThemeMode,
    onSwitchRole: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    composable(Routes.EMPLOYER_GIGS) {
        EmployerGigsScreen(
            onGigClick = { navController.navigate(Routes.applicants(it)) },
            onPostGig = { navController.navigateToTopLevel(Routes.POST_GIG) },
        )
    }
    composable(Routes.POST_GIG) {
        PostGigScreen(onViewMyGigs = { navController.navigateToTopLevel(Routes.EMPLOYER_GIGS) })
    }
    composable(Routes.APPLICANTS, arguments = gigIdArgument) {
        ApplicantsScreen(onBack = { navController.popBackStack() })
    }
    composable(Routes.EMPLOYER_ACCOUNT) {
        EmployerAccountScreen(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            onSwitchRole = onSwitchRole,
        )
    }
}
