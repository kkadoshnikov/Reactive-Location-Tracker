package com.gmail.kadoshnikovkirill.fake.tracks.generator.core

import com.gmail.kadoshnikovkirill.fake.tracks.generator.UserCoordinatesDto

interface UserCoordinatesGenerator {
    fun getCoordinates(): UserCoordinatesDto
}