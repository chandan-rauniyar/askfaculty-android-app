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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.chandan.askfaculty.data.local.SessionManager
import com.chandan.askfaculty.data.repository.AuthRepository
import com.chandan.askfaculty.ui.auth.LoginScreen
import com.chandan.askfaculty.ui.home.HomeShell
import com.chandan.askfaculty.ui.theme.AskFacultyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

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
                        LoginScreen(
                            modifier = Modifier.padding(innerPadding),
                            onLoginSuccess = {}
                        )
                    }
                }
            }
        }
    }
}
