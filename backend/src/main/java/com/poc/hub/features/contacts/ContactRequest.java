package com.poc.hub.features.contacts;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Everything except the name is optional; empty or blank values are stored as null. An email that is given
 * must be well-formed (an empty string counts as "not given").
 */
public record ContactRequest(
        @NotBlank @Size(max = 200) String fullName,
        @Email @Size(max = 200) String email,
        @Size(max = 50) String phone,
        @Size(max = 200) String company,
        @Size(max = 5000) String notes) {
}
