package com.nexus.service;

import com.nexus.model.*;
import com.nexus.exception.NexusValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class LogProcessor {

    public void processLog(String fileName, Workspace workspace, List<User> users) {
        try {
            InputStream resource = getClass().getClassLoader().getResourceAsStream(fileName);
            
            if (resource == null) {
                throw new IOException("Arquivo não encontrado no classpath: " + fileName);
            }

            try (Scanner s = new Scanner(resource).useDelimiter("\\A")) {
                String content = s.hasNext() ? s.next() : "";
                String[] lines = content.split("\\R");
                
                for (String line : lines) {
                    if (line.isBlank() || line.startsWith("#")) continue;

                    String[] p = line.split(";");
                    String action = p[0];

                    try {
                        switch (action) {
                            case "CREATE_USER" -> {
                                // CREATE_USER;username;email
                                User newUser = new User(p[1], p[2]);
                                users.add(newUser);
                                workspace.addUser(newUser);
                                System.out.println("[LOG] Usuário criado: " + p[1]);
                            }
                            case "CREATE_PROJECT" -> {
                                // CREATE_PROJECT;projectName;budgetHours
                                workspace.addProject(new Project(p[1], Double.parseDouble(p[2])));
                                System.out.println("[LOG] Projeto criado: " + p[1]);
                            }
                            case "CREATE_TASK" -> {
                                // CREATE_TASK;taskName;deadline;effort;projectName
                                Task t = new Task(p[1], LocalDate.parse(p[2]), Double.parseDouble(p[3]));
                                Project proj = workspace.getProjectByName(p[4]);
                                proj.addTask(t); // Valida orçamento
                                workspace.addTask(t);
                                System.out.println("[LOG] Tarefa '" + p[1] + "' vinculada ao projeto: " + p[4]);
                            }
                            case "ASSIGN_USER" -> {
                                // ASSIGN_USER;taskId;username
                                Task task = workspace.getTaskById(Integer.parseInt(p[1]));
                                User user = workspace.getUserByUsername(p[2]);
                                task.setOwner(user);
                                System.out.println("[LOG] Tarefa " + p[1] + " atribuída a " + p[2]);
                            }
                            case "CHANGE_STATUS" -> {
                                // CHANGE_STATUS;taskId;newStatus
                                Task task = workspace.getTaskById(Integer.parseInt(p[1]));
                                task.transitionTo(TaskStatus.valueOf(p[2]));
                                System.out.println("[LOG] Status da tarefa " + p[1] + " alterado para " + p[2]);
                            }
                            case "REPORT_STATUS" -> {
                                printAnalytics(workspace);
                            }
                            default -> System.err.println("[WARN] Ação desconhecida: " + action);
                        }
                    } catch (NexusValidationException e) {
                        System.err.println("[ERRO DE REGRAS] Falha no comando '" + line + "': " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("[ERRO TÉCNICO] Erro ao processar linha '" + line + "': " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERRO FATAL] " + e.getMessage());
        }
    }

    private void printAnalytics(Workspace ws) {
        System.out.println("\n=== RELATÓRIO ANALÍTICO NEXUS ===");
        System.out.println("Top Performers: " + ws.getTopPerformers());
        System.out.println("Gargalo do Sistema: " + ws.getGlobalBottleneck());
        System.out.println("=================================\n");
    }
}