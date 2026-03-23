package com.nexus.model;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um usuário dentro do sistema Nexus.
 * É responsável por manter as credenciais e gerenciar a lista de tarefas 
 * atribuídas a ele, permitindo o cálculo dinâmico da sua carga de trabalho.
 */
public class User {
    private final String username;
    private final String email;
    private List<Task> myTasks = new ArrayList<>();

    /**
     * Cria um novo usuário validando as regras de integridade e formatação.
     * Regras de negócio: Username não pode ser vazio e o e-mail deve ter um formato válido.
     * @param username O nome de usuário escolhido (não pode ser nulo ou vazio).
     * @param email O endereço de e-mail do usuário (deve seguir o padrão usuario@dominio.com).
     * @throws IllegalArgumentException Se o username for inválido ou o e-mail estiver fora do formato esperado.
     */
    public User(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("E-mail com formato inválido.");
        }
        this.username = username;
        this.email = email;
    }

    /**
     * Obtém o endereço de e-mail do usuário.
     * @return O e-mail cadastrado.
     */
    public String consultEmail() {
        return email;
    }

    /**
     * Obtém o nome de usuário (username).
     * @return O username cadastrado.
     */
    public String consultUsername() {
        return username;
    }

    /**
     * Adiciona uma tarefa à lista de responsabilidades deste usuário.
     * Este método é geralmente chamado pela própria máquina de estados da Task.
     * @param task A tarefa a ser atribuída ao usuário.
     */
    public void assignTask(Task task) {
        this.myTasks.add(task);
    }

    /**
     * Calcula a carga de trabalho ativa do usuário de forma dinâmica.
     * Utiliza a Stream API para iterar sobre a lista e contar apenas as tarefas que estão em andamento.
     * @return O número total de tarefas com o status IN_PROGRESS associadas a este usuário.
     */
    public long calculateWorkload() {
        return myTasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS)
            .count();
    }

    /**
     * Retorna a lista de tarefas pertencentes ao usuário.
     * Para garantir o encapsulamento e evitar vazamento de referência, retorna uma exibição 
     * imutável (unmodifiableList) da lista original.
     * @return Uma lista imutável contendo as tarefas do usuário.
     */
    public List<Task> getTasks() {
    return Collections.unmodifiableList(myTasks);
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