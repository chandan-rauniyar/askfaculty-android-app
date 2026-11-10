package com.chandan.askfaculty.data.local

import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SessionManagerTest {

    private lateinit var tokenStorage: TokenStorage
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        tokenStorage = mockk(relaxed = true)
        every { tokenStorage.getAccessToken() } returns null
        every { tokenStorage.getRefreshToken() } returns null

        sessionManager = SessionManager(tokenStorage)
    }

    @Test
    fun testSaveAndReadSession() = runTest {
        every { tokenStorage.getAccessToken() } returns "access-token-123"
        every { tokenStorage.getRefreshToken() } returns "refresh-token-456"

        sessionManager.saveSession("access-token-123", "refresh-token-456")

        val session = sessionManager.session.first()
        assertTrue(session.isLoggedIn)
        assertEquals("access-token-123", session.accessToken)
        assertEquals("refresh-token-456", session.refreshToken)
        verify { tokenStorage.saveTokens("access-token-123", "refresh-token-456") }
    }

    @Test
    fun testClearSession() = runTest {
        sessionManager.saveSession("access-123", "refresh-456")
        sessionManager.clearSession()

        val session = sessionManager.session.first()
        assertFalse(session.isLoggedIn)
        assertNull(session.accessToken)
        assertNull(session.refreshToken)
        verify { tokenStorage.clear() }
    }
}
