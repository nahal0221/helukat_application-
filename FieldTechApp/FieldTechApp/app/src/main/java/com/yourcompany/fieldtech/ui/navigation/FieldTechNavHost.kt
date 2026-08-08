package com.yourcompany.fieldtech.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.yourcompany.fieldtech.ui.jobdetail.JobDetailScreen
import com.yourcompany.fieldtech.ui.jobs.JobListScreen
import com.yourcompany.fieldtech.ui.login.LoginScreen

object Routes {
    const val LOGIN = "login"
    const val JOB_LIST = "jobs"
    const val JOB_DETAIL = "jobs/{jobId}"
    fun jobDetail(jobId: Long) = "jobs/$jobId"
}

@Composable
fun FieldTechNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.JOB_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.JOB_LIST) {
            JobListScreen(
                onJobClick = { jobId -> navController.navigate(Routes.jobDetail(jobId)) }
            )
        }

        composable(
            route = Routes.JOB_DETAIL,
            arguments = listOf(navArgument("jobId") { type = NavType.LongType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getLong("jobId") ?: return@composable
            JobDetailScreen(jobId = jobId, onBack = { navController.popBackStack() })
        }
    }
}
