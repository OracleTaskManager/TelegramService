package com.Oracle.TelegramService.service;

import com.Oracle.TelegramService.client.TaskServiceClient;
import com.Oracle.TelegramService.data.tasks.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.sql.Date;
import java.util.List;

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
                    Integer.parseInt(args[7])
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

}