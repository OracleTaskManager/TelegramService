package com.Oracle.TelegramService.client;

import com.Oracle.TelegramService.data.tasks.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "task-service", url = "${task.service.url}")
public interface TaskServiceClient {

    @PostMapping("/sprints/")
    ResponseEntity<SprintResponse> createSprint(
            @RequestHeader("Authorization") String token,
            @RequestBody SprintRegister sprintRegister
    );

    @GetMapping("/sprints/")
    ResponseEntity<List<SprintResponse>> getSprints(
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String token
    );

    @PostMapping("/sprints/{sprintId}/start")
    ResponseEntity<Void> startSprint(
            @PathVariable Long sprintId,
            @RequestHeader("Authorization") String token
    );

    @PostMapping("/tasks/")
    ResponseEntity<TaskResponse> createTask(
            @RequestHeader("Authorization") String token,
            @RequestBody TaskRegister taskRegister
    );

    @PostMapping("/tasksprint/add")
    ResponseEntity<Void> addTaskToSprint(
            @RequestHeader("Authorization") String token,
            @RequestBody TaskSprintRequest taskSprintRequest
    );

    @PostMapping("/taskassignments/add")
    ResponseEntity<Void> assignTaskToUser(
            @RequestHeader("Authorization") String token,
            @RequestBody TaskAssignmentRequest taskAssignment
    );

    @PostMapping("/tasks/change-status")
    ResponseEntity<Void> updateTaskStatus(
            @RequestHeader("Authorization") String token,
            @RequestBody TaskUpdateStatusRequest taskUpdateStatus
    );

    @PostMapping("/epics")
    ResponseEntity<EpicResponse> createEpic(
            @RequestHeader("Authorization") String token,
            @RequestBody EpicRegister epicRegister
    );
    @GetMapping("/tasks/my-tasks")
    ResponseEntity<List<TaskResponse>> getTasksByUser(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/tasks/completed/sprint/{sprint_Id}/users")
    ResponseEntity<List<UserTaskCompletedReport>> getTasksCompletedPerSprint(@PathVariable Long sprintId);

    @GetMapping("/report/tasks/completed/sprints/user/{userId}")
    ResponseEntity<List<UserTaskCompletedReport>> getTasksCompletedByUserPerSprint(@PathVariable Long userId);
}
