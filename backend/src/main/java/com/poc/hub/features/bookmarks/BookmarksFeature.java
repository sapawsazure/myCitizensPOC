package com.poc.hub.features.bookmarks;

import com.poc.hub.core.feature.FeatureDescriptor;

import org.springframework.stereotype.Component;

@Component
class BookmarksFeature implements FeatureDescriptor {

    private final BookmarkRepository repository;

    BookmarksFeature(BookmarkRepository repository) {
        this.repository = repository;
    }

    @Override
    public String key() {
        return "bookmarks";
    }

    @Override
    public String name() {
        return "Bookmarks";
    }

    @Override
    public String description() {
        return "Save links and group them by category.";
    }

    @Override
    public String route() {
        return "/bookmarks";
    }

    @Override
    public String apiPath() {
        return "/api/bookmarks";
    }

    @Override
    public int phase() {
        return 1;
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public long itemCount() {
        return repository.count();
    }
}
