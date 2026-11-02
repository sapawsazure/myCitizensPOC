package com.poc.hub.features.notes;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.poc.hub.core.error.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteService {

    private final NoteRepository repository;
    private final Clock clock;

    public NoteService(NoteRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> list(String query) {
        List<Note> notes = (query == null || query.isBlank())
                ? repository.findAllByOrderByUpdatedAtDescIdDesc()
                : repository.search(escapeLike(query.trim()));
        return notes.stream().map(NoteResponse::from).toList();
    }

    @Transactional
    public NoteResponse create(NoteRequest request) {
        Note note = new Note(request.title().trim(), request.body(), Instant.now(clock));
        return NoteResponse.from(repository.save(note));
    }

    @Transactional
    public NoteResponse update(Long id, NoteRequest request) {
        Note note = find(id);
        note.edit(request.title().trim(), request.body(), Instant.now(clock));
        return NoteResponse.from(note);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Note find(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Note", id));
    }

    /** Makes '%' and '_' typed by the user match literally in a LIKE pattern (escape character '!'). */
    private static String escapeLike(String text) {
        return text.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
