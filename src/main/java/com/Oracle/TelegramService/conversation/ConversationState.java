package com.Oracle.TelegramService.conversation;

import java.util.HashMap;
import java.util.Map;

public class ConversationState {
    private String currentStep;
    private String action;
    private Map<String, Object> data;
    private int stepIndex;

    public ConversationState(String action) {
        this.action = action;
        this.data = new HashMap<>();
        this.stepIndex = 0;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public String getAction() {
        return action;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void addData(String key, Object value) {
        this.data.put(key, value);
    }

    public Object getData(String key) {
        return this.data.get(key);
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void nextStep() {
        this.stepIndex++;
    }

    public void reset() {
        this.currentStep = null;
        this.action = null;
        this.data.clear();
        this.stepIndex = 0;
    }

}
