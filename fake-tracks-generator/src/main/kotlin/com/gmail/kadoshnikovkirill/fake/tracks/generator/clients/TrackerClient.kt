package com.gmail.kadoshnikovkirill.fake.tracks.generator.clients

import com.gmail.kadoshnikovkirill.fake.tracks.generator.model.UserCoordinatesDto
import org.reactivestreams.Publisher

interface TrackerClient {
    fun sendTrack(coordinates: UserCoordinatesDto)
    fun streamTracks(publisher: Publisher<UserCoordinatesDto>)
}