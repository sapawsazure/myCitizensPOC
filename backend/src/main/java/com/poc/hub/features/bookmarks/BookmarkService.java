package com.poc.hub.features.bookmarks;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.poc.hub.core.error.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookmarkService {

    private static final String DEFAULT_CATEGORY = "General";

    private final BookmarkRepository repository;
    private final Clock clock;

    public BookmarkService(BookmarkRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<BookmarkResponse> list(String category) {
        List<Bookmark> bookmarks = (category == null || category.isBlank())
                ? repository.findAllByOrderByIdDesc()
                : repository.findByCategoryOrderByIdDesc(category.trim());
        return bookmarks.stream().map(BookmarkResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return repository.findCategories();
    }

    @Transactional
    public BookmarkResponse create(BookmarkRequest request) {
        Bookmark bookmark = new Bookmark(request.title().trim(), request.url().trim(),
                categoryOrDefault(request.category()), Instant.now(clock));
        return BookmarkResponse.from(repository.save(bookmark));
    }

    @Transactional
    public BookmarkResponse update(Long id, BookmarkRequest request) {
        Bookmark bookmark = find(id);
        bookmark.edit(request.title().trim(), request.url().trim(), categoryOrDefault(request.category()));
        return BookmarkResponse.from(bookmark);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Bookmark find(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Bookmark", id));
    }

    private static String categoryOrDefault(String category) {
        return (category == null || category.isBlank()) ? DEFAULT_CATEGORY : category.trim();
    }
}
