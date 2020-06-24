package com.gmail.kadoshnikovkirill.fake.tracks.generator

import java.time.LocalDateTime

data class UserCoordinatesDto(
    val userId: Long,
    val lat: Float,
    val lon: Float,
    val timestamp: LocalDateTime
) {
    constructor(userId: Long, timestamp: LocalDateTime, coordinates: Pair<Number, Number>)
            : this(userId, coordinates.first.toFloat(), coordinates.second.toFloat(), timestamp)
}