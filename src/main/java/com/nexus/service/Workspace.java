package com.nexus.model;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Workspace {
    private List<User> usuarios;
    private List<Task> todasAsTarefas;

    // 1. Top Performers: Os 3 usuários com mais tarefas DONE
    public List<User> getTopPerformers() {
        return usuarios.stream()
            .sorted(Comparator.comparingLong(u -> u.getTasksByStatus(TaskStatus.DONE).size()).reversed())
            .limit(3)
            .collect(Collectors.toList());
    }

    // 2. Overloaded Users: Usuários com mais de 10 tarefas IN_PROGRESS
    public List<User> getOverloadedUsers() {
        return usuarios.stream()
            .filter(u -> u.calculateWorkload() > 10)
            .collect(Collectors.toList());
    }

    // 3. Project Health: Percentual de conclusão (0.0 a 100.0)
    public double getProjectHealth(Project p) {
        List<Task> tarefasDoProjeto = p.getTarefas();
        if (tarefasDoProjeto.isEmpty()) return 0.0;

        long concluidas = tarefasDoProjeto.stream()
            .filter(t -> t.getStatus() == TaskStatus.DONE)
            .count();

        return (double) concluidas / tarefasDoProjeto.size() * 100.0;
    }

    // 4. Global Bottlenecks: O status com mais tarefas (exceto DONE)
    public TaskStatus getGlobalBottleneck() {
        return todasAsTarefas.stream()
            .filter(t -> t.getStatus() != TaskStatus.DONE)
            .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
}