package com.poc.hub.features.bookmarks;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findAllByOrderByIdDesc();

    List<Bookmark> findByCategoryOrderByIdDesc(String category);

    @Query("select distinct b.category from Bookmark b order by b.category")
    List<String> findCategories();
}
