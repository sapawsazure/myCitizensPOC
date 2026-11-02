package com.poc.hub.features.bookmarks;

import java.time.Instant;

public record BookmarkResponse(Long id, String title, String url, String category, Instant createdAt) {

    static BookmarkResponse from(Bookmark bookmark) {
        return new BookmarkResponse(bookmark.getId(), bookmark.getTitle(), bookmark.getUrl(),
                bookmark.getCategory(), bookmark.getCreatedAt());
    }
}
