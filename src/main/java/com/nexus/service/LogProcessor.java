package com.nexus.service;

import com.nexus.model.*;
import com.nexus.exception.NexusValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Processa um arquivo de log contendo comandos para manipulação do sistema Nexus.
 * Este processador interpreta linhas de comando no formato: COMANDO;PARAMETRO1;PARAMETRO2...
 * 
 * Suporta os seguintes comandos:
 * - CREATE_USER;username;email: Cria um novo usuário (com validação de e-mail)
 * - CREATE_PROJECT;projectName;budgetHours: Cria um novo projeto
 * - CREATE_TASK;taskName;deadline;effort;projectName: Cria uma tarefa vinculada a um projeto
 * - ASSIGN_USER;taskId;username: Atribui uma tarefa a um usuário
 * - CHANGE_STATUS;taskId;newStatus: Altera o status de uma tarefa (TO_DO, IN_PROGRESS, BLOCKED, DONE)
 * - REPORT_STATUS: Imprime os relatórios analíticos com Streams
 * 
 * Este processador adota a filosofia Fail-Fast: quando uma regra de negócio é violada,
 * uma NexusValidationException é lançada. O processador captura essas exceções,
 * incrementa o contador global de erros e continua processando as próximas linhas.
 * 
 * @author Sistema Nexus
 */
public class LogProcessor {

    /**
     * Processa um arquivo de log linha por linha, executando os comandos especificados.
     * 
     * O método lê o arquivo do classpath, ignora linhas em branco ou comentadas (prefixo #),
     * e executa cada comando conforme especificado. Em caso de erro, a exceção é capturada
     * e o processamento continua com a próxima linha.
     * 
     * @param fileName O nome do arquivo de log no classpath (ex: log_v1.txt).
     * @param workspace O workspace onde os dados serão inseridos.
     * @param users A lista global de usuários (sincronizada com workspace).
     * @throws IOException Se o arquivo não for encontrado ou houver erro ao ler.
     */
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
                        Task.totalValidationErrors++;
                        System.err.println("[ERRO DE REGRAS] Falha no comando '" + line + "': " + e.getMessage());
                    } catch (Exception e) {
                        Task.totalValidationErrors++;
                        System.err.println("[ERRO TÉCNICO] Erro ao processar linha '" + line + "': " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERRO FATAL] " + e.getMessage());
        }
    }

    /**
     * Imprime todos os relatórios analíticos gerados com Stream API.
     * Exibe os 4 relatórios exigidos: Top Performers, Usuários Sobrecarregados,
     * Saúde dos Projetos e Gargalo Global do Sistema.
     * 
     * @param ws O workspace contendo os dados para análise.
     */
    private void printAnalytics(Workspace ws) {
        System.out.println("\n=== RELATORIOS ANALITICOS ===");
        System.out.println("Top Performers: " + ws.getTopPerformers().stream()
            .map(User::getUsername).collect(Collectors.toList()));
        System.out.println("Usuarios Sobrecarregados: " + ws.getOverloadedUsers().stream()
            .map(User::getUsername).collect(Collectors.toList()));
        System.out.println("Saude dos Projetos: " + ws.getProjectHealthReport().entrySet().stream()
            .map(e -> e.getKey() + "=" + String.format("%.1f%%", e.getValue()))
            .collect(Collectors.joining(", ", "{", "}")));
        System.out.println("Gargalo do Sistema: " + ws.getGlobalBottleneck());
        System.out.println("===========================\n");
    }
}