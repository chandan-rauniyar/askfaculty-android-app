package com.chandan.askfaculty.data.remote

import com.chandan.askfaculty.data.remote.model.ApiErrorResponse
import com.chandan.askfaculty.data.remote.model.ApiMeta
import com.chandan.askfaculty.data.remote.model.ApiResponse
import kotlinx.serialization.SerializationException
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class ApiEnvelopeTest {

    @Test
    fun testSuccessfulEnvelopeUnwrap() {
        val response = ApiResponse(
            success = true,
            data = "Hello Data",
            meta = ApiMeta(requestId = "req-123")
        )
        val data = response.unwrap()
        assertEquals("Hello Data", data)
    }

    @Test(expected = AppError.ValidationError::class)
    fun testErrorEnvelopeMappingValidationError() {
        val response = ApiResponse<String>(
            success = false,
            error = ApiErrorResponse(
                code = "VALIDATION_ERROR",
                message = "Invalid input",
                details = emptyList()
            ),
            meta = ApiMeta(requestId = "req-456")
        )
        response.unwrap()
    }

    @Test
    fun testUnreachableServerMapping() {
        val ioEx = IOException("Connection refused")
        val appError = AppError.from(ioEx)
        assertTrue(appError is AppError.NetworkError)
        assertEquals("Cannot reach the server. Please check your connection.", appError.message)
    }

    @Test
    fun testParseErrorMapping() {
        val parseEx = SerializationException("Malformed JSON")
        val appError = AppError.from(parseEx)
        assertTrue(appError is AppError.ParseError)
        assertEquals("Failed to parse server response.", appError.message)
    }

    @Test
    fun testErrorMappingCodes() {
        val notReadyErr = AppError.from("NOT_READY", "Not ready", emptyList(), "req-789")
        assertTrue(notReadyErr is AppError.NotReady)
        assertEquals("Not ready", notReadyErr.message)
        assertEquals("req-789", notReadyErr.requestId)
    }
}
