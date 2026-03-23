package com.nexus.model;

import com.nexus.exception.NexusValidationException;
import java.util.ArrayList;
import java.util.List;

public class Project {
    private String nome;
    private List<Task> tarefas;
    private double totalBudget; // em horas

    public Project(String nome, double totalBudget) {
        if (nome == null || nome.isBlank()) {
            throw new NexusValidationException("O nome do projeto não pode ser vazio.");
        }
        if (totalBudget <= 0) {
            throw new NexusValidationException("O orçamento total deve ser maior que zero.");
        }
        this.nome = nome;
        this.totalBudget = totalBudget;
        this.tarefas = new ArrayList<>();
    }

    /**
     * Adiciona uma tarefa ao projeto validando o orçamento de horas.
     * Regra de Ouro: A soma do esforço não pode exceder o totalBudget.
     */
    public void addTask(Task t) {
        if (t == null) {
            throw new NexusValidationException("Não é possível adicionar uma tarefa nula.");
        }
        
        double esforcoAtual = tarefas.stream()
                .mapToDouble(Task::getEstimatedEffort)
                .sum();

        if (esforcoAtual + t.getEstimatedEffort() > totalBudget) {
            // Lançamos a exceção se estourar o orçamento
            throw new NexusValidationException("Erro: O esforço total da tarefa excede o orçamento do projeto!");
        }

        this.tarefas.add(t);
    }

    // Getters
    public String getNome() {
        return nome;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public List<Task> getTarefas() {
        // Retornamos uma cópia para proteger a lista original
        return List.copyOf(tarefas);
    }
}