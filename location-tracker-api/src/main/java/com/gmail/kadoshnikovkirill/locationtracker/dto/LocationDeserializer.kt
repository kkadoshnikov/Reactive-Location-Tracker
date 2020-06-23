package com.gmail.kadoshnikovkirill.locationtracker.dto

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer

class LocationDeserializer @JvmOverloads constructor(vc: Class<*>? = LocationDto::class.java) : StdDeserializer<LocationDto>(vc) {
    private val timer = Metrics.timer("location.deserialization")

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): LocationDto {
        val sample = Timer.start()
        val jsonNode = jsonParser.codec.readTree<JsonNode>(jsonParser)
        val address = jsonNode["response"]["GeoObjectCollection"]["featureMember"]
                .elements().next()["GeoObject"]["metaDataProperty"]["GeocoderMetaData"]["Address"]
        val locationDto = LocationDto()
        if (address.has("postal_code")) {
            locationDto.postalCode = address["postal_code"].asInt()
        }
        if (address.has("country_code")) {
            locationDto.countryCode = address["country_code"].asText()
        }
        address["Components"].iterator()
                .forEachRemaining { component: JsonNode ->
                    val kind = component["kind"].asText()
                    when (kind) {
                        "country" -> locationDto.country = component["name"].asText()
                        "province" -> locationDto.region = component["name"].asText()
                        "locality" -> locationDto.city = component["name"].asText()
                        "street" -> locationDto.street = component["name"].asText()
                        "house" -> locationDto.house = component["name"].asText()
                        else -> {
                        }
                    }
                }
        sample.stop(timer)
        return locationDto
    }
}