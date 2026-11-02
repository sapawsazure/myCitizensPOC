package com.poc.hub.features.notes;

import com.poc.hub.core.feature.FeatureDescriptor;

import org.springframework.stereotype.Component;

@Component
class NotesFeature implements FeatureDescriptor {

    private final NoteRepository repository;

    NotesFeature(NoteRepository repository) {
        this.repository = repository;
    }

    @Override
    public String key() {
        return "notes";
    }

    @Override
    public String name() {
        return "Notes";
    }

    @Override
    public String description() {
        return "Write notes and search them instantly.";
    }

    @Override
    public String route() {
        return "/notes";
    }

    @Override
    public String apiPath() {
        return "/api/notes";
    }

    @Override
    public int phase() {
        return 1;
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public long itemCount() {
        return repository.count();
    }
}
