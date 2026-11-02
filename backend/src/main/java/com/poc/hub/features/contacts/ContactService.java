package com.poc.hub.features.contacts;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.poc.hub.core.error.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactService {

    private final ContactRepository repository;
    private final Clock clock;

    public ContactService(ContactRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> list(String query) {
        List<Contact> contacts = (query == null || query.isBlank())
                ? repository.findAllSorted()
                : repository.search(escapeLike(query.trim()));
        return contacts.stream().map(ContactResponse::from).toList();
    }

    @Transactional
    public ContactResponse create(ContactRequest request) {
        Contact contact = new Contact(request.fullName().trim(), blankToNull(request.email()),
                blankToNull(request.phone()), blankToNull(request.company()), blankToNull(request.notes()),
                Instant.now(clock));
        return ContactResponse.from(repository.save(contact));
    }

    @Transactional
    public ContactResponse update(Long id, ContactRequest request) {
        Contact contact = find(id);
        contact.edit(request.fullName().trim(), blankToNull(request.email()), blankToNull(request.phone()),
                blankToNull(request.company()), blankToNull(request.notes()));
        return ContactResponse.from(contact);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Contact find(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Contact", id));
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    /** Makes '%' and '_' typed by the user match literally in a LIKE pattern (escape character '!'). */
    private static String escapeLike(String text) {
        return text.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
