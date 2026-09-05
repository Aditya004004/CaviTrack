package com.company.cavitrack.domain.usecase.auth

import javax.inject.Inject

sealed interface ValidationResult {
    data object Success : ValidationResult
    data class Error(val reason: ValidationReason) : ValidationResult
}

enum class ValidationReason {
    EMPTY_NAME,
    EMPTY_EMAIL,
    INVALID_EMAIL,
    EMPTY_PASSWORD,
    PASSWORD_TOO_SHORT,
    PASSWORD_REQUIRES_LETTER_AND_DIGIT
}

class ValidateEmailUseCase @Inject constructor() {
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult.Error(ValidationReason.EMPTY_EMAIL)
        }
        if (!email.matches(emailRegex)) {
            return ValidationResult.Error(ValidationReason.INVALID_EMAIL)
        }
        return ValidationResult.Success
    }
}

class ValidatePasswordUseCase @Inject constructor() {
    operator fun invoke(password: String, isRegistration: Boolean = false): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult.Error(ValidationReason.EMPTY_PASSWORD)
        }
        if (isRegistration) {
            if (password.length < 8) {
                return ValidationResult.Error(ValidationReason.PASSWORD_TOO_SHORT)
            }
            if (!password.any { it.isLetter() } || !password.any { it.isDigit() }) {
                return ValidationResult.Error(ValidationReason.PASSWORD_REQUIRES_LETTER_AND_DIGIT)
            }
        }
        return ValidationResult.Success
    }
}

class ValidateNameUseCase @Inject constructor() {
    operator fun invoke(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult.Error(ValidationReason.EMPTY_NAME)
        }
        return ValidationResult.Success
    }
}
