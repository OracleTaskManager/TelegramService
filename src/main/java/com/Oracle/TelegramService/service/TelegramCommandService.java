package com.Oracle.TelegramService.service;

import com.Oracle.TelegramService.conversation.ConversationManager;
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
    private InteractiveTaskService interactiveTaskService;

    @Autowired
    private ConversationManager conversationManager;

    @Autowired
    private SessionCache sessionCache;

    public SendMessage processCommand(Long chatId, String command) {
        // Verificar si hay conversación activa
        if (conversationManager.hasActiveConversation(chatId)) {
            return interactiveTaskService.handleInteractiveMessage(chatId, command);
        }

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

        switch (role) {
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

        // Manejar botones del teclado interactivo
        switch (command) {
            // Sprint Management
            case "Create Sprint":
                return interactiveTaskService.startCreateSprintConversation(chatId);
            case "Update Sprint":
                return interactiveTaskService.startUpdateSprintConversation(chatId);
            case "Delete Sprint":
                return interactiveTaskService.startDeleteSprintConversation(chatId);
            case "Start Sprint":
                return interactiveTaskService.startStartSprintConversation(chatId);
            case "View Sprints":
                return taskIntegrationService.handleGetSprints(chatId, new String[]{});

            // Epic Management
            case "Create Epic":
                return interactiveTaskService.startCreateEpicConversation(chatId);
            case "Update Epic":
                return interactiveTaskService.startUpdateEpicConversation(chatId);
            case "Delete Epic":
                return interactiveTaskService.startDeleteEpicConversation(chatId);
            case "View All Epics":
                return taskIntegrationService.handleGetAllEpics(chatId);

            // Task Management
            case "Create Task":
                return interactiveTaskService.startCreateTaskConversation(chatId);
            case "Delete Task":
                return interactiveTaskService.startDeleteTaskConversation(chatId);
            case "Find Task by ID":
                return interactiveTaskService.startFindTaskByIdConversation(chatId);
            case "Find Tasks by Status":
                return interactiveTaskService.startFindTasksByStatusConversation(chatId);
            case "Find Tasks by Priority":
                return interactiveTaskService.startFindTasksByPriorityConversation(chatId);
            case "View All Tasks":
                return taskIntegrationService.handleGetAllTasks(chatId);

            // Task Sprint Operations
            case "Add Task to Sprint":
                return interactiveTaskService.startAddTaskToSprintConversation(chatId);
            case "Remove Task from Sprint":
                return interactiveTaskService.startRemoveTaskFromSprintConversation(chatId);

            // Task Assignment
            case "Assign Task":
                return interactiveTaskService.startAssignTaskConversation(chatId);
            case "Remove Task Assignment":
                return interactiveTaskService.startRemoveTaskAssignmentConversation(chatId);
            case "View Task Assignments":
                return taskIntegrationService.handleGetAllTaskAssignments(chatId);

            // Personal Tasks
            case "My Tasks":
                return taskIntegrationService.handleGetMyTasks(chatId);

            // Reports
            case "Tasks Completed Per Sprint":
                return interactiveTaskService.startCompletedTasksSprintConversation(chatId);
            case "Tasks Completed Per User":
                return interactiveTaskService.startCompletedTasksUserConversation(chatId);
            case "Sprint Hours Report":
                return interactiveTaskService.startSprintHoursReportConversation(chatId);
        }

        // Manejar comandos tradicionales (mantener compatibilidad)
        switch (args[0]) {
            // Sprint Commands
            case "/createsprint":
                if (args.length == 1) {
                    return interactiveTaskService.startCreateSprintConversation(chatId);
                }
                return taskIntegrationService.handleCreateSprint(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/updatesprint":
                if (args.length == 1) {
                    return interactiveTaskService.startUpdateSprintConversation(chatId);
                }
                return taskIntegrationService.handleUpdateSprint(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/deletesprint":
                if (args.length == 1) {
                    return interactiveTaskService.startDeleteSprintConversation(chatId);
                }
                return taskIntegrationService.handleDeleteSprint(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/getsprints":
                return taskIntegrationService.handleGetSprints(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/startsprint":
                if (args.length == 1) {
                    return interactiveTaskService.startStartSprintConversation(chatId);
                }
                // Handle direct command if ID provided

                // Epic Commands
            case "/createepic":
                if (args.length == 1) {
                    return interactiveTaskService.startCreateEpicConversation(chatId);
                }
                return taskIntegrationService.handleCreateEpic(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/updateepic":
                if (args.length == 1) {
                    return interactiveTaskService.startUpdateEpicConversation(chatId);
                }
                return taskIntegrationService.handleUpdateEpic(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/deleteepic":
                if (args.length == 1) {
                    return interactiveTaskService.startDeleteEpicConversation(chatId);
                }
                return taskIntegrationService.handleDeleteEpic(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/getallepics":
                return taskIntegrationService.handleGetAllEpics(chatId);

            // Task Commands
            case "/createtask":
                if (args.length == 1) {
                    return interactiveTaskService.startCreateTaskConversation(chatId);
                }
                return taskIntegrationService.handleCreateTask(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/updatetask":
                if (args.length == 1) {
                    return new SendMessage(chatId.toString(), "Use the interactive menu for task updates. Type /menu");
                }
                return taskIntegrationService.handleUpdateTask(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/deletetask":
                if (args.length == 1) {
                    return interactiveTaskService.startDeleteTaskConversation(chatId);
                }
                return taskIntegrationService.handleDeleteTask(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/gettask":
                if (args.length == 1) {
                    return interactiveTaskService.startFindTaskByIdConversation(chatId);
                }
                return taskIntegrationService.handleGetTaskById(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/getalltasks":
                return taskIntegrationService.handleGetAllTasks(chatId);
            case "/findtasksbystatus":
                if (args.length == 1) {
                    return interactiveTaskService.startFindTasksByStatusConversation(chatId);
                }
                return taskIntegrationService.handleFindTasksByStatus(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/findtasksbypriority":
                if (args.length == 1) {
                    return interactiveTaskService.startFindTasksByPriorityConversation(chatId);
                }
                return taskIntegrationService.handleFindTasksByPriority(chatId, Arrays.copyOfRange(args, 1, args.length));

            // Task Sprint Commands
            case "/addtasktosprint":
                if (args.length == 1) {
                    return interactiveTaskService.startAddTaskToSprintConversation(chatId);
                }
                return taskIntegrationService.handleAddTaskToSprint(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/removetaskfromsprint":
                if (args.length == 1) {
                    return interactiveTaskService.startRemoveTaskFromSprintConversation(chatId);
                }
                return taskIntegrationService.handleRemoveTaskFromSprint(chatId, Arrays.copyOfRange(args, 1, args.length));

            // Task Assignment Commands
            case "/assigntask":
                if (args.length == 1) {
                    return interactiveTaskService.startAssignTaskConversation(chatId);
                }
                return taskIntegrationService.handleAssignTaskToUser(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/removetaskassignment":
                if (args.length == 1) {
                    return interactiveTaskService.startRemoveTaskAssignmentConversation(chatId);
                }
                return taskIntegrationService.handleRemoveTaskAssignment(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/gettaskassignments":
                return taskIntegrationService.handleGetAllTaskAssignments(chatId);

            // Personal Commands
            case "/mytasks":
                return taskIntegrationService.handleGetMyTasks(chatId);

            // Report Commands
            case "/taskscompletedpersprint":
                if (args.length == 1) {
                    return interactiveTaskService.startCompletedTasksSprintConversation(chatId);
                }
                return taskIntegrationService.handleShowCompletedTasksPerSprint(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/taskscompletedperuser":
                if (args.length == 1) {
                    return interactiveTaskService.startCompletedTasksUserConversation(chatId);
                }
                return taskIntegrationService.handleShowCompletedTasksPerUserPerSprint(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/sprinthoursreport":
                if (args.length == 1) {
                    return interactiveTaskService.startSprintHoursReportConversation(chatId);
                }
                return taskIntegrationService.handleGetSprintHoursReport(chatId, Arrays.copyOfRange(args, 1, args.length));

            // Menu Commands
            case "/menu":
                return buildManagerMenu(chatId);
            case "/help":
                return buildManagerHelpMessage(chatId);

            default:
                message.setText("❌ Comando no reconocido para Manager\n\nUsa /menu para ver las opciones disponibles o /help para ver todos los comandos.");
                return message;
        }
    }

    private SendMessage buildManagerMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("👔 *Menú de Manager* - Elige una opción:\n\n" +
                "📝 Gestión completa de proyectos\n" +
                "🔗 Assignments y configuraciones\n" +
                "📊 Reportes y análisis\n\n" +
                "_Toca cualquier botón para comenzar una conversación guiada_");
        message.setParseMode("Markdown");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        // Sprint Management
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Create Sprint");
        row1.add("Update Sprint");
        row1.add("Start Sprint");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Delete Sprint");
        row2.add("View Sprints");

        // Epic Management
        KeyboardRow row3 = new KeyboardRow();
        row3.add("Create Epic");
        row3.add("Update Epic");
        row3.add("Delete Epic");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("View All Epics");

        // Task Management
        KeyboardRow row5 = new KeyboardRow();
        row5.add("Create Task");
        row5.add("Delete Task");
        row5.add("View All Tasks");

        KeyboardRow row6 = new KeyboardRow();
        row6.add("Find Task by ID");
        row6.add("Find Tasks by Status");

        KeyboardRow row7 = new KeyboardRow();
        row7.add("Find Tasks by Priority");

        // Task Operations
        KeyboardRow row8 = new KeyboardRow();
        row8.add("Add Task to Sprint");
        row8.add("Remove Task from Sprint");

        KeyboardRow row9 = new KeyboardRow();
        row9.add("Assign Task");
        row9.add("Remove Task Assignment");

        KeyboardRow row10 = new KeyboardRow();
        row10.add("View Task Assignments");
        row10.add("My Tasks");

        // Reports
        KeyboardRow row11 = new KeyboardRow();
        row11.add("Tasks Completed Per Sprint");
        row11.add("Tasks Completed Per User");

        KeyboardRow row12 = new KeyboardRow();
        row12.add("Sprint Hours Report");

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        rows.add(row4);
        rows.add(row5);
        rows.add(row6);
        rows.add(row7);
        rows.add(row8);
        rows.add(row9);
        rows.add(row10);
        rows.add(row11);
        rows.add(row12);

        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);
        message.setReplyMarkup(keyboard);

        return message;
    }

    private SendMessage buildManagerHelpMessage(Long chatId) {
        StringBuilder help = new StringBuilder();
        help.append("🤖 *Comandos disponibles para Manager:*\n\n");

        help.append("*📋 Gestión de Sprints:*\n");
        help.append("/createsprint - Crear nuevo sprint\n");
        help.append("/updatesprint - Actualizar sprint\n");
        help.append("/deletesprint - Eliminar sprint\n");
        help.append("/getsprints - Ver todos los sprints\n");
        help.append("/startsprint - Iniciar un sprint\n\n");

        help.append("*📚 Gestión de Epics:*\n");
        help.append("/createepic - Crear nuevo epic\n");
        help.append("/updateepic - Actualizar epic\n");
        help.append("/deleteepic - Eliminar epic\n");
        help.append("/getallepics - Ver todos los epics\n\n");

        help.append("*📝 Gestión de Tasks:*\n");
        help.append("/createtask - Crear nueva task\n");
        help.append("/deletetask - Eliminar task\n");
        help.append("/gettask - Buscar task por ID\n");
        help.append("/getalltasks - Ver todas las tasks\n");
        help.append("/findtasksbystatus - Buscar por estado\n");
        help.append("/findtasksbypriority - Buscar por prioridad\n\n");

        help.append("*🔗 Asignaciones:*\n");
        help.append("/assigntask - Asignar task a usuario\n");
        help.append("/removetaskassignment - Remover asignación\n");
        help.append("/addtasktosprint - Agregar task a sprint\n");
        help.append("/removetaskfromsprint - Remover task de sprint\n\n");

        help.append("*📊 Reportes:*\n");
        help.append("/taskscompletedpersprint - Tasks completadas por sprint\n");
        help.append("/taskscompletedperuser - Tasks completadas por usuario\n");
        help.append("/sprinthoursreport - Reporte de horas por sprint\n\n");

        help.append("*💡 Usa /menu para interfaz interactiva*");

        SendMessage message = new SendMessage(chatId.toString(), help.toString());
        message.setParseMode("Markdown");
        return message;
    }

    private SendMessage buildDeveloperResponse(SendMessage message, String command, Long chatId) {
        String[] args = command.split(" ");

        // Manejar botones del teclado
        switch (command) {
            case "My Tasks":
                return taskIntegrationService.handleGetMyTasks(chatId);
            case "Change Task Status":
                return interactiveTaskService.startUpdateTaskStatusConversation(chatId);
            case "Find Task by ID":
                return interactiveTaskService.startFindTaskByIdConversation(chatId);
            case "Find Tasks by Status":
                return interactiveTaskService.startFindTasksByStatusConversation(chatId);
        }

        // Manejar comandos tradicionales
        switch (args[0]) {
            case "/menu":
                return buildDeveloperMenu(chatId);
            case "/changetaskstatus":
            case "/updatetaskstatus":
                if (args.length == 1) {
                    return interactiveTaskService.startUpdateTaskStatusConversation(chatId);
                }
                return taskIntegrationService.handleUpdateTaskStatus(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/mytasks":
                return taskIntegrationService.handleGetMyTasks(chatId);
            case "/gettask":
                if (args.length == 1) {
                    return interactiveTaskService.startFindTaskByIdConversation(chatId);
                }
                return taskIntegrationService.handleGetTaskById(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/findtasksbystatus":
                if (args.length == 1) {
                    return interactiveTaskService.startFindTasksByStatusConversation(chatId);
                }
                return taskIntegrationService.handleFindTasksByStatus(chatId, Arrays.copyOfRange(args, 1, args.length));
            case "/help":
                return buildDeveloperHelpMessage(chatId);
            default:
                message.setText("❌ Comando no reconocido para Developer\n\nUsa /menu para ver las opciones disponibles o /help para ver todos los comandos.");
        }
        return message;
    }

    private SendMessage buildDeveloperMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("👨‍💻 *Menú de Developer* - Elige una opción:\n\n" +
                "📋 Ver y gestionar tus tasks\n" +
                "🔄 Cambiar estado de tasks\n" +
                "🔍 Buscar tasks específicas\n\n" +
                "_Toca cualquier botón para comenzar_");
        message.setParseMode("Markdown");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("My Tasks");
        row1.add("Change Task Status");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Find Task by ID");
        row2.add("Find Tasks by Status");

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);

        return message;
    }

    private SendMessage buildDeveloperHelpMessage(Long chatId) {
        StringBuilder help = new StringBuilder();
        help.append("🤖 *Comandos disponibles para Developer:*\n\n");

        help.append("*📋 Gestión Personal:*\n");
        help.append("/mytasks - Ver mis tasks asignadas\n");
        help.append("/changetaskstatus - Cambiar estado de task\n\n");

        help.append("*💡 Usa /menu para interfaz interactiva*");

        SendMessage message = new SendMessage(chatId.toString(), help.toString());
        message.setParseMode("Markdown");
        return message;
    }
}