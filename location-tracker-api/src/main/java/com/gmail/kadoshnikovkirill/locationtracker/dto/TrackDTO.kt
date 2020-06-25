package com.gmail.kadoshnikovkirill.locationtracker.dto

import java.time.LocalDateTime

data class TrackDTO(
    val userId: Long,
    val lat: Float,
    val lon: Float,
    val timestamp: LocalDateTime,
    val countryCode: String?,
    val postalCode: Int?,
    val country: String?,
    val region: String?,
    val city: String?,
    val street: String?,
    val house: String?)