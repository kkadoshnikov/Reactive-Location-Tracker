package com.gmail.kadoshnikovkirill.locationtracker.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonDeserialize(using = LocationDeserializer.class)
public class LocationDto implements Serializable {
    private String countryCode;
    private Integer postalCode;
    private String country;
    private String region;
    private String city;
    private String street;
    private String house;
}
