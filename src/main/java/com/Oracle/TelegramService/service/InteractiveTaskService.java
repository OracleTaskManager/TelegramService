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
            case "UPDATE_SPRINT":
                return handleUpdateSprintConversation(chatId, message, conversation);
            case "DELETE_SPRINT":
                return handleDeleteSprintConversation(chatId, message, conversation);
            case "CREATE_EPIC":
                return handleCreateEpicConversation(chatId, message, conversation);
            case "UPDATE_EPIC":
                return handleUpdateEpicConversation(chatId, message, conversation);
            case "DELETE_EPIC":
                return handleDeleteEpicConversation(chatId, message, conversation);
            case "CREATE_TASK":
                return handleCreateTaskConversation(chatId, message, conversation);
//            case "UPDATE_TASK":
//                return handleUpdateTaskConversation(chatId, message, conversation);
            case "DELETE_TASK":
                return handleDeleteTaskConversation(chatId, message, conversation);
            case "ADD_TASK_TO_SPRINT":
                return handleAddTaskToSprintConversation(chatId, message, conversation);
            case "REMOVE_TASK_FROM_SPRINT":
                return handleRemoveTaskFromSprintConversation(chatId, message, conversation);
            case "ASSIGN_TASK":
                return handleAssignTaskConversation(chatId, message, conversation);
            case "REMOVE_TASK_ASSIGNMENT":
                return handleRemoveTaskAssignmentConversation(chatId, message, conversation);
            case "UPDATE_TASK_STATUS":
                return handleUpdateTaskStatusConversation(chatId, message, conversation);
            case "START_SPRINT":
                return handleStartSprintConversation(chatId, message, conversation);
            case "GET_COMPLETED_TASKS_SPRINT":
                return handleCompletedTasksSprintConversation(chatId, message, conversation);
            case "GET_COMPLETED_TASKS_USER":
                return handleCompletedTasksUserConversation(chatId, message, conversation);
            case "GET_SPRINT_HOURS_REPORT":
                return handleSprintHoursReportConversation(chatId, message, conversation);
            case "FIND_TASK_BY_ID":
                return handleFindTaskByIdConversation(chatId, message, conversation);
            case "FIND_TASKS_BY_STATUS":
                return handleFindTasksByStatusConversation(chatId, message, conversation);
            case "FIND_TASKS_BY_PRIORITY":
                return handleFindTasksByPriorityConversation(chatId, message, conversation);
            default:
                conversationManager.endConversation(chatId);
                return new SendMessage(chatId.toString(), "❌ Acción no reconocida.");
        }
    }

    // ========== SPRINT CONVERSATIONS ==========

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

    public SendMessage startUpdateSprintConversation(Long chatId) {
        conversationManager.startConversation(chatId, "UPDATE_SPRINT");
        return new SendMessage(chatId.toString(),
                "🔄 *Actualizar Sprint*\n\n" +
                        "Paso 1/4: ¿Cuál es el ID del sprint a actualizar?\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleUpdateSprintConversation(Long chatId, String message, ConversationState conversation) {
        switch (conversation.getStepIndex()) {
            case 0: // Sprint ID
                if (!isValidLong(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ ID inválido. Debe ser un número entero.");
                }
                conversation.addData("sprintId", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "📝 Paso 2/4: ¿Cuál será el nuevo nombre del sprint?");

            case 1: // Nombre
                conversation.addData("name", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "📅 Paso 3/4: ¿Cuál es la nueva fecha de inicio?\n" +
                                "_Formato: YYYY-MM-DD_");

            case 2: // Fecha inicio
                if (!isValidDate(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ Fecha inválida. Usa el formato YYYY-MM-DD");
                }
                conversation.addData("startDate", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "📅 Paso 4/4: ¿Cuál es la nueva fecha de fin?\n" +
                                "_Formato: YYYY-MM-DD_");

            case 3: // Fecha fin
                if (!isValidDate(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ Fecha inválida. Usa el formato YYYY-MM-DD");
                }
                conversation.addData("endDate", message);

                String[] args = {
                        (String) conversation.getData("sprintId"),
                        (String) conversation.getData("name"),
                        (String) conversation.getData("startDate"),
                        (String) conversation.getData("endDate")
                };

                conversationManager.endConversation(chatId);
                return taskIntegrationService.handleUpdateSprint(chatId, args);

            default:
                conversationManager.endConversation(chatId);
                return new SendMessage(chatId.toString(), "❌ Error en la conversación.");
        }
    }

    public SendMessage startDeleteSprintConversation(Long chatId) {
        conversationManager.startConversation(chatId, "DELETE_SPRINT");
        return new SendMessage(chatId.toString(),
                "🗑️ *Eliminar Sprint*\n\n" +
                        "¿Cuál es el ID del sprint que deseas eliminar?\n\n" +
                        "⚠️ *Esta acción no se puede deshacer*\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleDeleteSprintConversation(Long chatId, String message, ConversationState conversation) {
        if (!isValidLong(message)) {
            return new SendMessage(chatId.toString(),
                    "❌ ID inválido. Debe ser un número entero.");
        }

        String[] args = { message };
        conversationManager.endConversation(chatId);
        return taskIntegrationService.handleDeleteSprint(chatId, args);
    }

    // ========== EPIC CONVERSATIONS ==========

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

    public SendMessage startUpdateEpicConversation(Long chatId) {
        conversationManager.startConversation(chatId, "UPDATE_EPIC");
        return new SendMessage(chatId.toString(),
                "🔄 *Actualizar Epic*\n\n" +
                        "Paso 1/4: ¿Cuál es el ID del epic a actualizar?\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleUpdateEpicConversation(Long chatId, String message, ConversationState conversation) {
        switch (conversation.getStepIndex()) {
            case 0: // Epic ID
                if (!isValidLong(message)) {
                    return new SendMessage(chatId.toString(),
                            "❌ ID inválido. Debe ser un número entero.");
                }
                conversation.addData("epicId", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "📝 Paso 2/4: ¿Cuál será el nuevo título?");

            case 1: // Título
                conversation.addData("title", message);
                conversation.nextStep();
                return new SendMessage(chatId.toString(),
                        "📝 Paso 3/4: ¿Cuál será la nueva descripción?");

            case 2: // Descripción
                conversation.addData("description", message);
                conversation.nextStep();

                SendMessage statusMsg = new SendMessage(chatId.toString(),
                        "📊 Paso 4/4: ¿Cuál es el estado del epic?");
                statusMsg.setReplyMarkup(createEpicStatusKeyboard());
                return statusMsg;

            case 3: // Estado
                if (!isValidEpicStatus(message)) {
                    SendMessage errorMsg = new SendMessage(chatId.toString(),
                            "❌ Estado inválido. Selecciona una opción:");
                    errorMsg.setReplyMarkup(createEpicStatusKeyboard());
                    return errorMsg;
                }
                conversation.addData("status", message);

                String[] args = {
                        (String) conversation.getData("epicId"),
                        (String) conversation.getData("title"),
                        (String) conversation.getData("description"),
                        (String) conversation.getData("status")
                };

                conversationManager.endConversation(chatId);
                SendMessage response = taskIntegrationService.handleUpdateEpic(chatId, args);
                response.setReplyMarkup(new ReplyKeyboardRemove(true));
                return response;

            default:
                conversationManager.endConversation(chatId);
                return new SendMessage(chatId.toString(), "❌ Error en la conversación.");
        }
    }

    public SendMessage startDeleteEpicConversation(Long chatId) {
        conversationManager.startConversation(chatId, "DELETE_EPIC");
        return new SendMessage(chatId.toString(),
                "🗑️ *Eliminar Epic*\n\n" +
                        "¿Cuál es el ID del epic que deseas eliminar?\n\n" +
                        "⚠️ *Esta acción no se puede deshacer*\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleDeleteEpicConversation(Long chatId, String message, ConversationState conversation) {
        if (!isValidLong(message)) {
            return new SendMessage(chatId.toString(),
                    "❌ ID inválido. Debe ser un número entero.");
        }

        String[] args = { message };
        conversationManager.endConversation(chatId);
        return taskIntegrationService.handleDeleteEpic(chatId, args);
    }

    // ========== TASK CONVERSATIONS ==========

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

                String[] args = {
                        (String) conversation.getData("title"),
                        (String) conversation.getData("description"),
                        (String) conversation.getData("epicId"),
                        (String) conversation.getData("priority"),
                        (String) conversation.getData("type"),
                        (String) conversation.getData("estimatedDeadline"),
                        (String) conversation.getData("realDeadline"),
                        (String) conversation.getData("storyPoints"),
                        (String) conversation.getData("estimatedHours")
                };

                conversationManager.endConversation(chatId);
                return taskIntegrationService.handleCreateTask(chatId, args);

            default:
                conversationManager.endConversation(chatId);
                return new SendMessage(chatId.toString(), "❌ Error en la conversación.");
        }
    }

    public SendMessage startDeleteTaskConversation(Long chatId) {
        conversationManager.startConversation(chatId, "DELETE_TASK");
        return new SendMessage(chatId.toString(),
                "🗑️ *Eliminar Task*\n\n" +
                        "¿Cuál es el ID de la task que deseas eliminar?\n\n" +
                        "⚠️ *Esta acción no se puede deshacer*\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleDeleteTaskConversation(Long chatId, String message, ConversationState conversation) {
        if (!isValidLong(message)) {
            return new SendMessage(chatId.toString(),
                    "❌ ID inválido. Debe ser un número entero.");
        }

        String[] args = { message };
        conversationManager.endConversation(chatId);
        return taskIntegrationService.handleDeleteTask(chatId, args);
    }

    // ========== TASK SPRINT CONVERSATIONS ==========

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

    public SendMessage startRemoveTaskFromSprintConversation(Long chatId) {
        conversationManager.startConversation(chatId, "REMOVE_TASK_FROM_SPRINT");
        return new SendMessage(chatId.toString(),
                "🔗 *Remover Task de Sprint*\n\n" +
                        "Paso 1/2: ¿Cuál es el ID de la task?\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleRemoveTaskFromSprintConversation(Long chatId, String message, ConversationState conversation) {
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
                return taskIntegrationService.handleRemoveTaskFromSprint(chatId, args);

            default:
                conversationManager.endConversation(chatId);
                return new SendMessage(chatId.toString(), "❌ Error en la conversación.");
        }
    }

    // ========== TASK ASSIGNMENT CONVERSATIONS ==========

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

    public SendMessage startRemoveTaskAssignmentConversation(Long chatId) {
        conversationManager.startConversation(chatId, "REMOVE_TASK_ASSIGNMENT");
        return new SendMessage(chatId.toString(),
                "❌ *Remover Asignación de Task*\n\n" +
                        "Paso 1/2: ¿Cuál es el ID de la task?\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleRemoveTaskAssignmentConversation(Long chatId, String message, ConversationState conversation) {
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
                return taskIntegrationService.handleRemoveTaskAssignment(chatId, args);

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

    // ========== SEARCH CONVERSATIONS ==========

    public SendMessage startFindTaskByIdConversation(Long chatId) {
        conversationManager.startConversation(chatId, "FIND_TASK_BY_ID");
        return new SendMessage(chatId.toString(),
                "🔍 *Buscar Task por ID*\n\n" +
                        "¿Cuál es el ID de la task?\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleFindTaskByIdConversation(Long chatId, String message, ConversationState conversation) {
        if (!isValidLong(message)) {
            return new SendMessage(chatId.toString(),
                    "❌ ID inválido. Debe ser un número entero.");
        }

        String[] args = { message };
        conversationManager.endConversation(chatId);
        return taskIntegrationService.handleGetTaskById(chatId, args);
    }

    public SendMessage startFindTasksByStatusConversation(Long chatId) {
        conversationManager.startConversation(chatId, "FIND_TASKS_BY_STATUS");

        SendMessage msg = new SendMessage(chatId.toString(),
                "🔍 *Buscar Tasks por Estado*\n\n" +
                        "¿Cuál es el estado que deseas buscar?\n\n" +
                        "Envía /cancel para cancelar.");
        msg.setReplyMarkup(createStatusKeyboard());
        return msg;
    }

    private SendMessage handleFindTasksByStatusConversation(Long chatId, String message, ConversationState conversation) {
        String[] args = { message };
        conversationManager.endConversation(chatId);
        SendMessage response = taskIntegrationService.handleFindTasksByStatus(chatId, args);
        response.setReplyMarkup(new ReplyKeyboardRemove(true));
        return response;
    }

    public SendMessage startFindTasksByPriorityConversation(Long chatId) {
        conversationManager.startConversation(chatId, "FIND_TASKS_BY_PRIORITY");

        SendMessage msg = new SendMessage(chatId.toString(),
                "🔍 *Buscar Tasks por Prioridad*\n\n" +
                        "¿Cuál es la prioridad que deseas buscar?\n\n" +
                        "Envía /cancel para cancelar.");
        msg.setReplyMarkup(createPriorityKeyboard());
        return msg;
    }

    private SendMessage handleFindTasksByPriorityConversation(Long chatId, String message, ConversationState conversation) {
        String[] args = { message };
        conversationManager.endConversation(chatId);
        SendMessage response = taskIntegrationService.handleFindTasksByPriority(chatId, args);
        response.setReplyMarkup(new ReplyKeyboardRemove(true));
        return response;
    }

    // ========== REPORT CONVERSATIONS ==========

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

    public SendMessage startCompletedTasksUserConversation(Long chatId) {
        conversationManager.startConversation(chatId, "GET_COMPLETED_TASKS_USER");
        return new SendMessage(chatId.toString(),
                "📊 *Tasks Completadas por Usuario*\n\n" +
                        "¿Cuál es el ID del usuario?\n\n" +
                        "Envía /cancel para cancelar.");
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

    public SendMessage startSprintHoursReportConversation(Long chatId) {
        conversationManager.startConversation(chatId, "GET_SPRINT_HOURS_REPORT");
        return new SendMessage(chatId.toString(),
                "📊 *Reporte de Horas por Sprint*\n\n" +
                        "¿Cuál es el ID del sprint?\n\n" +
                        "Envía /cancel para cancelar.");
    }

    private SendMessage handleSprintHoursReportConversation(Long chatId, String message, ConversationState conversation) {
        if (!isValidLong(message)) {
            return new SendMessage(chatId.toString(),
                    "❌ ID inválido. Debe ser un número entero.");
        }

        String[] args = { message };
        conversationManager.endConversation(chatId);
        return taskIntegrationService.handleGetSprintHoursReport(chatId, args);
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
        row1.add("Backlog");
        row1.add("ToDo");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("InProgress");
        row2.add("Done");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Blocked");
        row3.add("Cancel");

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        return keyboard;
    }

    private ReplyKeyboardMarkup createEpicStatusKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("ToDo");
        row1.add("InProgress");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Done");
        row2.add("Cancel");

        rows.add(row1);
        rows.add(row2);

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

    private boolean isValidEpicStatus(String status) {
        return Arrays.asList("ToDo", "InProgress", "Done").contains(status);
    }
}