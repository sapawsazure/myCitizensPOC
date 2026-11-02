package com.poc.hub.features.bookmarks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** {@code category} is optional and falls back to "General". */
public record BookmarkRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 2000) @Pattern(regexp = "^https?://\\S+$", message = "must start with http:// or https://") String url,
        @Size(max = 100) String category) {
}
