package com.nexus.model;

public class User {
    private final String username;
    private final String email;

    public User(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }
        this.username = username;
        this.email = email;
    }

    public String consultEmail() {
        return email;
    }

    public String consultUsername() {
        return username;
    }

    public long calculateWorkload() {
        return 0; 
    }

    /**
     * Retorna a lista de tarefas pertencentes ao usuário com um status especificado.
     * Para garantir o encapsulamento e evitar vazamento de referência, retorna uma exibição 
     * imutável (unmodifiableList) da lista original.
     * @return Uma lista imutável contendo as tarefas do usuário com um status especificado.
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        List<Task> filtered = myTasks.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());

        return Collections.unmodifiableList(filtered);
    }

    /**
     * Retorna a lista de tarefas pertencentes ao usuário com um status especificado.
     * Para garantir o encapsulamento e evitar vazamento de referência, retorna uma exibição 
     * imutável (unmodifiableList) da lista original.
     * @return Uma lista imutável contendo as tarefas do usuário com um status especificado.
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        List<Task> filtered = myTasks.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());

        return Collections.unmodifiableList(filtered);
    }
}