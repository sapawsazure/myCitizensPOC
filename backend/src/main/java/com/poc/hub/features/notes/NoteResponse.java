package com.poc.hub.features.notes;

import java.time.Instant;

public record NoteResponse(Long id, String title, String body, Instant createdAt, Instant updatedAt) {

    static NoteResponse from(Note note) {
        return new NoteResponse(note.getId(), note.getTitle(), note.getBody(), note.getCreatedAt(),
                note.getUpdatedAt());
    }
}
