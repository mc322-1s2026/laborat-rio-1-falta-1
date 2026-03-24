package com.nexus.service;

import com.nexus.model.User;
import com.nexus.model.Project;
import com.nexus.model.TaskStatus;
import com.nexus.exception.NexusValidationException;
import com.nexus.model.Task;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Classe principal do sistema Nexus, atuando como contêiner para users, projects e tasks.
 * Fornece métodos para gerenciamento e análise de dados utilizando Stream API.
 * 
 * @author Sistema Nexus
 */
public class Workspace {
    private List<User> users;
    private List<Project> projects;
    private List<Task> allTasks;

    public Workspace() {
        this.users = new ArrayList<>();
        this.projects = new ArrayList<>();
        this.allTasks = new ArrayList<>();
    }


    /**
     * Adiciona um projeto ao workspace.
     * @param p O projeto a ser adicionado.
     * @throws NexusValidationException Se o projeto for nulo.
     */
    public void addProject(Project p) {
        if (p == null) {
            throw new NexusValidationException("Projeto inválido.");
        }
        this.projects.add(p);
    }

    /**
     * Busca um projeto pelo nome.
     * @param nome O nome do projeto (case-insensitive).
     * @return O projeto encontrado.
     * @throws NexusValidationException Se o projeto não for encontrado.
     */
    public Project getProjectByName(String nome) {
        return projects.stream()
            .filter(p -> p.getName().equalsIgnoreCase(nome))
            .findFirst()
            .orElseThrow(() -> new NexusValidationException("Projeto não encontrado: " + nome));
    }

    /**
     * Busca uma tarefa pelo ID.
     * @param id O ID da tarefa.
     * @return A tarefa encontrada.
     * @throws NexusValidationException Se a tarefa não for encontrada.
     */
    public Task getTaskById(int id) {
        return allTasks.stream()
            .filter(t -> t.getId() == id)
            .findFirst()
            .orElseThrow(() -> new NexusValidationException("Tarefa não encontrada: " + id));
    }

    /**
     * Adiciona uma tarefa ao workspace.
     * @param t A tarefa a ser adicionada.
     */
    public void addTask(Task t) {
        this.allTasks.add(t);
    }

    /**
     * Adiciona um usuário ao workspace.
     * @param user O usuário a ser adicionado.
     * @throws NexusValidationException Se o usuário for nulo.
     */
    public void addUser(User user) {
        if (user == null) {
            throw new NexusValidationException("Usuário inválido.");
        }
        this.users.add(user);
    }

    /**
     * Busca um usuário pelo nome de usuário.
     * @param username O nome de usuário a ser buscado.
     * @return O usuário encontrado.
     * @throws NexusValidationException Se o usuário não for encontrado.
     */
    public User getUserByUsername(String username) {
        return users.stream()
            .filter(u -> u.getUsername().equalsIgnoreCase(username))
            .findFirst()
            .orElseThrow(() -> new NexusValidationException("Usuário não encontrado: " + username));
    }

    // 1. Top Performers: Os 3 usuários com mais tarefas DONE
    /**
     * Retorna os 3 usuários com o maior número de tarefas concluídas (status DONE).
     * Utiliza Stream API para ordenação e limitação.
     * @return Uma lista imutável dos 3 melhores performers.
     */
    public List<User> getTopPerformers() {
        return Collections.unmodifiableList(users.stream()
            .sorted(Comparator.comparingLong(u -> u.getTasksByStatus(TaskStatus.DONE).size()).reversed())
            .limit(3)
            .collect(Collectors.toList()));
    }

    // 2. Overloaded Users: Usuários com mais de 10 tarefas IN_PROGRESS
    /**
     * Retorna todos os usuários cuja carga de trabalho atual (tarefas IN_PROGRESS) ultrapassa 10.
     * Utiliza Stream API para filtragem.
     * @return Uma lista imutável dos usuários sobrecarregados.
     */
    public List<User> getOverloadedUsers() {
        return Collections.unmodifiableList(users.stream()
            .filter(u -> u.calculateWorkload() > 10)
            .collect(Collectors.toList()));
    }

    // 3. Project Health: Percentual de conclusão (0.0 a 100.0)
    /**
     * Calcula o percentual de conclusão de um projeto (tarefas DONE / total de tarefas).
     * Utiliza Stream API para contagem.
     * @param p O projeto a ser analisado.
     * @return O percentual de conclusão (0.0 a 100.0).
     */
    public double getProjectHealth(Project p) {
        List<Task> projectTasks = p.getTasks();
        if (projectTasks.isEmpty()) return 0.0;

        long concluidas = projectTasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.DONE)
            .count();

        return (double) concluidas / projectTasks.size() * 100.0;
    }

    // 4. Global Bottlenecks: O status com mais tarefas (exceto DONE)
    /**
     * Identifica o status com o maior número de tarefas no workspace (excluindo DONE).
     * Utiliza Stream API para agrupamento e comparação.
     * @return O status com mais tarefas, ou null se nenhum encontrado.
     */
    public TaskStatus getGlobalBottleneck() {
        return allTasks.stream()
            .filter(t -> t.getStatus() != TaskStatus.DONE)
            .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    /**
     * Retorna um relatório de saúde de todos os projetos (nome do projeto -> percentual de conclusão).
     * Utiliza Stream API para mapeamento.
     * @return Um mapa imutável com a saúde de cada projeto.
     */
    public Map<String, Double> getProjectHealthReport() {
        return projects.stream()
            .collect(Collectors.toMap(
                Project::getName,
                this::getProjectHealth,
                (existing, replacement) -> existing // em caso de conflito, mantém o existente
            ));
    }