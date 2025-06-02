package com.Oracle.TelegramService.service;

import com.Oracle.TelegramService.client.TaskServiceClient;
import com.Oracle.TelegramService.conversation.ConversationManager;
import com.Oracle.TelegramService.conversation.ConversationState;
import com.Oracle.TelegramService.data.tasks.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Service
public class InteractiveTaskService {

    @Autowired
    private ConversationManager conversationManager;

    @Autowired
    private TaskServiceClient taskServiceClient;

    @Autowired
    private TaskIntegrationService taskIntegrationService;

    @Autowired
    private SessionCache sessionCache;

    public SendMessage handleInteractiveMessage(Long chatId, String message) {
        ConversationState conversation = conversationManager.getConversation(chatId);

        if (conversation == null) {
            return new SendMessage(chatId.toString(), "❌ No hay conversación activa. Usa /menu para empezar.");
        }

        // Manejar comando cancelar
        if (message.equalsIgnoreCase("/cancel") || message.equalsIgnoreCase("Cancel")) {
            conversationManager.cancelConversation(chatId);
            SendMessage response = new SendMessage(chatId.toString(), "❌ Operación cancelada.");
            response.setReplyMarkup(new ReplyKeyboardRemove(true));
            return response;
        }

        switch (conversation.getAction()) {
            case "CREATE_SPRINT":
                return handleCreateSprintConversation(chatId, message, conversation);
            case "CREATE_EPIC":
                return handleCreateEpicConversation(chatId, message, conversation);
            case "CREATE_TASK":
                return handleCreateTaskConversation(chatId, message, conversation);
            case "ADD_TASK_TO_SPRINT":
                return handleAddTaskToSprintConversation(chatId, message, conversation);
            case "ASSIGN_TASK":
                return handleAssignTaskConversation(chatId, message, conversation);
            case "UPDATE_TASK_STATUS":
                return handleUpdateTaskStatusConversation(chatId, message, conversation);
            case "START_SPRINT":
                return handleStartSprintConversation(chatId, message, conversation);
            case "GET_COMPLETED_TASKS_SPRINT":
                return handleCompletedTasksSprintConversation(chatId, message, conversation);
            case "GET_COMPLETED_TASKS_USER":
                return handleCompletedTasksUserConversation(chatId, message, conversation);
            default:
                conversationManager.endConversation(chatId);
                return new SendMessage(chatId.toString(), "❌ Acción no reconocida.");
        }
    }

    // ========== CREATE SPRINT CONVERSATION ==========
    public SendMessage startCreateSprintConversation(Long chatId) {
        conversationManager.startConversation(chatId, "CREATE_SPRINT");
        return new SendMessage(chatId.toString(),
                "🏃‍♂️ *Crear nuevo Sprint*\n\n" +
                        "Paso 1/3: ¿Cuál será el nombre del sprint?\n\n" +
                        "_Ejemplo: Sprint 2024-Q1_\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleCreateSprintConversation(Long chatId, String message, ConversationState conversation) {
        switch (conversation.getStepIndex()) {
            case 0: // Nombre del sprint
                conversation.addData("name", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "📅 Paso 2/3: ¿Cuál es la fecha de inicio?\n\n" +
                                "_Formato: YYYY-MM-DD (ejemplo: 2024-06-01)_");

            case 1: // Fecha inicio
                if (!isValidDate(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ Fecha inválida. Usa el formato YYYY-MM-DD\n" +
                                    "_Ejemplo: 2024-06-01_");
                }
                conversation.addData("startDate", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "📅 Paso 3/3: ¿Cuál es la fecha de fin?\n\n" +
                                "_Formato: YYYY-MM-DD (ejemplo: 2024-06-30)_");

            case 2: // Fecha fin
                if (!isValidDate(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ Fecha inválida. Usa el formato YYYY-MM-DD\n" +
                                    "_Ejemplo: 2024-06-30_");
                }
                conversation.addData("endDate", message);

                // Crear el sprint
                String[] args = {
                        (String) conversation.getData("name"),
                        (String) conversation.getData("startDate"),
                        (String) conversation.getData("endDate")
                };

                conversationManager.endConversation(chatId);
                return taskIntegrationService.handleCreateSprint(chatId, args);

            default:
                conversationManager.endConversation(chatId);
                return new SendMessage(chatId.toString(), "❌ Error en la conversación.");
        }
    }

    // ========== CREATE EPIC CONVERSATION ==========
    public SendMessage startCreateEpicConversation(Long chatId) {
        conversationManager.startConversation(chatId, "CREATE_EPIC");
        return new SendMessage(chatId.toString(),
                "📋 *Crear nuevo Epic*\n\n" +
                        "Paso 1/2: ¿Cuál será el título del epic?\n\n" +
                        "_Ejemplo: Sistema de autenticación_\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleCreateEpicConversation(Long chatId, String message, ConversationState conversation) {
        switch (conversation.getStepIndex()) {
            case 0: // Título
                conversation.addData("title", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "📝 Paso 2/2: ¿Cuál es la descripción del epic?\n\n" +
                                "_Describe qué incluirá este epic_");

            case 1: // Descripción
                conversation.addData("description", message);

                String[] args = {
                        (String) conversation.getData("title"),
                        (String) conversation.getData("description")
                };

                conversationManager.endConversation(chatId);
                return taskIntegrationService.handleCreateEpic(chatId, args);

            default:
                conversationManager.endConversation(chatId);
                return new SendMessage(chatId.toString(), "❌ Error en la conversación.");
        }
    }

    // ========== CREATE TASK CONVERSATION ==========
    public SendMessage startCreateTaskConversation(Long chatId) {
        conversationManager.startConversation(chatId, "CREATE_TASK");
        return new SendMessage(chatId.toString(),
                "📝 *Crear nueva Task*\n\n" +
                        "Paso 1/9: ¿Cuál será el título de la task?\n\n" +
                        "_Ejemplo: Implementar login con JWT_\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleCreateTaskConversation(Long chatId, String message, ConversationState conversation) {
        switch (conversation.getStepIndex()) {
            case 0: // Título
                conversation.addData("title", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "📝 Paso 2/9: ¿Cuál es la descripción de la task?");

            case 1: // Descripción
                conversation.addData("description", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "🏷️ Paso 3/9: ¿Cuál es el ID del epic al que pertenece?\n\n" +
                                "_Número entero, ejemplo: 1_");

            case 2: // Epic ID
                if (!isValidLong(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ ID inválido. Debe ser un número entero.\n_Ejemplo: 1_");
                }
                conversation.addData("epicId", message);
                conversation.nextStep();

                SendMessage priorityMsg = new SendMessage(chatId.toString(),
                        "⚡ Paso 4/9: ¿Cuál es la prioridad?");
                priorityMsg.setReplyMarkup(createPriorityKeyboard());
                return priorityMsg;

            case 3: // Prioridad
                if (!isValidPriority(message)) {
                    SendMessage errorMsg = new SendMessage(chatId.toString(),
                            "❌ Prioridad inválida. Selecciona una opción:");
                    errorMsg.setReplyMarkup(createPriorityKeyboard());
                    return errorMsg;
                }
                conversation.addData("priority", message);
                conversation.nextStep();

                SendMessage typeMsg = new SendMessage(chatId.toString(),
                        "🔧 Paso 5/9: ¿Cuál es el tipo de task?");
                typeMsg.setReplyMarkup(createTypeKeyboard());
                return typeMsg;

            case 4: // Tipo
                if (!isValidType(message)) {
                    SendMessage errorMsg = new SendMessage(chatId.toString(),
                            "❌ Tipo inválido. Selecciona una opción:");
                    errorMsg.setReplyMarkup(createTypeKeyboard());
                    return errorMsg;
                }
                conversation.addData("type", message);
                conversation.nextStep();

                SendMessage deadlineMsg = new SendMessage(chatId.toString(),
                        "📅 Paso 6/9: ¿Cuál es el deadline estimado?\n\n" +
                                "_Formato: YYYY-MM-DD_");
                deadlineMsg.setReplyMarkup(new ReplyKeyboardRemove(true));
                return deadlineMsg;

            case 5: // Deadline estimado
                if (!isValidDate(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ Fecha inválida. Usa formato YYYY-MM-DD");
                }
                conversation.addData("estimatedDeadline", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "📅 Paso 7/9: ¿Cuál es el deadline real?\n\n" +
                                "_Formato: YYYY-MM-DD_");

            case 6: // Deadline real
                if (!isValidDate(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ Fecha inválida. Usa formato YYYY-MM-DD");
                }
                conversation.addData("realDeadline", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "⏱️ Paso 8/9: ¿Cuántas horas estimadas?\n\n" +
                                "_Número entero, ejemplo: 8_");

            case 7: // Horas estimadas
                if (!isValidInteger(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ Número inválido. Ingresa un número entero.");
                }
                conversation.addData("estimatedHours", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "🎯 Paso 9/9: ¿Cuántos story points?\n\n" +
                                "_Número entero, ejemplo: 5_");

            case 8: // Story points
                if (!isValidInteger(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ Número inválido. Ingresa un número entero.");
                }
                conversation.addData("storyPoints", message);

                // Crear la task con todos los datos
                String[] args = {
                        (String) conversation.getData("title"),
                        (String) conversation.getData("description"),
                        (String) conversation.getData("epicId"),
                        (String) conversation.getData("priority"),
                        (String) conversation.getData("type"),
                        (String) conversation.getData("estimatedDeadline"),
                        (String) conversation.getData("realDeadline"),
                        (String) conversation.getData("estimatedHours"),
                        "0", // horas reales inicialmente 0
                        (String) conversation.getData("storyPoints")
                };

                conversationManager.endConversation(chatId);
                return taskIntegrationService.handleCreateTask(chatId, args);

            default:
                conversationManager.endConversation(chatId);
                return new SendMessage(chatId.toString(), "❌ Error en la conversación.");
        }
    }

    // ========== OTRAS CONVERSACIONES SIMPLES ==========

    public SendMessage startAddTaskToSprintConversation(Long chatId) {
        conversationManager.startConversation(chatId, "ADD_TASK_TO_SPRINT");
        return new SendMessage(chatId.toString(),
                "🔗 *Agregar Task a Sprint*\n\n" +
                        "Paso 1/2: ¿Cuál es el ID de la task?\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleAddTaskToSprintConversation(Long chatId, String message, ConversationState conversation) {
        switch (conversation.getStepIndex()) {
            case 0: // Task ID
                if (!isValidLong(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ ID inválido. Debe ser un número entero.");
                }
                conversation.addData("taskId", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "🏃‍♂️ Paso 2/2: ¿Cuál es el ID del sprint?");

            case 1: // Sprint ID
                if (!isValidLong(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ ID inválido. Debe ser un número entero.");
                }
                conversation.addData("sprintId", message);

                String[] args = {
                        (String) conversation.getData("taskId"),
                        (String) conversation.getData("sprintId")
                };

                conversationManager.endConversation(chatId);
                return taskIntegrationService.handleAddTaskToSprint(chatId, args);

            default:
                conversationManager.endConversation(chatId);
                return new SendMessage(chatId.toString(), "❌ Error en la conversación.");
        }
    }

    public SendMessage startAssignTaskConversation(Long chatId) {
        conversationManager.startConversation(chatId, "ASSIGN_TASK");
        return new SendMessage(chatId.toString(),
                "👤 *Asignar Task*\n\n" +
                        "Paso 1/2: ¿Cuál es el ID de la task?\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleAssignTaskConversation(Long chatId, String message, ConversationState conversation) {
        switch (conversation.getStepIndex()) {
            case 0: // Task ID
                if (!isValidLong(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ ID inválido. Debe ser un número entero.");
                }
                conversation.addData("taskId", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "👤 Paso 2/2: ¿Cuál es el ID del usuario?");

            case 1: // User ID
                if (!isValidLong(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ ID inválido. Debe ser un número entero.");
                }
                conversation.addData("userId", message);

                String[] args = {
                        (String) conversation.getData("taskId"),
                        (String) conversation.getData("userId")
                };

                conversationManager.endConversation(chatId);
                return taskIntegrationService.handleAssignTaskToUser(chatId, args);

            default:
                conversationManager.endConversation(chatId);
                return new SendMessage(chatId.toString(), "❌ Error en la conversación.");
        }
    }

    public SendMessage startUpdateTaskStatusConversation(Long chatId) {
        conversationManager.startConversation(chatId, "UPDATE_TASK_STATUS");
        return new SendMessage(chatId.toString(),
                "🔄 *Cambiar Estado de Task*\n\n" +
                        "Paso 1/2: ¿Cuál es el ID de la task?\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleUpdateTaskStatusConversation(Long chatId, String message, ConversationState conversation) {
        switch (conversation.getStepIndex()) {
            case 0: // Task ID
                if (!isValidLong(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ ID inválido. Debe ser un número entero.");
                }
                conversation.addData("taskId", message);
                conversation.nextStep();

                SendMessage statusMsg = new SendMessage(chatId.toString(),
                        "📊 Paso 2/2: ¿Cuál es el nuevo estado?");
                statusMsg.setReplyMarkup(createStatusKeyboard());
                return statusMsg;

            case 1: // Estado
                conversation.addData("status", message);

                String[] args = {
                        (String) conversation.getData("taskId"),
                        (String) conversation.getData("status")
                };

                conversationManager.endConversation(chatId);
                SendMessage response = taskIntegrationService.handleUpdateTaskStatus(chatId, args);
                response.setReplyMarkup(new ReplyKeyboardRemove(true));
                return response;

            default:
                conversationManager.endConversation(chatId);
                return new SendMessage(chatId.toString(), "❌ Error en la conversación.");
        }
    }

    public SendMessage startCompletedTasksSprintConversation(Long chatId) {
        conversationManager.startConversation(chatId, "GET_COMPLETED_TASKS_SPRINT");
        return new SendMessage(chatId.toString(),
                "📊 *Tasks Completadas por Sprint*\n\n" +
                        "¿Cuál es el ID del sprint?\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleCompletedTasksSprintConversation(Long chatId, String message, ConversationState conversation) {
        if (!isValidLong(message)) {
            return new SendMessage(chatId.toString(),
                    "❌ ID inválido. Debe ser un número entero.");
        }

        String[] args = { message };
        conversationManager.endConversation(chatId);
        return taskIntegrationService.handleShowCompletedTasksPerSprint(chatId, args);
    }

    public SendMessage startStartSprintConversation(Long chatId) {
        conversationManager.startConversation(chatId, "START_SPRINT");
        return new SendMessage(chatId.toString(),
                "🚀 *Iniciar Sprint*\n\n" +
                        "¿Cuál es el ID del sprint que quieres iniciar?\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleStartSprintConversation(Long chatId, String message, ConversationState conversation) {
        if (!isValidLong(message)) {
            return new SendMessage(chatId.toString(),
                    "❌ ID inválido. Debe ser un número entero.");
        }

        String token = sessionCache.getToken(chatId);
        if (token == null) {
            conversationManager.endConversation(chatId);
            return new SendMessage(chatId.toString(), "Please login first.");
        }

        try {
            Long sprintId = Long.parseLong(message);
            ResponseEntity<Void> response = taskServiceClient.startSprint(sprintId, "Bearer " + token);

            conversationManager.endConversation(chatId);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new SendMessage(chatId.toString(), "✅ Sprint iniciado exitosamente!");
            } else {
                return new SendMessage(chatId.toString(), "❌ Error al iniciar sprint: " + response.getStatusCode());
            }
        } catch (Exception e) {
            conversationManager.endConversation(chatId);
            return new SendMessage(chatId.toString(), "❌ Error: " + e.getMessage());
        }
    }

    private SendMessage handleCompletedTasksUserConversation(Long chatId, String message, ConversationState conversation) {
        if (!isValidLong(message)) {
            return new SendMessage(chatId.toString(),
                    "❌ ID inválido. Debe ser un número entero.");
        }

        String[] args = { message };
        conversationManager.endConversation(chatId);
        return taskIntegrationService.handleShowCompletedTasksPerUserPerSprint(chatId, args);
    }

    // ========== KEYBOARDS ==========

    private ReplyKeyboardMarkup createPriorityKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("LOW");
        row1.add("MEDIUM");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("HIGH");
        row2.add("CRITICAL");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Cancel");

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        return keyboard;
    }

    private ReplyKeyboardMarkup createTypeKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("STORY");
        row1.add("BUG");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("TASK");
        row2.add("EPIC");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Cancel");

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        return keyboard;
    }

    private ReplyKeyboardMarkup createStatusKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("TODO");
        row1.add("IN_PROGRESS");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("DONE");
        row2.add("BLOCKED");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Cancel");

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        return keyboard;
    }

    // ========== VALIDATION METHODS ==========

    private boolean isValidDate(String date) {
        try {
            Date.valueOf(date);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isValidLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidPriority(String priority) {
        return Arrays.asList("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(priority);
    }

    private boolean isValidType(String type) {
        return Arrays.asList("STORY", "BUG", "TASK", "EPIC").contains(type);
    }
}