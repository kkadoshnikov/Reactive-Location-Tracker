package com.gmail.kadoshnikovkirill.locationtracker.dto

import java.time.LocalDateTime

data class UserCoordinatesDto(
        val userId: Long,
        val lat: Float,
        val lon: Float,
        val timestamp: LocalDateTime)