package com.Oracle.TelegramService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

@Service
public class TelegramCommandService {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TaskIntegrationService taskIntegrationService;

    @Autowired
    private AuthIntegrationService authIntegrationService;

    @Autowired
    private SessionCache sessionCache;

    public SendMessage processCommand(Long chatId, String command) {
        String[] args = command.split(" ");
        String baseCommand = args[0];

        // Comandos que no requieren autenticación
        switch (baseCommand) {
            case "/start":
                return authIntegrationService.handleStart(chatId);
            case "/login":
                return authIntegrationService.handleLogin(chatId);
        }

        // Verificar autenticación para otros comandos
        String token = sessionCache.getToken(chatId);
        if (token == null) {
            return new SendMessage(chatId.toString(), "⚠️ Debes iniciar sesión primero usando /login");
        }

        // Procesar comandos según rol
        String role = tokenService.getRole(token);
        return createResponseForRole(chatId, command, role);
    }

    private SendMessage createResponseForRole(Long chatId, String command, String role) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());

        switch(role) {
            case "Manager":
                return buildManagerResponse(message, command, chatId);
            case "Developer":
                return buildDeveloperResponse(message, command, chatId);
            default:
                message.setText("Rol no reconocido");
                return message;
        }
    }

    private SendMessage buildManagerResponse(SendMessage message, String command, Long chatId) {
        String[] args = command.split(" ");
        switch(args[0]) {
            case "/createsprint":
                return taskIntegrationService.handleCreateSprint(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/createepic":
                return taskIntegrationService.handleCreateEpic(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/createtask":
                return taskIntegrationService.handleCreateTask(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/addtasktosprint":
                return taskIntegrationService.handleAddTaskToSprint(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/assigntask":
                return taskIntegrationService.handleAssignTaskToUser(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/menu":
                return buildManagerMenu(chatId);
            case "/mytasks":
                return taskIntegrationService.handleGetMyTasks(chatId);
            case "/tasksCompletedPerSprint":
                return taskIntegrationService.handleShowCompletedTasksPerSprint(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/tasksCompletedPerUserPerSprint":
                return taskIntegrationService.handleShowCompletedTasksPerUserPerSprint(chatId, Arrays.copyOfRange(args, 1, args.length));
            default:
                message.setText("Comando no reconocido para Manager");
                return message;
        }
    }

    private SendMessage buildManagerMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("👔 Menú de Manager - Elige una opción:");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        // Fila 1
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Create Sprint");
        row1.add("Create Epic");

        // Fila 2
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Create Task");
        row2.add("Add Task to Sprint");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Assign Task");
        row3.add("Start Sprint");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("My Tasks");
        row4.add("Tasks Completed Per Sprint");

        KeyboardRow row5 = new KeyboardRow();
        row5.add("Tasks Completed Per Sprint Per User");

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        rows.add(row4);
        rows.add(row5);

        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);
        message.setReplyMarkup(keyboard);

        return message;
    }

    private SendMessage buildDeveloperResponse(SendMessage message, String command, Long chatId) {
        String[] args = command.split(" ");
        switch(args[0]) {
            case "/menu":
                message.setText("Opciones de Developer:");
                message.setReplyMarkup(createDeveloperKeyboard());
                break;
            case "/changetaskstatus":
                return taskIntegrationService.handleUpdateTaskStatus(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/mytasks":
                return taskIntegrationService.handleGetMyTasks(chatId);
            default:
                message.setText("Comando no reconocido para Developer");
        }
        return message;
    }

    private ReplyKeyboardMarkup createDeveloperKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("My Tasks");
        row1.add("Change Task Status");

        keyboard.add(row1);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }
}