package com.chandan.askfaculty.ui.auth

import app.cash.turbine.test
import com.chandan.askfaculty.data.remote.AppError
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
class ActivationViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: ActivationViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        viewModel = ActivationViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testActivationSuccess() = runTest {
        coEvery { authRepository.activate("valid-token", "password123") } just Runs

        viewModel.uiState.test {
            assertEquals(ActivationUiState.Idle, awaitItem())

            viewModel.activate("valid-token", "password123")

            assertEquals(ActivationUiState.Loading, awaitItem())
            assertEquals(ActivationUiState.Success, awaitItem())
        }
    }

    @Test
    fun testActivationInvalidToken() = runTest {
        coEvery { authRepository.activate(any(), any()) } throws AppError.InvalidActivationToken("Invalid token", null)

        viewModel.uiState.test {
            assertEquals(ActivationUiState.Idle, awaitItem())

            viewModel.activate("bad-token", "password123")

            assertEquals(ActivationUiState.Loading, awaitItem())
            val errorState = awaitItem() as ActivationUiState.Error
            assertEquals("Invalid or expired activation token.", errorState.message)
        }
    }
}
