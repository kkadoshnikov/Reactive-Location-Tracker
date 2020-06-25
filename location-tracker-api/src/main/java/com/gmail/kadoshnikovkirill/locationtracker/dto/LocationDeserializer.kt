package com.gmail.kadoshnikovkirill.locationtracker.dto

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.micrometer.core.instrument.Metrics
import java.util.function.Supplier

class LocationDeserializer(vc: Class<*>? = LocationDto::class.java) : StdDeserializer<LocationDto>(vc) {
    private val timer = Metrics.timer("location.deserialization")

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): LocationDto {
        return timer.record(Supplier {
            val jsonNode = jsonParser.codec.readTree<JsonNode>(jsonParser)
            val address = getAddressNode(jsonNode)
            parseLocation(address)
        })
    }

    private fun getAddressNode(jsonNode: JsonNode) =
            jsonNode.get("response")
                    ?.get("GeoObjectCollection")
                    ?.get("featureMember")
                    ?.elements()
                    ?.next()
                    ?.get("GeoObject")
                    ?.get("metaDataProperty")
                    ?.get("GeocoderMetaData")
                    ?.get("Address")
                    ?: error("Invalid location format. Input: $jsonNode")

    private fun parseLocation(addressNode: JsonNode) = LocationDto().apply {
        postalCode = addressNode.getInt("postal_code")
        countryCode = addressNode.getText("country_code")
        addressNode["Components"].forEach {
            when (it["kind"].asText()) {
                "country" -> country = it.getText("name")
                "province" -> region = it.getText("name")
                "locality" -> city = it.getText("name")
                "street" -> street = it.getText("name")
                "house" -> house = it.getText("name")
                else -> {
                }
            }
        }
    }

    private fun JsonNode.getInt(fieldName: String) = this.get(fieldName)?.asInt()

    private fun JsonNode.getText(fieldName: String) = this.get(fieldName)?.asText()
}