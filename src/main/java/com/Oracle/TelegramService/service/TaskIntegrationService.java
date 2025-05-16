package com.Oracle.TelegramService.service;

import com.Oracle.TelegramService.client.TaskServiceClient;
import com.Oracle.TelegramService.data.tasks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Service
public class TaskIntegrationService {
    @Autowired
    private TaskServiceClient taskServiceClient;

    @Autowired
    private SessionCache sessionCache;

    public SendMessage handleCreateSprint(Long chatid, String[] args){
        String token = sessionCache.getToken(chatid);
        if (token == null) {
            return new SendMessage(chatid.toString(), "Please login first.");
        }

        try{
            if(args.length < 3){
                return new SendMessage(chatid.toString(),
                        "Incorrect format. Please use /createsprint <name> <start_date> <end_date> YYYY-MM-DD");
            }

            SprintRegister sprintRegister = new SprintRegister(
                    args[0],
                    Date.valueOf(args[1]),
                    Date.valueOf(args[2])
            );

            ResponseEntity<SprintResponse> sprintResponse = taskServiceClient.createSprint("Bearer " + token, sprintRegister);

            if (sprintResponse.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatid.toString(), "Sprint created successfully: " + sprintResponse.getBody().toString());
            } else {
                return new SendMessage(chatid.toString(), "Failed to create sprint: " + sprintResponse.getStatusCode());
            }

        }catch (IllegalArgumentException e) {
            return new SendMessage(chatid.toString(), "Error: " + e.getMessage());
        }
    }

    public SendMessage handleCreateEpic(Long chatid, String[] args){

        String token = sessionCache.getToken(chatid);
        if (token == null) {
            return new SendMessage(chatid.toString(), "Please login first.");
        }

        try {
            if (args.length < 2) {
                return new SendMessage(chatid.toString(),
                        "Incorrect format. Please use /create_epic <title> <description>");
            }

            EpicRegister epicRegister = new EpicRegister(
                    args[0],
                    args[1]
            );

            ResponseEntity<EpicResponse> epicResponse = taskServiceClient.createEpic("Bearer " + token, epicRegister);

            if (epicResponse.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatid.toString(), "Epic created successfully: " + epicResponse.getBody().toString());
            } else {
                return new SendMessage(chatid.toString(), "Failed to create epic: " + epicResponse.getStatusCode());
            }

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatid.toString(), "Error: " + e.getMessage());
        }
    }

    public SendMessage handleCreateTask(Long chatid, String[] args){
        String token = sessionCache.getToken(chatid);
        if (token == null) {
            return new SendMessage(chatid.toString(), "Please login first.");
        }

        try {
            if (args.length < 2) {
                return new SendMessage(chatid.toString(),
                        "Incorrect format. Please use /create_task <title> <description> <epic_id> <priority> <type> <estimated_deadline> <real_deadline> <user_points>");
            }

            Priority priority = Priority.valueOf(args[3]);

            Type type = Type.valueOf(args[4]);

            TaskRegister taskRegister = new TaskRegister(
                    args[0],
                    args[1],
                    Long.parseLong(args[2]),
                    priority,
                    type,
                    Date.valueOf(args[5]),
                    Date.valueOf(args[6]),
                    Integer.parseInt(args[7]),
                    Integer.parseInt(args[8]),
                    Integer.parseInt(args[9])
            );

            ResponseEntity<TaskResponse> taskResponse = taskServiceClient.createTask("Bearer " + token, taskRegister);

            if (taskResponse.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatid.toString(), "Task created successfully: " + taskResponse.getBody().toString());
            } else {
                return new SendMessage(chatid.toString(), "Failed to create task: " + taskResponse.getStatusCode());
            }

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatid.toString(), "Error: " + e.getMessage());
        }
    }

    public SendMessage handleAddTaskToSprint(Long chatid, String[] args){
        String token = sessionCache.getToken(chatid);
        if (token == null) {
            return new SendMessage(chatid.toString(), "Please login first.");
        }

        try {
            if (args.length < 2) {
                return new SendMessage(chatid.toString(),
                        "Incorrect format. Please use /addtasktosprint <task_id> <sprint_id>");
            }

            TaskSprintRequest taskSprintRequest = new TaskSprintRequest(
                    Long.parseLong(args[0]),
                    Long.parseLong(args[1])
            );

            ResponseEntity<Void> response = taskServiceClient.addTaskToSprint("Bearer " + token, taskSprintRequest);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatid.toString(), "Task added to sprint successfully.");
            } else {
                return new SendMessage(chatid.toString(), "Failed to add task to sprint: " + response.getStatusCode());
            }

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatid.toString(), "Error: " + e.getMessage());
        }
    }

    public SendMessage handleAssignTaskToUser(Long chatid, String[] args){
        String token = sessionCache.getToken(chatid);
        if (token == null) {
            return new SendMessage(chatid.toString(), "Please login first.");
        }

        try {
            if (args.length < 2) {
                return new SendMessage(chatid.toString(),
                        "Incorrect format. Please use /assigntask <task_id> <user_id>");
            }

            TaskAssignmentRequest taskAssignmentRequest = new TaskAssignmentRequest(
                    Long.parseLong(args[0]),
                    Long.parseLong(args[1])
            );

            ResponseEntity<Void> response = taskServiceClient.assignTaskToUser("Bearer " + token, taskAssignmentRequest);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatid.toString(), "Task assigned to user successfully.");
            } else {
                return new SendMessage(chatid.toString(), "Failed to assign task to user: " + response.getStatusCode());
            }

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatid.toString(), "Error: " + e.getMessage());
        }
    }

    public SendMessage handleUpdateTaskStatus(Long chatid, String[] args){
        String token = sessionCache.getToken(chatid);
        if (token == null) {
            return new SendMessage(chatid.toString(), "Please login first.");
        }

        try {
            if (args.length < 2) {
                return new SendMessage(chatid.toString(),
                        "Incorrect format. Please use /updatetaskstatus <task_id> <status>");
            }

            TaskUpdateStatusRequest taskUpdateStatusRequest = new TaskUpdateStatusRequest(
                    Long.parseLong(args[0]),
                    args[1]
            );

            ResponseEntity<Void> response = taskServiceClient.updateTaskStatus("Bearer " + token, taskUpdateStatusRequest);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatid.toString(), "Task status updated successfully.");
            } else {
                return new SendMessage(chatid.toString(), "Failed to update task status: " + response.getStatusCode());
            }

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatid.toString(), "Error: " + e.getMessage());
        }
    }

    public SendMessage handleGetMyTasks(Long chatId) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            ResponseEntity<List<TaskResponse>> response = taskServiceClient.getTasksByUser("Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful()) {
                List<TaskResponse> tasks = response.getBody();
                if (tasks == null || tasks.isEmpty()) {
                    return new SendMessage(chatId.toString(), "No tasks found.");
                }

                StringBuilder message = new StringBuilder("Your tasks:\n");
                for (TaskResponse task : tasks) {
                    message.append("- ").append(task.title()).append(": ").append(task.description()).append("\n");
                }

                return new SendMessage(chatId.toString(), message.toString());
            } else {
                return new SendMessage(chatId.toString(), "Failed to get tasks: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "Error: " + e.getMessage());
        }
    }
    public SendMessage handleShowCompletedTasksPerUserPerSprint(Long chatid, String[] args) {
        String token = sessionCache.getToken(chatid);
        if (token == null) {
            return new SendMessage(chatid.toString(), "Please login first.");
        }

        try {
            if (args.length < 1) {
                return new SendMessage(chatid.toString(),
                        "Incorrect format. Please use /completed_tasks_user <userId>");
            }

            Long userId = Long.parseLong(args[0]);

            ResponseEntity<List<UserTaskCompletedReport>> response = taskServiceClient
                    .getTasksCompletedByUserPerSprint(userId);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<UserTaskCompletedReport> reportList = response.getBody();

                if (reportList.isEmpty()) {
                    return new SendMessage(chatid.toString(), "No completed tasks found for the user.");
                }

                StringBuilder message = new StringBuilder();
                for (UserTaskCompletedReport report : reportList) {
                    message.append("👤 *User:* ").append(report.getUserName())
                            .append(" (ID: ").append(report.getUserId()).append(")\n")
                            .append("📦 *Sprint:* ").append(report.getSprintName())
                            .append(" (ID: ").append(report.getSprintId()).append(")\n")
                            .append("✅ *Total Tasks Completed:* ").append(report.getTotalTasksCompleted()).append("\n\n");

                    for (UserTaskCompletedReport.CompletedTask task : report.getCompletedTasks()) {
                        message.append("📝 *Task:* ").append(task.getTaskTitle()).append(" (ID: ").append(task.getTaskId()).append(")\n")
                                .append("📅 Date: ").append(task.getCompletionDate()).append("\n")
                                .append("⏱ Hours: ").append(task.getRealHours()).append("\n\n");
                    }

                    message.append("——————————————————————\n\n");
                }

                SendMessage sendMessage = new SendMessage(chatid.toString(), message.toString());
                sendMessage.setParseMode("Markdown"); // Optional: to enable bold formatting
                return sendMessage;
            } else {
                return new SendMessage(chatid.toString(), "Could not retrieve the report: " + response.getStatusCode());
            }

        } catch (NumberFormatException e) {
            return new SendMessage(chatid.toString(), "Invalid user ID format.");
        } catch (Exception e) {
            return new SendMessage(chatid.toString(), "Error: " + e.getMessage());
        }
    }

    public SendMessage handleShowCompletedTasksPerSprint(Long chatid, String[] args) {
        String token = sessionCache.getToken(chatid);
        if (token == null) {
            return new SendMessage(chatid.toString(), "Please login first.");
        }

        try {
            if (args.length < 1) {
                return new SendMessage(chatid.toString(),
                        "Incorrect format. Please use /completed_tasks_sprint <sprintId>");
            }

            Long sprintId = Long.parseLong(args[0]);

            ResponseEntity<List<UserTaskCompletedReport>> response =
                    taskServiceClient.getTasksCompletedPerSprint(sprintId);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<UserTaskCompletedReport> reportList = response.getBody();

                List<UserTaskCompletedReport.CompletedTask> allTasks = reportList.stream()
                        .flatMap(report -> report.getCompletedTasks().stream())
                        .toList();

                if (allTasks.isEmpty()) {
                    return new SendMessage(chatid.toString(), "No completed tasks found for this sprint.");
                }

                StringBuilder message = new StringBuilder("📊 *Tasks Completed in Sprint " + sprintId + "*\n\n");

                for (UserTaskCompletedReport.CompletedTask task : allTasks) {
                    message.append("📝 *Task:* ").append(task.getTaskTitle())
                            .append(" (ID: ").append(task.getTaskId()).append(")\n")
                            .append("📅 Date: ").append(task.getCompletionDate()).append("\n")
                            .append("⏱ Hours: ").append(task.getRealHours()).append("\n\n");
                }

                SendMessage sendMessage = new SendMessage(chatid.toString(), message.toString());
                sendMessage.setParseMode("Markdown");
                return sendMessage;

            } else {
                return new SendMessage(chatid.toString(), "Could not retrieve the report: " + response.getStatusCode());
            }

        } catch (NumberFormatException e) {
            return new SendMessage(chatid.toString(), "Invalid sprint ID format.");
        } catch (Exception e) {
            return new SendMessage(chatid.toString(), "Error: " + e.getMessage());
        }
    }

}