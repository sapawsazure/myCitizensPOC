package com.poc.hub.features.notes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findAllByOrderByUpdatedAtDescIdDesc();

    /** Case-insensitive substring search in title or body; {@code pattern} must already escape with '!'. */
    @Query("""
            select n from Note n
            where lower(n.title) like lower(concat('%', :pattern, '%')) escape '!'
               or lower(coalesce(n.body, '')) like lower(concat('%', :pattern, '%')) escape '!'
            order by n.updatedAt desc, n.id desc
            """)
    List<Note> search(@Param("pattern") String pattern);
}
