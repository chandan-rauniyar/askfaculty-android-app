package com.chandan.askfaculty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.chandan.askfaculty.data.local.SessionManager
import com.chandan.askfaculty.data.repository.AuthRepository
import com.chandan.askfaculty.ui.auth.ActivationScreen
import com.chandan.askfaculty.ui.auth.ForgotPasswordScreen
import com.chandan.askfaculty.ui.auth.LoginScreen
import com.chandan.askfaculty.ui.auth.ResetPasswordScreen
import com.chandan.askfaculty.ui.home.HomeShell
import com.chandan.askfaculty.ui.theme.AskFacultyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthFlowScreen {
    LOGIN, ACTIVATE, FORGOT, RESET
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AskFacultyTheme {
                val session by sessionManager.session.collectAsState()
                val coroutineScope = rememberCoroutineScope()
                var authScreen by remember { mutableStateOf(AuthFlowScreen.LOGIN) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (session.isLoggedIn) {
                        HomeShell(
                            user = session.user,
                            onLogout = {
                                coroutineScope.launch {
                                    authRepository.logout()
                                }
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        when (authScreen) {
                            AuthFlowScreen.LOGIN -> LoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLoginSuccess = {},
                                onNavigateToActivate = { authScreen = AuthFlowScreen.ACTIVATE },
                                onNavigateToForgot = { authScreen = AuthFlowScreen.FORGOT },
                                onNavigateToReset = { authScreen = AuthFlowScreen.RESET }
                            )
                            AuthFlowScreen.ACTIVATE -> ActivationScreen(
                                modifier = Modifier.padding(innerPadding),
                                onActivationSuccess = { authScreen = AuthFlowScreen.LOGIN },
                                onBackToLogin = { authScreen = AuthFlowScreen.LOGIN }
                            )
                            AuthFlowScreen.FORGOT -> ForgotPasswordScreen(
                                modifier = Modifier.padding(innerPadding),
                                onBackToLogin = { authScreen = AuthFlowScreen.LOGIN }
                            )
                            AuthFlowScreen.RESET -> ResetPasswordScreen(
                                modifier = Modifier.padding(innerPadding),
                                onResetSuccess = { authScreen = AuthFlowScreen.LOGIN },
                                onBackToLogin = { authScreen = AuthFlowScreen.LOGIN }
                            )
                        }
                    }
                }
            }
        }
    }
}
