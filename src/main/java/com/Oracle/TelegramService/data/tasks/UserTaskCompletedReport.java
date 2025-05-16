package com.Oracle.TelegramService.data.tasks;



import java.util.Date;
import java.util.List;


public class UserTaskCompletedReport {
    private Long userId;
    private String userName;
    private Long sprintId;
    private String sprintName;
    private Integer totalTasksCompleted;
    private List<CompletedTask> completedTasks;

    public UserTaskCompletedReport() {}

    public UserTaskCompletedReport(Long userId, String userName, Long sprintId, String sprintName, Integer totalTasksCompleted, List<CompletedTask> completedTasks) {
        this.userId = userId;
        this.userName = userName;
        this.sprintId = sprintId;
        this.sprintName = sprintName;
        this.totalTasksCompleted = totalTasksCompleted;
        this.completedTasks = completedTasks;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public Integer getTotalTasksCompleted() {
        return totalTasksCompleted;
    }

    public void setTotalTasksCompleted(Integer totalTasksCompleted) {
        this.totalTasksCompleted = totalTasksCompleted;
    }

    public List<CompletedTask> getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(List<CompletedTask> completedTasks) {
        this.completedTasks = completedTasks;
    }

    public static class CompletedTask {
        private Long taskId;
        private String taskTitle;
        private Date completionDate;
        private Integer realHours;

        public CompletedTask() {}

        public CompletedTask(Long taskId, String taskTitle, Date completionDate, Integer realHours){
            this.taskId = taskId;
            this.taskTitle = taskTitle;
            this.completionDate = completionDate;
            this.realHours = realHours;
        }

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public String getTaskTitle() {
            return taskTitle;
        }

        public void setTaskTitle(String taskTitle) {
            this.taskTitle = taskTitle;
        }

        public Date getCompletionDate() {
            return completionDate;
        }

        public void setCompletionDate(Date completionDate) {
            this.completionDate = completionDate;
        }

        public Integer getRealHours() {
            return realHours;
        }

        public void setRealHours(Integer realHours) {
            this.realHours = realHours;
        }
    }
}