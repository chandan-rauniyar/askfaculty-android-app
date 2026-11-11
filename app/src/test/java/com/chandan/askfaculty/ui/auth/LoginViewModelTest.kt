package com.chandan.askfaculty.ui.auth

import app.cash.turbine.test
import com.chandan.askfaculty.data.remote.AppError
import com.chandan.askfaculty.data.remote.model.User
import com.chandan.askfaculty.data.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoginSuccess() = runTest {
        val mockUser = User(id = "1", email = "student1@college.test", role = "STUDENT", status = "ACTIVE", name = "Student 1")
        coEvery { authRepository.login("student1@college.test", "password123") } returns mockUser

        viewModel.uiState.test {
            assertEquals(LoginUiState.Idle, awaitItem())

            viewModel.login("student1@college.test", "password123")

            assertEquals(LoginUiState.Loading, awaitItem())
            assertEquals(LoginUiState.Success, awaitItem())
        }
    }

    @Test
    fun testLoginInvalidCredentials() = runTest {
        coEvery { authRepository.login(any(), any()) } throws AppError.InvalidCredentials("Invalid credentials", null)

        viewModel.uiState.test {
            assertEquals(LoginUiState.Idle, awaitItem())

            viewModel.login("wrong@college.test", "wrongpass")

            assertEquals(LoginUiState.Loading, awaitItem())
            val errorState = awaitItem() as LoginUiState.Error
            assertEquals("Invalid email or password.", errorState.message)
        }
    }

    @Test
    fun testLoginAccountInactive() = runTest {
        coEvery { authRepository.login(any(), any()) } throws AppError.AccountInactive("Account inactive", null)

        viewModel.uiState.test {
            assertEquals(LoginUiState.Idle, awaitItem())

            viewModel.login("inactive@college.test", "password123")

            assertEquals(LoginUiState.Loading, awaitItem())
            val errorState = awaitItem() as LoginUiState.Error
            assertEquals("Your account is inactive. Please contact college support.", errorState.message)
        }
    }
}
