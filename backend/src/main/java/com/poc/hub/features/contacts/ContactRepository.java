package com.poc.hub.features.contacts;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Query("select c from Contact c order by lower(c.fullName), c.id")
    List<Contact> findAllSorted();

    /** Case-insensitive substring search in name, email or company; {@code pattern} must already escape with '!'. */
    @Query("""
            select c from Contact c
            where lower(c.fullName) like lower(concat('%', :pattern, '%')) escape '!'
               or lower(coalesce(c.email, '')) like lower(concat('%', :pattern, '%')) escape '!'
               or lower(coalesce(c.company, '')) like lower(concat('%', :pattern, '%')) escape '!'
            order by lower(c.fullName), c.id
            """)
    List<Contact> search(@Param("pattern") String pattern);
}
