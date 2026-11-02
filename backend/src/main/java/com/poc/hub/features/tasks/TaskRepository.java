package com.poc.hub.features.tasks;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOrderByIdDesc();

    List<Task> findByStatusOrderByIdDesc(TaskStatus status);
}
