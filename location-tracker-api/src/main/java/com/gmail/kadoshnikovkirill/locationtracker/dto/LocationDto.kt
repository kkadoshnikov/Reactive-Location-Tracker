package com.gmail.kadoshnikovkirill.locationtracker.dto

import java.io.Serializable

data class LocationDto(
    var countryCode: String? = null,
    var postalCode: Int? = null,
    var country: String? = null,
    var region: String? = null,
    var city: String? = null,
    var street: String? = null,
    var house: String? = null
): Serializable