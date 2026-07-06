package ropold.backend.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationDTOTest {

    @Test
    void testLocationDTOCreation() {
        UUID id = UUID.randomUUID();
        String name = "Location One";
        String address = "Musterstrasse 1, 12345 Musterstadt";
        String phone = "+49 170 1234567";
        String email = "location.one@example.com";
        String notes = "Notes for location one";
        String imageUrl = "https://example.com/location1.jpg";

        LocationDTO locationDTO = new LocationDTO(id, name, address, phone, email, notes, imageUrl);

        assertEquals(id, locationDTO.id());
        assertEquals(name, locationDTO.name());
        assertEquals(address, locationDTO.address());
        assertEquals(phone, locationDTO.phone());
        assertEquals(email, locationDTO.email());
        assertEquals(notes, locationDTO.notes());
        assertEquals(imageUrl, locationDTO.imageUrl());
    }
}