package com.mit.bodhiq.chatbot;

import com.mit.bodhiq.data.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains conversation context for better responses
 */
public class ConversationContext {
    
    private UserProfile userProfile;
    private String lastReport;
    private List<String> conversationHistory;
    private String lastTopic;
    
    public ConversationContext() {
        this.conversationHistory = new ArrayList<>();
    }
    
    public void setUserProfile(UserProfile profile) {
        this.userProfile = profile;
    }
    
    public UserProfile getUserProfile() {
        return userProfile;
    }
    
    public void setLastReport(String report) {
        this.lastReport = report;
    }
    
    public String getLastReport() {
        return lastReport;
    }
    
    public void addUserMessage(String message) {
        conversationHistory.add("User: " + message);
        if (conversationHistory.size() > 10) {
            conversationHistory.remove(0);
        }
    }
    
    public void addBotMessage(String message) {
        conversationHistory.add("Bot: " + message);
        if (conversationHistory.size() > 10) {
            conversationHistory.remove(0);
        }
    }
    
    public List<String> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }
    
    public void setLastTopic(String topic) {
        this.lastTopic = topic;
    }
    
    public String getLastTopic() {
        return lastTopic;
    }
    
    public void clear() {
        conversationHistory.clear();
        lastTopic = null;
    }
    
    /**
     * Get recent messages as a formatted string for context
     * @param count Number of recent messages to include
     * @return Formatted string of recent messages
     */
    public String getRecentMessagesAsString(int count) {
        if (conversationHistory.isEmpty()) {
            return "";
        }
        
        int startIndex = Math.max(0, conversationHistory.size() - count);
        List<String> recentMessages = conversationHistory.subList(startIndex, conversationHistory.size());
        
        StringBuilder sb = new StringBuilder();
        for (String message : recentMessages) {
            sb.append(message).append("\n");
        }
        
        return sb.toString().trim();
    }
}
