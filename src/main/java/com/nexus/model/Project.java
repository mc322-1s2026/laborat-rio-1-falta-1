package com.nexus.model;

import com.nexus.exception.NexusValidationException;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um projeto no sistema Nexus, agrupando um conjunto de tarefas
 * e controlando o uso do orçamento total alocado (em horas).
 * 
 * Um projeto mantém uma lista de tarefas atribuídas e garante que a soma
 * dos esforços estimados de todas as suas tarefas não ultrapasse o
 * orçamento total establecido. Esta classe implementa a Regra de Ouro:
 * nenhuma tarefa pode ser adicionada se seu esforço, acumulado aos demais,
 * exceder o totalBudget.
 * 
 * @author Sistema Nexus
 */
public class Project {
    private String name;
    private List<Task> tasks;
    private double totalBudget; // em horas

    /**
     * Cria um novo projeto com nome e orçamento total em horas.
     * 
     * O nome do projeto identifica-o de forma unica no workspace, enquanto
     * o orçamento total define o limite máximo de esforço (em horas) que pode
     * ser alocado às tarefas associadas.
     * 
     * @param name O nome do projeto (não pode ser nulo, vazio ou composto apenas por espaços).
     * @param totalBudget O orçamento total em horas (deve ser estritamente maior que zero).
     * @throws IllegalArgumentException Se o nome for inválido ou o orçamento for <= 0.
     */
    public Project(String name, double totalBudget) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome do projeto não pode ser vazio.");
        }
        if (totalBudget <= 0) {
            throw new IllegalArgumentException("O orçamento total deve ser maior que zero.");
        }
        this.name = name;
        this.totalBudget = totalBudget;
        this.tasks = new ArrayList<>();
    }

    /**
     * Adiciona uma tarefa ao projeto, validando a Regra de Ouro do orçamento.
     * 
     * Antes de adicionar a tarefa, este método calcula o esforço total atual
     * somando os estimatedEffort de todas as tarefas existentes. Se a adição da
     * nova tarefa causar uma ultrapassagem do totalBudget, uma exceção é lançada
     * e a tarefa não é adicionada.
     * 
     * @param t A tarefa a ser adicionada ao projeto.
     * @throws NexusValidationException Se a tarefa for nula ou se seu esforço
     *         causar a ultrapassagem do orçamento total do projeto.
     */
    public void addTask(Task t) {
        if (t == null) {
            throw new NexusValidationException("Não é possível adicionar uma tarefa nula.");
        }
        
        double esforcoAtual = tasks.stream()
                .mapToDouble(Task::getEstimatedEffort)
                .sum();

        if (esforcoAtual + t.getEstimatedEffort() > totalBudget) {
            throw new NexusValidationException("O esforço total da tarefa excede o orçamento do projeto.");
        }

        this.tasks.add(t);
    }

    /**
     * Obtém o nome do projeto.
     * 
     * @return O nome identificador do projeto.
     */
    public String getName() {
        return name;
    }

    /**
     * Obtém o orçamento total alocado ao projeto em horas.
     * 
     * @return O orçamento total (valor positivo).
     */
    public double getTotalBudget() {
        return totalBudget;
    }

    /**
     * Retorna uma lista imutável contendo todas as tarefas do projeto.
     * 
     * A lista retornada é uma cópia imutável da lista interna, garantindo
     * que modificações externas não afetem o estado do projeto e evitando
     * vazamentos de referência.
     * 
     * @return Uma lista imutável das tarefas associadas ao projeto.
     */
    public List<Task> getTasks() {
        return List.copyOf(tasks);
    }
}