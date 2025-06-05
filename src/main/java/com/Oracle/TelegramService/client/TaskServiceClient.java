package com.Oracle.TelegramService.client;

import com.Oracle.TelegramService.data.tasks.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "task-service", url = "${task.service.url}")
public interface TaskServiceClient {

    // ========== SPRINT ENDPOINTS ==========

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

    @PutMapping("/sprints/")
    ResponseEntity<SprintResponse> updateSprint(
            @RequestHeader("Authorization") String token,
            @RequestBody SprintUpdate sprintUpdate
    );

    @DeleteMapping("/sprints/")
    ResponseEntity<Void> deleteSprint(
            @RequestParam Long sprintId,
            @RequestHeader("Authorization") String token
    );

    @PostMapping("/sprints/{sprintId}/start")
    ResponseEntity<Void> startSprint(
            @PathVariable Long sprintId,
            @RequestHeader("Authorization") String token
    );

    // ========== TASK ENDPOINTS ==========

    @PostMapping("/tasks/")
    ResponseEntity<TaskResponse> createTask(
            @RequestHeader("Authorization") String token,
            @RequestBody TaskRegister taskRegister
    );

    @DeleteMapping("/tasks/")
    ResponseEntity<Void> deleteTask(
            @RequestParam("task_id") Long taskId,
            @RequestHeader("Authorization") String token
    );

    @PutMapping("/tasks/update-task/{task_id}")
    ResponseEntity<TaskResponse> updateTaskContent(
            @PathVariable("task_id") Long taskId,
            @RequestHeader("Authorization") String token,
            @RequestBody TaskUpdateContent taskUpdateContent
    );

    @PutMapping("/tasks/update-my-task/{task_id}")
    ResponseEntity<TaskResponse> updateMyTask(
            @PathVariable("task_id") Long taskId,
            @RequestHeader("Authorization") String token,
            @RequestBody TaskUpdateContent taskUpdateContent
    );

    @PostMapping("/tasks/change-status/{task_id}")
    ResponseEntity<TaskResponse> changeTaskStatus(
            @PathVariable("task_id") Long taskId,
            @RequestHeader("Authorization") String token,
            @RequestBody TaskUpdateStatus taskUpdateStatus
    );

    @GetMapping("/tasks/my-tasks")
    ResponseEntity<List<TaskResponse>> getMyTasks(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/tasks/all")
    ResponseEntity<List<TaskResponse>> getAllTasks(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/tasks/task/{task_id}")
    ResponseEntity<TaskResponse> findTaskById(
            @PathVariable("task_id") Long taskId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/tasks/title/{title}")
    ResponseEntity<List<TaskResponse>> findTasksByTitle(
            @PathVariable String title,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/tasks/status/{status}")
    ResponseEntity<List<TaskResponse>> findTasksByStatus(
            @PathVariable String status,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/tasks/priority/{priority}")
    ResponseEntity<List<TaskResponse>> findTasksByPriority(
            @PathVariable String priority,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/tasks/type/{type}")
    ResponseEntity<List<TaskResponse>> findTasksByType(
            @PathVariable String type,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/tasks/user-points/{user_points}")
    ResponseEntity<List<TaskResponse>> findTasksByUserPoints(
            @PathVariable("user_points") Integer userPoints,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/tasks/estimatedhours/{hours}")
    ResponseEntity<List<TaskResponse>> findTasksByEstimatedHours(
            @PathVariable Integer hours,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/tasks/realhours/{hours}")
    ResponseEntity<List<TaskResponse>> findTasksByRealHours(
            @PathVariable Integer hours,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/tasks/kpi-tasks")
    ResponseEntity<List<TaskKPIView>> getKpiTasks(
            @RequestParam String status,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to,
            @RequestHeader("Authorization") String token
    );

    // ========== EPIC ENDPOINTS ==========

    @PostMapping("/epics")
    ResponseEntity<EpicResponse> createEpic(
            @RequestHeader("Authorization") String token,
            @RequestBody EpicRegister epicRegister
    );

    @PutMapping("/epics/")
    ResponseEntity<EpicResponse> updateEpic(
            @RequestHeader("Authorization") String token,
            @RequestBody EpicUpdate epicUpdate
    );

    @DeleteMapping("/epics/")
    ResponseEntity<Void> deleteEpic(
            @RequestParam Long epicId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/epics/all")
    ResponseEntity<List<EpicResponse>> getAllEpics(
            @RequestHeader("Authorization") String token
    );

    // ========== TASK SPRINT ENDPOINTS ==========

    @PostMapping("/tasksprint/add")
    ResponseEntity<Void> addTaskToSprint(
            @RequestHeader("Authorization") String token,
            @RequestBody TaskSprintRequest taskSprintRequest
    );

    @DeleteMapping("/tasksprint/remove")
    ResponseEntity<Void> removeTaskFromSprint(
            @RequestHeader("Authorization") String token,
            @RequestBody TaskSprintRequest taskSprintRequest
    );

    // ========== TASK ASSIGNMENT ENDPOINTS ==========

    @PostMapping("/taskassignments/add")
    ResponseEntity<Void> assignTaskToUser(
            @RequestHeader("Authorization") String token,
            @RequestBody TaskAssignmentRequest taskAssignment
    );

    @DeleteMapping("/taskassignments/remove")
    ResponseEntity<Void> removeTaskAssignment(
            @RequestHeader("Authorization") String token,
            @RequestBody TaskAssignmentRequest taskAssignment
    );

    @GetMapping("/taskassignments/")
    ResponseEntity<List<TaskAssignmentResponse>> getAllTaskAssignments(
            @RequestHeader("Authorization") String token
    );

    // ========== TASK DEPENDENCY ENDPOINTS ==========

    @PostMapping("/task_dependencies/")
    ResponseEntity<TaskDependencyResponse> saveTaskDependency(
            @RequestHeader("Authorization") String token,
            @RequestBody TaskDependencyRegister taskDependencyRegister
    );

    @DeleteMapping("/task_dependencies/")
    ResponseEntity<Void> deleteTaskDependency(
            @RequestHeader("Authorization") String token,
            @RequestBody Long taskDependencyId
    );

    @GetMapping("/task_dependencies/all")
    ResponseEntity<List<TaskDependencyResponse>> getAllTaskDependencies(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/task_dependencies/{task_dependency_id}")
    ResponseEntity<TaskDependencyResponse> getTaskDependencyById(
            @PathVariable("task_dependency_id") Long taskDependencyId,
            @RequestHeader("Authorization") String token
    );

    // ========== REPORT ENDPOINTS ==========

    // Tasks Completed Reports
    @GetMapping("/reports/tasks/completed/sprints/users")
    ResponseEntity<List<UserTasksCompletedReport>> getTasksCompletedPerAllSprintsAndAllUsers(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/tasks/completed/sprints/user/{userId}")
    ResponseEntity<List<UserTasksCompletedReport>> getTasksCompletedByUserPerSprint(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/tasks/completed/sprint/{sprintId}/users")
    ResponseEntity<List<UserTasksCompletedReport>> getTasksCompletedPerSprint(
            @PathVariable Long sprintId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/tasks/completed/sprint/{sprintId}/user/{userId}")
    ResponseEntity<UserTasksCompletedReport> getTasksCompletedPerSprintAndUser(
            @PathVariable Long sprintId,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token
    );

    // Hours Reports
    @GetMapping("/reports/hours/sprints")
    ResponseEntity<List<SprintHoursReport>> getHoursPerAllSprints(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/hours/sprint/{sprintId}")
    ResponseEntity<SprintHoursReport> getHoursPerSprint(
            @PathVariable Long sprintId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/hours/sprints/users")
    ResponseEntity<List<UserSprintHoursReport>> getHoursPerAllSprintsAndAllUsers(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/hours/sprints/user/{userId}")
    ResponseEntity<List<UserSprintHoursReport>> getHoursPerAllSprintsAndUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/hours/sprint/{sprintId}/users")
    ResponseEntity<List<UserSprintHoursReport>> getHoursPerSprintAndAllUsers(
            @PathVariable Long sprintId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/hours/sprint/{sprintId}/user/{userId}")
    ResponseEntity<UserSprintHoursReport> getHoursPerSprintAndUser(
            @PathVariable Long sprintId,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/hours/sprints/teams")
    ResponseEntity<List<TeamSprintHoursReport>> getHoursPerAllSprintsAndAllTeams(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/hours/sprints/team/{teamId}")
    ResponseEntity<List<TeamSprintHoursReport>> getHoursPerAllSprintsAndTeam(
            @PathVariable Long teamId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/hours/sprint/{sprintId}/teams")
    ResponseEntity<List<TeamSprintHoursReport>> getHoursPerSprintAndAllTeams(
            @PathVariable Long sprintId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/reports/hours/sprint/{sprintId}/team/{teamId}")
    ResponseEntity<TeamSprintHoursReport> getHoursPerSprintAndTeam(
            @PathVariable Long sprintId,
            @PathVariable Long teamId,
            @RequestHeader("Authorization") String token
    );
}