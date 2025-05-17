package com.Oracle.TelegramService.service;

import com.Oracle.TelegramService.client.TaskServiceClient;
import com.Oracle.TelegramService.data.tasks.TaskRegister;
import com.Oracle.TelegramService.data.tasks.TaskResponse;
import com.Oracle.TelegramService.data.tasks.Type;
import com.Oracle.TelegramService.data.tasks.UserTaskCompletedReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaskIntegrationServiceTest {

    @Mock
    private TaskServiceClient taskServiceClient;

    @Mock
    private SessionCache sessionCache;

    @InjectMocks
    private TaskIntegrationService taskIntegrationService;

    private final Long chatId = 12345L;

    @Test
    void testHandleCreateTask_userNotLoggedIn() {
        Mockito.when(sessionCache.getToken(chatId)).thenReturn(null);

        SendMessage result = taskIntegrationService.handleCreateTask(chatId, new String[]{});

        assertEquals("Please login first.", result.getText());
    }

    @Test
    void testHandleCreateTask_success() {
        String token = "token123";
        Mockito.when(sessionCache.getToken(chatId)).thenReturn(token);

        TaskResponse taskResponse = new TaskResponse(
                1L, "Title", "Description", 2L,
                "High", "Done", "Bug",
                Date.valueOf("2025-06-01"),
                Date.valueOf("2025-06-05"),
                4, 5, 8
        );
        ResponseEntity<TaskResponse> response = new ResponseEntity<>(taskResponse, HttpStatus.CREATED);

        Mockito.when(taskServiceClient.createTask(Mockito.eq("Bearer " + token), Mockito.any(TaskRegister.class)))
                .thenReturn(response);

        String[] args = {
                "Title", "Description", "2", "High","Bug",
                "2025-06-01", "2025-06-05", "5", "4", "8"
        };

        SendMessage result = taskIntegrationService.handleCreateTask(chatId, args);

        // Print actual text for debugging
        System.out.println("Result text: " + result.getText());

        // Assert safely
        assertNotNull(result.getText());
        assertTrue(result.getText().contains("Task created successfully"));
        assertTrue(result.getText().contains("Title"));
    }


    @Test
    void testHandleCreateTask_invalidEnum() {
        String token = "token123";
        Mockito.when(sessionCache.getToken(chatId)).thenReturn(token);

        String[] args = {
                "Title", "Description", "1", "NOT_A_PRIORITY", "BUG",
                "2025-06-01", "2025-06-05", "5", "3", "8"
        };

        SendMessage result = taskIntegrationService.handleCreateTask(chatId, args);

        assertTrue(result.getText().startsWith("Error"));
    }

    @Test
    void testHandleShowCompletedTasksPerUserPerSprint_success() {
        String token = "token123";
        Mockito.when(sessionCache.getToken(chatId)).thenReturn(token);

        UserTaskCompletedReport.CompletedTask completedTask = new UserTaskCompletedReport.CompletedTask(
                101L, "Fix bug", Date.valueOf("2025-05-01"), 4
        );
        UserTaskCompletedReport report = new UserTaskCompletedReport(
                999L, "Alice", 888L, "Sprint Alpha", 1, List.of(completedTask)
        );

        Mockito.when(taskServiceClient.getTasksCompletedByUserPerSprint(999L))
                .thenReturn(new ResponseEntity<>(List.of(report), HttpStatus.OK));

        SendMessage result = taskIntegrationService.handleShowCompletedTasksPerUserPerSprint(chatId, new String[]{"999"});

        assertTrue(result.getText().contains("Fix bug"));
        assertTrue(result.getText().contains("*User:* Alice"));
    }

    @Test
    void testHandleShowCompletedTasksPerUserPerSprint_invalidId() {
        Mockito.when(sessionCache.getToken(chatId)).thenReturn("token");

        SendMessage result = taskIntegrationService.handleShowCompletedTasksPerUserPerSprint(chatId, new String[]{"abc"});

        assertEquals("Invalid user ID format.", result.getText());
    }

    @Test
    void testHandleShowCompletedTasksPerUserPerSprint_noToken() {
        Mockito.when(sessionCache.getToken(chatId)).thenReturn(null);

        SendMessage result = taskIntegrationService.handleShowCompletedTasksPerUserPerSprint(chatId, new String[]{"1"});

        assertEquals("Please login first.", result.getText());
    }

    @Test
    void testHandleShowCompletedTasksPerSprint_success() {
        String token = "token123";
        Mockito.when(sessionCache.getToken(chatId)).thenReturn(token);

        UserTaskCompletedReport.CompletedTask completedTask = new UserTaskCompletedReport.CompletedTask(
                200L, "Implement feature", Date.valueOf("2025-05-05"), 6
        );
        UserTaskCompletedReport report = new UserTaskCompletedReport(
                1000L, "Dev", 1001L, "Sprint Beta", 1, List.of(completedTask)
        );

        Mockito.when(taskServiceClient.getTasksCompletedPerSprint(888L))
                .thenReturn(new ResponseEntity<>(List.of(report), HttpStatus.OK));

        SendMessage result = taskIntegrationService.handleShowCompletedTasksPerSprint(chatId, new String[]{"888"});

        assertTrue(result.getText().contains("Implement feature"));
        assertTrue(result.getText().contains("📊 *Tasks Completed in Sprint 888*"));
    }

    @Test
    void testHandleShowCompletedTasksPerSprint_noToken() {
        Mockito.when(sessionCache.getToken(chatId)).thenReturn(null);

        SendMessage result = taskIntegrationService.handleShowCompletedTasksPerSprint(chatId, new String[]{"888"});

        assertEquals("Please login first.", result.getText());
    }

    @Test
    void testHandleShowCompletedTasksPerSprint_invalidId() {
        Mockito.when(sessionCache.getToken(chatId)).thenReturn("token");

        SendMessage result = taskIntegrationService.handleShowCompletedTasksPerSprint(chatId, new String[]{"oops"});

        assertEquals("Invalid sprint ID format.", result.getText());
    }


}