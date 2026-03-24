package com.nexus.model;

import com.nexus.exception.NexusValidationException;
import java.time.LocalDate;

/**
 * Representa uma tarefa no sistema Nexus, operando como uma máquina de estados finitos.
 * Gerencia regras de transição de status, métricas globais e garante a integridade 
 * dos dados como Identidade e Prazo (imutáveis).
 */
public class Task {
    public static int totalTasksCreated = 0;
    public static int totalValidationErrors = 0;
    public static int activeWorkload = 0;

    private static int nextId = 1;

    private final int id;
    private final LocalDate deadline; 
    
    private String title;
    private TaskStatus status;
    private User owner;
    private double estimatedEffort;

    /**
     * Cria uma nova tarefa com o título e prazo fornecidos, inicializando seu status como TO_DO.
     * Incrementa a métrica global de tarefas criadas.
     * * @param title O título da tarefa (não pode ser nulo ou vazio).
     * @param deadline O prazo da tarefa (não pode ser nulo).
     * @throws IllegalArgumentException Se o título for vazio ou o prazo for nulo.
     */
    public Task(String title, LocalDate deadline, double estimatedEffort) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Título da tarefa não pode ser vazio.");
        }
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline é obrigatório.");
        }
        this.id = nextId++;
        this.deadline = deadline;
        this.title = title;
        this.status = TaskStatus.TO_DO;
        this.estimatedEffort = estimatedEffort.
        
        totalTasksCreated++; 
    }

    /**
     * Move a tarefa para o status IN_PROGRESS, atribuindo um proprietário a ela.
     * Atualiza automaticamente a carga de trabalho ativa global e a lista do usuário.
     * @param user O usuário que assumirá a tarefa.
     * @throws NexusValidationException Se o usuário for nulo ou se a tarefa estiver com status BLOCKED.
     */
    public void moveToInProgress(User user) {
        if (user == null) {
            totalValidationErrors++;
            throw new NexusValidationException("NexusValidationException: Owner obrigatório para iniciar a tarefa.");
        }

        if (this.status == TaskStatus.BLOCKED) {
            totalValidationErrors++;
            throw new NexusValidationException("NexusValidationException: Uma tarefa BLOCKED não pode ir para IN_PROGRESS.");
        }

        if (this.status != TaskStatus.IN_PROGRESS) {
            this.status = TaskStatus.IN_PROGRESS;
            activeWorkload++; 
        }

        this.owner = user;
        user.assignTask(this); 
    }

    /**
     * Finaliza a tarefa, movendo seu status para DONE.
     * Reduz a carga de trabalho ativa global se a tarefa estava em progresso.
     * @throws NexusValidationException Se a tarefa estiver com status BLOCKED.
     */
    public void markAsDone() {
        if (this.status == TaskStatus.BLOCKED) {
            totalValidationErrors++;
            throw new NexusValidationException("Impossível finalizar tarefa bloqueada.");
        }

        if (this.status == TaskStatus.IN_PROGRESS) {
            activeWorkload--;
        }

        this.status = TaskStatus.DONE;
    }

    /**
     * Altera o estado de bloqueio da tarefa.
     * Pode ser chamada a partir de qualquer estado, exceto se a tarefa já estiver finalizada (DONE).
     * @param blocked Se true, muda o status para BLOCKED; se false, retorna para TO_DO.
     * @throws NexusValidationException Se a tarefa já estiver com status DONE.
     */
    public void setBlocked(boolean blocked) {
        if (this.status == TaskStatus.DONE) {
            totalValidationErrors++;
            throw new NexusValidationException("Tarefa finalizada não pode ser bloqueada.");
        }

        if (blocked) {
            if (this.status == TaskStatus.IN_PROGRESS) {
                activeWorkload--;
            }
            this.status = TaskStatus.BLOCKED;
        } else {
            this.status = TaskStatus.TO_DO; 
        }
    }

    /**
     * Obtém o identificador único e imutável da tarefa.
     * @return O ID da tarefa.
     */
    public int getId() { return id; }

    /**
     * Obtém o prazo de entrega estipulado na criação da tarefa.
     * @return O prazo (deadline) da tarefa.
     */
    public LocalDate getDeadline() { return deadline; }

    /**
     * Obtém o status atual da tarefa na máquina de estados.
     * @return O status atual (TO_DO, IN_PROGRESS, BLOCKED, DONE).
     */
    public TaskStatus getStatus() { return status; }

    /**
     * Obtém o título descritivo da tarefa.
     * @return O título da tarefa.
     */
    public String getTitle() { return title; }

    /**
     * Obtém o usuário responsável pela execução da tarefa.
     * @return O usuário dono da tarefa.
     */
    public User getOwner() { return owner; }

    /**
     * Obtém o trabalho estimado da tarefa.
     * @return O trabalho da tarefa.
     */
    public double getEstimatedEffort() { return estimatedEffort; }

    /**
     * Define o proprietário da tarefa.
     * @param owner O usuário proprietário.
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }
}