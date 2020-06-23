package com.gmail.kadoshnikovkirill.locationtracker.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.SneakyThrows;

public class LocationDeserializer extends StdDeserializer<LocationDto> {

    private Timer timer = Metrics.timer("location.deserialization");

    public LocationDeserializer() {
        this(LocationDto.class);
    }

    public LocationDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    @SneakyThrows
    public LocationDto deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        Timer.Sample sample = Timer.start();

        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        JsonNode address = jsonNode.get("response")
                .get("GeoObjectCollection")
                .get("featureMember")
                .elements().next()
                .get("GeoObject")
                .get("metaDataProperty")
                .get("GeocoderMetaData")
                .get("Address");
        LocationDto locationDto = new LocationDto();
        if (address.has("postal_code")) {
            locationDto.setPostalCode(address.get("postal_code").asInt());
        }
        if (address.has("country_code")) {
            locationDto.setCountryCode(address.get("country_code").asText());
        }
        address.get("Components").iterator()
                .forEachRemaining(component -> {
                    String kind = component.get("kind").asText();
                    switch (kind) {
                        case "country":
                            locationDto.setCountry(component.get("name").asText());
                            break;
                        case "province":
                            locationDto.setRegion(component.get("name").asText());
                            break;
                        case "locality":
                            locationDto.setCity(component.get("name").asText());
                            break;
                        case "street":
                            locationDto.setStreet(component.get("name").asText());
                            break;
                        case "house":
                            locationDto.setHouse(component.get("name").asText());
                        default:
                            break;
                    }
                });

        sample.stop(timer);
        return locationDto;
    }
}
