package com.krew18.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.krew18.app.domain.model.UserRole

object Routes {
    const val ARG_GIG_ID = "gigId"

    // Talent
    const val JOBS = "talent/jobs"
    const val JOB_DETAIL = "talent/jobs/{$ARG_GIG_ID}"
    const val MY_APPLICATIONS = "talent/applications"
    const val PROFILE = "talent/profile"

    // Employer
    const val EMPLOYER_GIGS = "employer/gigs"
    const val POST_GIG = "employer/post"
    const val APPLICANTS = "employer/gigs/{$ARG_GIG_ID}/applicants"
    const val EMPLOYER_ACCOUNT = "employer/account"

    fun jobDetail(gigId: String) = "talent/jobs/$gigId"
    fun applicants(gigId: String) = "employer/gigs/$gigId/applicants"
}

enum class TopLevelDestination(val route: String, val label: String, val icon: ImageVector) {
    JOBS(Routes.JOBS, "Jobs", Icons.Filled.Work),
    APPLICATIONS(Routes.MY_APPLICATIONS, "Applications", Icons.Filled.Description),
    PROFILE(Routes.PROFILE, "Profile", Icons.Filled.Person),
    EMPLOYER_GIGS(Routes.EMPLOYER_GIGS, "My gigs", Icons.Filled.Business),
    POST_GIG(Routes.POST_GIG, "Post a gig", Icons.Filled.AddCircle),
    EMPLOYER_ACCOUNT(Routes.EMPLOYER_ACCOUNT, "Account", Icons.Filled.Person),
    ;

    companion object {
        fun forRole(role: UserRole): List<TopLevelDestination> = when (role) {
            UserRole.TALENT -> listOf(JOBS, APPLICATIONS, PROFILE)
            UserRole.EMPLOYER -> listOf(EMPLOYER_GIGS, POST_GIG, EMPLOYER_ACCOUNT)
        }
    }
}
