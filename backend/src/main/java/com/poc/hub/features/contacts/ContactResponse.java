package com.poc.hub.features.contacts;

import java.time.Instant;

public record ContactResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String company,
        String notes,
        Instant createdAt) {

    static ContactResponse from(Contact contact) {
        return new ContactResponse(contact.getId(), contact.getFullName(), contact.getEmail(), contact.getPhone(),
                contact.getCompany(), contact.getNotes(), contact.getCreatedAt());
    }
}
