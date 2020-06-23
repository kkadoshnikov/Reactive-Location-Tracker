package com.gmail.kadoshnikovkirill.locationtracker.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Getter
public class TrackDto {

    private final Long userId;
    private final Float lat;
    private final Float lon;
    private final LocalDateTime timestamp;
    private final String countryCode;
    private final Integer postalCode;
    private final String country;
    private final String region;
    private final String city;
    private final String street;
    private final String house;

}
