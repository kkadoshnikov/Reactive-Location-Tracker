package com.gmail.kadoshnikovkirill.locationtracker.mapper

import com.gmail.kadoshnikovkirill.locationtracker.domain.Track
import com.gmail.kadoshnikovkirill.locationtracker.domain.TrackKey
import com.gmail.kadoshnikovkirill.locationtracker.dto.TrackDTO
import org.springframework.stereotype.Component
import java.time.temporal.ChronoUnit

@Component
class TrackDTOMapper: EntityDTOMapper<Track, TrackDTO> {
    override fun TrackDTO.mapToEntity() = Track(
            key = TrackKey(
                    hr = timestamp.truncatedTo(ChronoUnit.HOURS),
                    userId = userId,
                    ts = timestamp),
            lat = lat,
            lon = lon,
            countryCode = countryCode,
            postalCode = postalCode,
            country = country,
            region = region,
            city = city,
            street = street,
            house = house)

    override fun Track.mapToDTO() = TrackDTO(
        userId = key.userId,
        lat = lat,
        lon = lon,
        timestamp = key.ts,
        countryCode = countryCode,
        postalCode = postalCode,
        country = country,
        region = region,
        city = city,
        street = street,
        house = house)
}