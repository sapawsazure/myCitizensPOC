package com.poc.hub.features.tasks;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.poc.hub.core.error.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private static final int DEFAULT_PRIORITY = 2;

    private final TaskRepository repository;
    private final Clock clock;

    public TaskService(TaskRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> list(TaskStatus status) {
        List<Task> tasks = status == null
                ? repository.findAllByOrderByIdDesc()
                : repository.findByStatusOrderByIdDesc(status);
        return tasks.stream().map(TaskResponse::from).toList();
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task(
                request.title().trim(),
                request.description(),
                request.status() == null ? TaskStatus.TODO : request.status(),
                request.priority() == null ? DEFAULT_PRIORITY : request.priority(),
                request.dueDate(),
                Instant.now(clock));
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = find(id);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        task.setDueDate(request.dueDate());
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatus status) {
        Task task = find(id);
        task.setStatus(status);
        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Task find(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Task", id));
    }
}
