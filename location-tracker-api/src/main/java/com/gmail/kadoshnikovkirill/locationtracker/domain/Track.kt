package com.gmail.kadoshnikovkirill.locationtracker.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 *
 */
@Table("tracks")
@Builder(toBuilder = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class Track {

    @PrimaryKey
    private final TrackKey key;
    @Column
    private final Float lat;
    @Column
    private final Float lon;
    @Column("country_code")
    private final String countryCode;
    @Column("postal_code")
    private final Integer postalCode;
    @Column
    private final String country;
    @Column
    private final String region;
    @Column
    private final String city;
    @Column
    private final String street;
    @Column
    private final String house;
}
