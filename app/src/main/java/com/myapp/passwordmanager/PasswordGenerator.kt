package com.myapp.passwordmanager

import kotlin.random.Random

object PasswordGenerator {

    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"
    private const val SPECIAL_CHARACTERS = "!@#\$%^&*()-_=+[]{}|;:,.<>?"

    private const val PASSWORD_LENGTH = 12 // default duljina lozinke

    fun generatePassword(length: Int = PASSWORD_LENGTH): String {
        val charPool = LOWERCASE + UPPERCASE + DIGITS + SPECIAL_CHARACTERS
        return (1..length)
            .map { Random.nextInt(0, charPool.length) }
            .map(charPool::get)
            .joinToString("")
    }
}