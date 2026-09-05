package com.company.cavitrack.domain.usecase.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateAuthFieldsUseCaseTest {

    private lateinit var validateEmailUseCase: ValidateEmailUseCase
    private lateinit var validatePasswordUseCase: ValidatePasswordUseCase
    private lateinit var validateNameUseCase: ValidateNameUseCase

    @Before
    fun setUp() {
        validateEmailUseCase = ValidateEmailUseCase()
        validatePasswordUseCase = ValidatePasswordUseCase()
        validateNameUseCase = ValidateNameUseCase()
    }

    @Test
    fun `validateEmail returns EMPTY_EMAIL when blank`() {
        val result = validateEmailUseCase("   ")
        assertTrue(result is ValidationResult.Error)
        assertEquals(ValidationReason.EMPTY_EMAIL, (result as ValidationResult.Error).reason)
    }

    @Test
    fun `validateEmail returns INVALID_EMAIL for invalid formats`() {
        val result = validateEmailUseCase("notanemail")
        assertTrue(result is ValidationResult.Error)
        assertEquals(ValidationReason.INVALID_EMAIL, (result as ValidationResult.Error).reason)
    }

    @Test
    fun `validateEmail returns Success for valid email`() {
        val result = validateEmailUseCase("user@example.com")
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `validatePassword returns EMPTY_PASSWORD when blank`() {
        val result = validatePasswordUseCase("")
        assertTrue(result is ValidationResult.Error)
        assertEquals(ValidationReason.EMPTY_PASSWORD, (result as ValidationResult.Error).reason)
    }

    @Test
    fun `validatePassword for registration validates length and alphanumeric criteria`() {
        val shortResult = validatePasswordUseCase("Pass1", isRegistration = true)
        assertTrue(shortResult is ValidationResult.Error)
        assertEquals(ValidationReason.PASSWORD_TOO_SHORT, (shortResult as ValidationResult.Error).reason)

        val lettersOnly = validatePasswordUseCase("PasswordOnly", isRegistration = true)
        assertTrue(lettersOnly is ValidationResult.Error)
        assertEquals(ValidationReason.PASSWORD_REQUIRES_LETTER_AND_DIGIT, (lettersOnly as ValidationResult.Error).reason)

        val digitsOnly = validatePasswordUseCase("1234567890", isRegistration = true)
        assertTrue(digitsOnly is ValidationResult.Error)
        assertEquals(ValidationReason.PASSWORD_REQUIRES_LETTER_AND_DIGIT, (digitsOnly as ValidationResult.Error).reason)

        val valid = validatePasswordUseCase("SecurePass123", isRegistration = true)
        assertTrue(valid is ValidationResult.Success)
    }

    @Test
    fun `validatePassword for login succeeds with any non-blank password`() {
        val result = validatePasswordUseCase("short", isRegistration = false)
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `validateName returns EMPTY_NAME when blank`() {
        val result = validateNameUseCase("  ")
        assertTrue(result is ValidationResult.Error)
        assertEquals(ValidationReason.EMPTY_NAME, (result as ValidationResult.Error).reason)
    }

    @Test
    fun `validateName returns Success when non-blank`() {
        val result = validateNameUseCase("Aditya")
        assertTrue(result is ValidationResult.Success)
    }
}
