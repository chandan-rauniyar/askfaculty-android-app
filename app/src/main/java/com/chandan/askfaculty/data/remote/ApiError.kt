package com.chandan.askfaculty.data.remote

import com.chandan.askfaculty.data.remote.model.ApiErrorDetail
import com.chandan.askfaculty.data.remote.model.ApiErrorResponse
import com.chandan.askfaculty.data.remote.model.ApiResponse
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class AppError(override val message: String, val requestId: String? = null) : Exception(message) {
    data class NetworkError(override val message: String) : AppError(message)
    data class ParseError(override val message: String) : AppError(message)
    data class ValidationError(override val message: String, val details: List<ApiErrorDetail>, val reqId: String?) : AppError(message, reqId)
    data class InvalidActivationToken(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class InvalidResetToken(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class UnsupportedFileType(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class ExternalIdentityUnsupported(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class Unauthenticated(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class InvalidAccessToken(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class InvalidCredentials(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class InvalidRefreshToken(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class AccountInactive(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class Forbidden(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class GroupMuted(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class NotFound(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class Conflict(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class IdempotencyConflict(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class AdminExists(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class AttachmentLimit(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class UploadIncomplete(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class FileTooLarge(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class RateLimited(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class InternalError(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class NotConfigured(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class NotReady(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class StorageUnavailable(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class EmailNotConfigured(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class EmailDeliveryFailed(override val message: String, val reqId: String?) : AppError(message, reqId)
    data class Unknown(override val message: String, val reqId: String?) : AppError(message, reqId)

    companion object {
        fun from(throwable: Throwable): AppError {
            return when (throwable) {
                is AppError -> throwable
                is UnknownHostException, is SocketTimeoutException, is IOException -> 
                    NetworkError("Cannot reach the server. Please check your connection.")
                is SerializationException ->
                    ParseError("Failed to parse server response.")
                else -> Unknown(throwable.localizedMessage ?: "An unexpected error occurred.", null)
            }
        }

        fun from(code: String, message: String, details: List<ApiErrorDetail>, requestId: String?): AppError {
            return when (code) {
                "VALIDATION_ERROR" -> ValidationError(message, details, requestId)
                "INVALID_ACTIVATION_TOKEN" -> InvalidActivationToken(message, requestId)
                "INVALID_RESET_TOKEN" -> InvalidResetToken(message, requestId)
                "UNSUPPORTED_FILE_TYPE" -> UnsupportedFileType(message, requestId)
                "EXTERNAL_IDENTITY_UNSUPPORTED" -> ExternalIdentityUnsupported(message, requestId)
                "UNAUTHENTICATED" -> Unauthenticated(message, requestId)
                "INVALID_ACCESS_TOKEN" -> InvalidAccessToken(message, requestId)
                "INVALID_CREDENTIALS" -> InvalidCredentials(message, requestId)
                "INVALID_REFRESH_TOKEN" -> InvalidRefreshToken(message, requestId)
                "ACCOUNT_INACTIVE" -> AccountInactive(message, requestId)
                "FORBIDDEN" -> Forbidden(message, requestId)
                "GROUP_MUTED" -> GroupMuted(message, requestId)
                "NOT_FOUND" -> NotFound(message, requestId)
                "CONFLICT" -> Conflict(message, requestId)
                "IDEMPOTENCY_CONFLICT" -> IdempotencyConflict(message, requestId)
                "ADMIN_EXISTS" -> AdminExists(message, requestId)
                "ATTACHMENT_LIMIT" -> AttachmentLimit(message, requestId)
                "UPLOAD_INCOMPLETE" -> UploadIncomplete(message, requestId)
                "FILE_TOO_LARGE" -> FileTooLarge(message, requestId)
                "RATE_LIMITED" -> RateLimited(message, requestId)
                "INTERNAL_ERROR" -> InternalError(message, requestId)
                "NOT_CONFIGURED" -> NotConfigured(message, requestId)
                "NOT_READY" -> NotReady(message, requestId)
                "STORAGE_UNAVAILABLE" -> StorageUnavailable(message, requestId)
                "EMAIL_NOT_CONFIGURED" -> EmailNotConfigured(message, requestId)
                "EMAIL_DELIVERY_FAILED" -> EmailDeliveryFailed(message, requestId)
                else -> Unknown(message, requestId)
            }
        }
    }
}

fun <T> ApiResponse<T>.unwrap(): T {
    if (success && data != null) {
        return data
    } else {
        val err = error ?: ApiErrorResponse(
            code = "INTERNAL_ERROR",
            message = "Unexpected server response"
        )
        throw AppError.from(err.code, err.message, err.details, meta.requestId)
    }
}
