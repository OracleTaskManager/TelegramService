package com.Oracle.TelegramService.service;

import com.Oracle.TelegramService.client.TaskServiceClient;
import com.Oracle.TelegramService.data.tasks.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TaskIntegrationService {

    @Autowired
    private TaskServiceClient taskServiceClient;

    @Autowired
    private SessionCache sessionCache;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ========== SPRINT METHODS ==========

    public SendMessage handleCreateSprint(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 3) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /createsprint <name> <start_date> <end_date> YYYY-MM-DD");
            }

            SprintRegister sprintRegister = new SprintRegister(
                    args[0],
                    Date.valueOf(args[1]),
                    Date.valueOf(args[2])
            );

            ResponseEntity<SprintResponse> sprintResponse = taskServiceClient.createSprint("Bearer " + token, sprintRegister);

            if (sprintResponse.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Sprint created successfully: " + sprintResponse.getBody().toString());
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to create sprint: " + sprintResponse.getStatusCode());
            }

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleUpdateSprint(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 4) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /updatesprint <sprint_id> <name> <start_date> <end_date>");
            }
            Long sprintId = Long.parseLong(args[0]);

            SprintUpdate sprintUpdate = new SprintUpdate(
                    args[1],
                    Date.valueOf(parseDate(args[2])),
                    Date.valueOf(parseDate(args[3]))
            );

            ResponseEntity<SprintResponse> response = taskServiceClient.updateSprint("Bearer " + token, sprintId,sprintUpdate);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Sprint updated successfully!");
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to update sprint: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleDeleteSprint(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 1) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /deletesprint <sprint_id>");
            }

            ResponseEntity<Void> response = taskServiceClient.deleteSprint(Long.parseLong(args[0]), "Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Sprint deleted successfully!");
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to delete sprint: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleGetSprints(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            String status = args.length > 0 ? args[0] : null;
            ResponseEntity<List<SprintResponse>> response = taskServiceClient.getSprints(status, "Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<SprintResponse> sprints = response.getBody();
                if (sprints.isEmpty()) {
                    return new SendMessage(chatId.toString(), "No sprints found.");
                }

                StringBuilder message = new StringBuilder("🏃‍♂️ *Sprints:*\n\n");
                for (SprintResponse sprint : sprints) {
                    message.append("*ID:* ").append(sprint.sprintId()).append("\n")
                            .append("*Name:* ").append(sprint.name()).append("\n")
                            .append("*Status:* ").append(sprint.status()).append("\n")
                            .append("*Start:* ").append(sprint.startDate()).append("\n")
                            .append("*End:* ").append(sprint.endDate()).append("\n\n");
                }

                SendMessage sendMessage = new SendMessage(chatId.toString(), message.toString());
                sendMessage.setParseMode("Markdown");
                return sendMessage;
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to get sprints: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    // ========== TASK METHODS ==========

    public SendMessage handleCreateTask(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 9) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /create_task <title> <description> <epic_id> <priority> <type> <estimated_deadline> <real_deadline> <user_points> <estimated_hours>");
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
                    0 // Real hours default to 0
            );

            ResponseEntity<TaskResponse> taskResponse = taskServiceClient.createTask("Bearer " + token, taskRegister);

            if (taskResponse.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Task created successfully: " + taskResponse.getBody().toString());
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to create task: " + taskResponse.getStatusCode());
            }

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleUpdateTask(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 10) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /updatetask <task_id> <title> <description> <epic_id> <priority> <status> <type> <estimated_deadline> <real_deadline> <user_points>");
            }

            Long taskId = Long.parseLong(args[0]);
            TaskUpdateContent taskUpdate = new TaskUpdateContent(
                    args[1],                                    // title
                    args[2],                                    // description
                    Long.parseLong(args[3]),                    // epic_id
                    args[4],                                    // priority
                    args[5],                                    // status
                    args[6],                                    // type
                    parseDate(args[7]).atStartOfDay(),          // estimated_deadline
                    parseDate(args[8]).atStartOfDay(),          // real_deadline
                    0,                                          // realHours
                    0,                                          // estimatedHours
                    Integer.parseInt(args[9])                   // user_points
            );

            ResponseEntity<TaskResponse> response = taskServiceClient.updateTaskContent(taskId, "Bearer " + token, taskUpdate);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Task updated successfully!");
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to update task: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleDeleteTask(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 1) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /deletetask <task_id>");
            }

            ResponseEntity<Void> response = taskServiceClient.deleteTask(Long.parseLong(args[0]), "Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Task deleted successfully!");
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to delete task: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleGetTaskById(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 1) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /gettask <task_id>");
            }

            ResponseEntity<TaskResponse> response = taskServiceClient.findTaskById(Long.parseLong(args[0]), "Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                TaskResponse task = response.getBody();
                StringBuilder message = new StringBuilder("📝 *Task Details:*\n\n")
                        .append("*ID:* ").append(task.id()).append("\n") //perhaps no ID was assigned
                        .append("*Title:* ").append(task.title()).append("\n")
                        .append("*Description:* ").append(task.description()).append("\n")
                        .append("*Status:* ").append(task.status()).append("\n")
                        .append("*Priority:* ").append(task.priority()).append("\n")
                        .append("*Type:* ").append(task.type()).append("\n")
                        .append("*Story Points:* ").append(task.user_points()).append("\n")
                        .append("*Estimated Hours:* ").append(task.estimatedHours()).append("\n")
                        .append("*Real Hours:* ").append(task.realHours()).append("\n");

                SendMessage sendMessage = new SendMessage(chatId.toString(), message.toString());
                sendMessage.setParseMode("Markdown");
                return sendMessage;
            } else {
                return new SendMessage(chatId.toString(), "❌ Task not found or failed to retrieve.");
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleGetAllTasks(Long chatId) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            ResponseEntity<List<TaskResponse>> response = taskServiceClient.getAllTasks("Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<TaskResponse> tasks = response.getBody();
                if (tasks.isEmpty()) {
                    return new SendMessage(chatId.toString(), "No tasks found.");
                }

                StringBuilder message = new StringBuilder("📝 *All Tasks:*\n\n");
                for (TaskResponse task : tasks) {
                    //optional
                    if (!"Done".equals(task.status())){
                        message.append("*").append(task.id()).append(":* ").append(task.title())
                                .append(" (").append(task.status()).append(")\n");
                    }
                }

                SendMessage sendMessage = new SendMessage(chatId.toString(), message.toString());
                sendMessage.setParseMode("Markdown");
                return sendMessage;
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to get tasks: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleGetMyTasks(Long chatId) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            ResponseEntity<List<TaskResponse>> response = taskServiceClient.getMyTasks("Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful()) {
                List<TaskResponse> tasks = response.getBody();
                if (tasks == null || tasks.isEmpty()) {
                    return new SendMessage(chatId.toString(), "No tasks found.");
                }

                StringBuilder message = new StringBuilder("📝 *Your tasks:*\n\n");
                for (TaskResponse task : tasks) { // taskResponse
                    if (!"Done".equals(task.status())){
                        message.append(task.id()).append(" -> ").append(task.title()).append(": ").append(task.description()).append("\n");
                    }
                }

                SendMessage sendMessage = new SendMessage(chatId.toString(), message.toString());
                sendMessage.setParseMode("Markdown");
                return sendMessage;
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to get tasks: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleFindTasksByStatus(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 1) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /findtasksbystatus <status>");
            }

            ResponseEntity<List<TaskResponse>> response = taskServiceClient.findTasksByStatus(args[0], "Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<TaskResponse> tasks = response.getBody();
                if (tasks.isEmpty()) {
                    return new SendMessage(chatId.toString(), "No tasks found with status: " + args[0]);
                }

                StringBuilder message = new StringBuilder("📝 *Tasks with status ").append(args[0]).append(":*\n\n");
                for (TaskResponse task : tasks) {
                    message.append("*").append(task.id()).append(":* ").append(task.title()).append("\n");
                }

                SendMessage sendMessage = new SendMessage(chatId.toString(), message.toString());
                sendMessage.setParseMode("Markdown");
                return sendMessage;
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to find tasks: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleFindTasksByPriority(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 1) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /findtasksbypriority <priority>");
            }

            ResponseEntity<List<TaskResponse>> response = taskServiceClient.findTasksByPriority(args[0], "Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<TaskResponse> tasks = response.getBody();
                if (tasks.isEmpty()) {
                    return new SendMessage(chatId.toString(), "No tasks found with priority: " + args[0]);
                }

                StringBuilder message = new StringBuilder("📝 *Tasks with priority ").append(args[0]).append(":*\n\n");
                for (TaskResponse task : tasks) {
                    message.append("*").append(task.id()).append(":* ").append(task.title())
                            .append(" (").append(task.status()).append(")\n");
                }

                SendMessage sendMessage = new SendMessage(chatId.toString(), message.toString());
                sendMessage.setParseMode("Markdown");
                return sendMessage;
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to find tasks: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    // ========== EPIC METHODS ==========

    public SendMessage handleCreateEpic(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 2) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /create_epic <title> <description>");
            }

            EpicRegister epicRegister = new EpicRegister(
                    args[0],
                    args[1]
            );

            ResponseEntity<EpicResponse> epicResponse = taskServiceClient.createEpic("Bearer " + token, epicRegister);

            if (epicResponse.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Epic created successfully: " + epicResponse.getBody().toString());
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to create epic: " + epicResponse.getStatusCode());
            }

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleUpdateEpic(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 4) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /updateepic <epic_id> <title> <description> <status>");
            }

            EpicUpdate epicUpdate = new EpicUpdate(
                    Long.parseLong(args[0]),
                    args[1],
                    args[2],
                    args[3] // ToDo, InProgress, Done
            );

            ResponseEntity<EpicResponse> response = taskServiceClient.updateEpic("Bearer " + token, epicUpdate);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Epic updated successfully!");
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to update epic: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleDeleteEpic(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 1) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /deleteepic <epic_id>");
            }

            ResponseEntity<Void> response = taskServiceClient.deleteEpic(Long.parseLong(args[0]), "Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Epic deleted successfully!");
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to delete epic: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleGetAllEpics(Long chatId) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            ResponseEntity<List<EpicResponse>> response = taskServiceClient.getAllEpics("Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<EpicResponse> epics = response.getBody();
                if (epics.isEmpty()) {
                    return new SendMessage(chatId.toString(), "No epics found.");
                }

                StringBuilder message = new StringBuilder("📋 *All Epics:*\n\n");
                for (EpicResponse epic : epics) {
                    message.append("*").append(epic.epicId()).append(":* ").append(epic.title())
                            .append(" - ").append(epic.description()).append("\n");
                }

                SendMessage sendMessage = new SendMessage(chatId.toString(), message.toString());
                sendMessage.setParseMode("Markdown");
                return sendMessage;
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to get epics: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    // ========== TASK SPRINT METHODS ==========

    public SendMessage handleAddTaskToSprint(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 2) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /addtasktosprint <task_id> <sprint_id>");
            }

            TaskSprintRequest taskSprintRequest = new TaskSprintRequest(
                    Long.parseLong(args[0]),
                    Long.parseLong(args[1])
            );

            ResponseEntity<Void> response = taskServiceClient.addTaskToSprint("Bearer " + token, taskSprintRequest);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Task added to sprint successfully.");
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to add task to sprint: " + response.getStatusCode());
            }

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleRemoveTaskFromSprint(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 2) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /removetaskfromsprint <task_id> <sprint_id>");
            }

            TaskSprintRequest taskSprintRequest = new TaskSprintRequest(
                    Long.parseLong(args[0]),
                    Long.parseLong(args[1])
            );

            ResponseEntity<Void> response = taskServiceClient.removeTaskFromSprint("Bearer " + token, taskSprintRequest);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Task removed from sprint successfully.");
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to remove task from sprint: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    // ========== TASK ASSIGNMENT METHODS ==========

    public SendMessage handleAssignTaskToUser(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 2) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /assigntask <task_id> <user_id>");
            }

            TaskAssignmentRequest taskAssignmentRequest = new TaskAssignmentRequest(
                    Long.parseLong(args[0]),
                    Long.parseLong(args[1])
            );

            ResponseEntity<Void> response = taskServiceClient.assignTaskToUser("Bearer " + token, taskAssignmentRequest);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Task assigned to user successfully.");
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to assign task to user: " + response.getStatusCode());
            }

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleRemoveTaskAssignment(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 2) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /removetaskassignment <task_id> <user_id>");
            }

            TaskAssignmentRequest taskAssignmentRequest = new TaskAssignmentRequest(
                    Long.parseLong(args[0]),
                    Long.parseLong(args[1])
            );

            ResponseEntity<Void> response = taskServiceClient.removeTaskAssignment("Bearer " + token, taskAssignmentRequest);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Task assignment removed successfully.");
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to remove task assignment: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    public SendMessage handleGetAllTaskAssignments(Long chatId) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            ResponseEntity<List<TaskAssignmentResponse>> response = taskServiceClient.getAllTaskAssignments("Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<TaskAssignmentResponse> assignments = response.getBody();
                if (assignments.isEmpty()) {
                    return new SendMessage(chatId.toString(), "No task assignments found.");
                }

                StringBuilder message = new StringBuilder("👥 *Task Assignments:*\n\n");
                for (TaskAssignmentResponse assignment : assignments) {
                    message.append("*Task ").append(assignment.taskId()).append(":* ")
                            .append(assignment.taskTitle()).append("\n") //tasktitle and username missing 10-june 20:10
                            .append("*Assigned to:* ").append(assignment.userName())
                            .append(" (ID: ").append(assignment.userId()).append(")\n\n");
                }

                SendMessage sendMessage = new SendMessage(chatId.toString(), message.toString());
                sendMessage.setParseMode("Markdown");
                return sendMessage;
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to get task assignments: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    // ========== TASK STATUS UPDATE ==========

    public SendMessage handleUpdateTaskStatus(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 2) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /updatetaskstatus <task_id> <status>");
            }

            Long taskId = Long.parseLong(args[0]);
            TaskUpdateStatus taskUpdateStatus = new TaskUpdateStatus(
                    args[1],
                    LocalDateTime.now(),
                    0 // Default real hours
            );

            ResponseEntity<TaskResponse> response = taskServiceClient.changeTaskStatus(taskId, "Bearer " + token, taskUpdateStatus);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Task status updated successfully.");
            } else {
                return new SendMessage(chatId.toString(), "❌ Failed to update task status: " + response.getStatusCode());
            }

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    // ========== REPORT METHODS ==========

    public SendMessage handleShowCompletedTasksPerUserPerSprint(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 1) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /completed_tasks_user <userId>");
            }

            Long userId = Long.parseLong(args[0]);

            ResponseEntity<List<UserTasksCompletedReport>> response = taskServiceClient
                    .getTasksCompletedByUserPerSprint(userId, "Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<UserTasksCompletedReport> reportList = response.getBody();

                if (reportList.isEmpty()) {
                    return new SendMessage(chatId.toString(), "No completed tasks found for the user.");
                }

                StringBuilder message = new StringBuilder();
                for (UserTasksCompletedReport report : reportList) {
                    message.append("👤 *User:* ").append(report.userName())
                            .append(" (ID: ").append(report.userId()).append(")\n")
                            .append("📦 *Sprint:* ").append(report.sprintName())
                            .append(" (ID: ").append(report.sprintId()).append(")\n")
                            .append("✅ *Total Tasks Completed:* ").append(report.totalTasksCompleted()).append("\n\n");

                    for (CompletedTask task : report.completedTasks()) {
                        message.append("📝 *Task:* ").append(task.taskTitle()).append(" (ID: ").append(task.taskId()).append(")\n")
                                .append("📅 Date: ").append(task.completionDate()).append("\n")
                                .append("⏱ Hours: ").append(task.realHours()).append("\n\n");
                    }

                    message.append("——————————————————————\n\n");
                }

                SendMessage sendMessage = new SendMessage(chatId.toString(), message.toString());
                sendMessage.setParseMode("Markdown");
                return sendMessage;
            } else {
                return new SendMessage(chatId.toString(), "Could not retrieve the report: " + response.getStatusCode());
            }

        } catch (NumberFormatException e) {
            return new SendMessage(chatId.toString(), "Invalid user ID format.");
        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "Error: " + e.getMessage());
        }
    }

    public SendMessage handleShowCompletedTasksPerSprint(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 1) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /completed_tasks_sprint <sprintId>");
            }

            Long sprintId = Long.parseLong(args[0]);

            ResponseEntity<List<UserTasksCompletedReport>> response =
                    taskServiceClient.getTasksCompletedPerSprint(sprintId, "Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<UserTasksCompletedReport> reportList = response.getBody();

                List<CompletedTask> allTasks = reportList.stream()
                        .flatMap(report -> report.completedTasks().stream())
                        .toList();

                if (allTasks.isEmpty()) {
                    return new SendMessage(chatId.toString(), "No completed tasks found for this sprint.");
                }

                StringBuilder message = new StringBuilder("📊 *Tasks Completed in Sprint " + sprintId + "*\n\n");

                for (CompletedTask task : allTasks) {
                    message.append("📝 *Task:* ").append(task.taskTitle())
                            .append(" (ID: ").append(task.taskId()).append(")\n")
                            .append("📅 Date: ").append(task.completionDate()).append("\n")
                            .append("⏱ Hours: ").append(task.realHours()).append("\n\n");
                }

                SendMessage sendMessage = new SendMessage(chatId.toString(), message.toString());
                sendMessage.setParseMode("Markdown");
                return sendMessage;

            } else {
                return new SendMessage(chatId.toString(), "Could not retrieve the report: " + response.getStatusCode());
            }

        } catch (NumberFormatException e) {
            return new SendMessage(chatId.toString(), "Invalid sprint ID format.");
        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "Error: " + e.getMessage());
        }
    }

    public SendMessage handleGetSprintHoursReport(Long chatId, String[] args) {
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            if (args.length < 1) {
                return new SendMessage(chatId.toString(),
                        "Incorrect format. Please use /sprinthoursreport <sprintId>");
            }

            Long sprintId = Long.parseLong(args[0]);
            ResponseEntity<SprintHoursReport> response = taskServiceClient.getHoursPerSprint(sprintId, "Bearer " + token);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                SprintHoursReport report = response.getBody();

                StringBuilder message = new StringBuilder("⏱️ *Sprint Hours Report*\n\n")
                        .append("*Sprint:* ").append(report.sprintName()).append(" (ID: ").append(report.sprintId()).append(")\n")
                        .append("*Total Hours:* ").append(report.totalHours()).append("\n")
                        .append("*Start Date:* ").append(report.startDate()).append("\n")
                        .append("*End Date:* ").append(report.endDate()).append("\n\n")
                        .append("*Task Details:*\n");

                for (TaskHoursDetail task : report.taskDetails()) {
                    message.append("📝 ").append(task.taskTitle())
                            .append(" - Real: ").append(task.realHours())
                            .append("h, Estimated: ").append(task.estimatedHours()).append("h\n");
                }

                SendMessage sendMessage = new SendMessage(chatId.toString(), message.toString());
                sendMessage.setParseMode("Markdown");
                return sendMessage;
            } else {
                return new SendMessage(chatId.toString(), "❌ Could not retrieve the report: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    // ========== UTILITY METHODS ==========

    private java.time.LocalDate parseDate(String dateStr) {
        return java.time.LocalDate.parse(dateStr, DATE_FORMATTER);
    }
}