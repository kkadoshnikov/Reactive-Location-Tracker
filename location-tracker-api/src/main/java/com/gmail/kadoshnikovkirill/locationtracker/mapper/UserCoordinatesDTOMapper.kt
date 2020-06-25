package com.gmail.kadoshnikovkirill.locationtracker.mapper

import com.gmail.kadoshnikovkirill.locationtracker.domain.Track
import com.gmail.kadoshnikovkirill.locationtracker.domain.TrackKey
import com.gmail.kadoshnikovkirill.locationtracker.dto.UserCoordinatesDto
import org.springframework.stereotype.Component
import java.time.temporal.ChronoUnit

@Component
class UserCoordinatesDTOMapper: EntityDTOMapper<Track, UserCoordinatesDto> {
    override fun UserCoordinatesDto.mapToEntity() = Track(
        key = TrackKey(
                hr = timestamp.truncatedTo(ChronoUnit.HOURS),
                userId = userId,
                ts = timestamp),
        lat = lat,
        lon = lon)

    override fun Track.mapToDTO() = UserCoordinatesDto(
            userId = key.userId,
            timestamp = key.ts,
            lat = lat,
            lon = lon
    )
}