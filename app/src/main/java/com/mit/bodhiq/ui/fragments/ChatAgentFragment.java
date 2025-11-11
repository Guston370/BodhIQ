package com.mit.bodhiq.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mit.bodhiq.agent.MedicalReportAgent;
import com.mit.bodhiq.chatbot.ChatController;
import com.mit.bodhiq.data.model.ChatMessage;
import com.mit.bodhiq.data.model.UserProfile;
import com.mit.bodhiq.data.repository.ProfileRepository;
import com.mit.bodhiq.databinding.FragmentChatAgentBinding;
import com.mit.bodhiq.ui.adapters.ChatMessageAdapter;
import com.mit.bodhiq.utils.GeminiApiService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ChatAgentFragment - Enhanced Medical AI Chat interface
 * Provides intelligent medical chat with ML Kit integration
 */
@AndroidEntryPoint
public class ChatAgentFragment extends Fragment {

    private FragmentChatAgentBinding binding;
    private ChatMessageAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private CompositeDisposable disposables;
    
    @Inject
    MedicalReportAgent medicalReportAgent;
    
    @Inject
    GeminiApiService geminiApiService;
    
    @Inject
    ProfileRepository profileRepository;
    
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private UserProfile currentUserProfile;
    private ChatController chatController;
    private String lastReportText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatAgentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize components
        initializeComponents();
        setupChatInterface();
        loadUserProfile();
        showWelcomeMessage();
    }

    private void initializeComponents() {
        disposables = new CompositeDisposable();
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        
        // Initialize ChatController
        chatController = new ChatController(requireContext());
        
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatMessageAdapter(chatMessages);
        
        binding.rvChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvChatMessages.setAdapter(chatAdapter);
    }

    private void setupChatInterface() {
        // Setup send button
        binding.fabSend.setOnClickListener(v -> sendMessage());
        
        // Setup enter key to send message
        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
        
        // Show empty state initially
        showEmptyState(chatMessages.isEmpty());
    }

    private void loadUserProfile() {
        disposables.add(
            profileRepository.getUserProfile()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    profile -> {
                        currentUserProfile = profile;
                        chatController.setUserProfile(profile);
                    },
                    error -> {
                        // Handle error silently, continue without profile
                    }
                )
        );
    }
    
    /**
     * Set report text from scanned/uploaded report
     */
    public void setReportText(String reportText) {
        this.lastReportText = reportText;
        if (chatController != null) {
            chatController.setReportText(reportText);
        }
    }

    private void showWelcomeMessage() {
        ChatMessage welcomeMessage = new ChatMessage();
        welcomeMessage.setType(ChatMessage.MessageType.AI_RESPONSE);
        welcomeMessage.setFromUser(false);
        welcomeMessage.setContent(
            "🤖 **Welcome to BodhIQ Medical Assistant!**\n\n" +
            "I'm powered by Google's Gemini AI and can help you with:\n\n" +
            "• 🔬 **Medical report analysis** - Upload or describe your lab results\n" +
            "• 🩺 **Symptom assessment** - Tell me about any symptoms you're experiencing\n" +
            "• 💊 **Health guidance** - Get personalized health recommendations\n" +
            "• 📋 **Test interpretation** - Understand what your medical tests mean\n" +
            "• 🏥 **When to seek care** - Guidance on when to see a healthcare provider\n\n" +
            "**How to get started:**\n" +
            "• Type your symptoms or health questions\n" +
            "• Share details from your medical reports\n" +
            "• Ask about medications, treatments, or health conditions\n" +
            "• Request explanations of medical terms\n\n" +
            "**Example questions:**\n" +
            "• \"I have a headache and feel dizzy\"\n" +
            "• \"What does high cholesterol mean?\"\n" +
            "• \"My blood pressure is 140/90, is that normal?\"\n\n" +
            "⚠️ **Important:** I provide educational information only. Always consult your healthcare provider for medical decisions and treatment."
        );
        
        addMessageToChat(welcomeMessage);
    }
    
    private void sendMessage() {
        String message = binding.etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // Clear input
        binding.etMessage.setText("");
        
        // Add user message to chat
        ChatMessage userMessage = new ChatMessage();
        userMessage.setType(ChatMessage.MessageType.USER_TEXT);
        userMessage.setFromUser(true);
        userMessage.setContent(message);
        
        addMessageToChat(userMessage);
        
        // Show typing indicator
        showTypingIndicator();
        
        // Process the message
        processUserMessage(message);
    }

    private void processUserMessage(String message) {
        // Process message in background using ChatController
        new Thread(() -> {
            try {
                ChatMessage response = chatController.processMessage(message);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideTypingIndicator();
                        addMessageToChat(response);
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideTypingIndicator();
                        showErrorMessage("I encountered an error processing your message. Please try again.");
                    });
                }
            }
        }).start();
    }
    
    /**
     * Handle action button clicks (Create Reminder, Open QR, etc.)
     */
    private void onActionButtonClick(String actionType) {
        switch (actionType) {
            case "CREATE_REMINDER":
                if (chatController.getActionHandler().executePendingReminder()) {
                    Toast.makeText(getContext(), "Opening reminder creation...", Toast.LENGTH_SHORT).show();
                }
                break;
            case "EMERGENCY_QR":
                if (chatController.getActionHandler().executePendingEmergencyQR()) {
                    Toast.makeText(getContext(), "Opening Emergency QR...", Toast.LENGTH_SHORT).show();
                }
                break;
            case "EMERGENCY_CALL":
                if (chatController.getActionHandler().executeEmergencyCall()) {
                    Toast.makeText(getContext(), "Opening emergency dialer...", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    
    private String buildUserContext() {
        StringBuilder context = new StringBuilder();
        
        if (currentUserProfile != null) {
            if (currentUserProfile.getAge() != null && !currentUserProfile.getAge().isEmpty()) {
                context.append("Age: ").append(currentUserProfile.getAge()).append(" years. ");
            }
            if (currentUserProfile.getGender() != null && !currentUserProfile.getGender().isEmpty()) {
                context.append("Gender: ").append(currentUserProfile.getGender()).append(". ");
            }
            if (currentUserProfile.getFullName() != null && !currentUserProfile.getFullName().isEmpty()) {
                context.append("Name: ").append(currentUserProfile.getFullName()).append(". ");
            }
        }
        
        return context.toString();
    }

    private boolean containsMedicalParameters(String message) {
        String lowerMessage = message.toLowerCase();
        String[] medicalKeywords = {
            "hemoglobin", "glucose", "cholesterol", "blood pressure", "heart rate",
            "white blood cell", "red blood cell", "platelets", "creatinine", "bun",
            "alt", "ast", "bilirubin", "tsh", "t3", "t4", "triglycerides", "hdl", "ldl"
        };
        
        for (String keyword : medicalKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSymptoms(String message) {
        String lowerMessage = message.toLowerCase();
        String[] symptomKeywords = {
            "pain", "ache", "fever", "headache", "nausea", "vomiting", "dizziness",
            "fatigue", "tired", "shortness of breath", "cough", "sore throat",
            "rash", "swelling", "chest pain", "abdominal pain", "back pain"
        };
        
        for (String keyword : symptomKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void handleMedicalParameterMessage(String message, String userContext) {
        // Use Gemini API to analyze medical parameters
        geminiApiService.analyzeMedicalReport(message, userContext, new GeminiApiService.GeminiCallback() {
            @Override
            public void onSuccess(ChatMessage response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideTypingIndicator();
                        addMessageToChat(response);
                        
                        // Add follow-up suggestions if needed
                        if (response.isRequiresFollowUp()) {
                            addFollowUpSuggestions();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideTypingIndicator();
                        showErrorMessage("I'm having trouble analyzing your medical data right now. Please try again.");
                    });
                }
            }
        });
    }

    private void handleSymptomMessage(String message, String userContext) {
        // Use Gemini API to analyze symptoms
        geminiApiService.analyzeSymptoms(message, userContext, new GeminiApiService.GeminiCallback() {
            @Override
            public void onSuccess(ChatMessage response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideTypingIndicator();
                        addMessageToChat(response);
                        
                        // Add emergency guidance if needed
                        if (response.getSeverity() == ChatMessage.Severity.HIGH || 
                            response.getSeverity() == ChatMessage.Severity.CRITICAL) {
                            addEmergencyGuidance();
                        }
                        
                        // Add follow-up suggestions
                        if (response.isRequiresFollowUp()) {
                            addFollowUpSuggestions();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideTypingIndicator();
                        showErrorMessage("I'm having trouble analyzing your symptoms. Please try again.");
                    });
                }
            }
        });
    }

    private void handleGeneralHealthQuery(String message, String userContext) {
        // Use Gemini API for general health queries
        geminiApiService.generateMedicalResponse(message, userContext, new GeminiApiService.GeminiCallback() {
            @Override
            public void onSuccess(ChatMessage response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideTypingIndicator();
                        addMessageToChat(response);
                        
                        // Add follow-up suggestions if needed
                        if (response.isRequiresFollowUp()) {
                            addFollowUpSuggestions();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideTypingIndicator();
                        // Fallback to local response if Gemini fails
                        ChatMessage fallbackResponse = new ChatMessage();
                        fallbackResponse.setType(ChatMessage.MessageType.AI_RESPONSE);
                        fallbackResponse.setFromUser(false);
                        fallbackResponse.setSeverity(ChatMessage.Severity.LOW);
                        fallbackResponse.setContent(generateGeneralHealthResponse(message));
                        fallbackResponse.setMedicalDisclaimer(
                            "This information is for educational purposes only. Consult your healthcare provider for personalized medical advice."
                        );
                        addMessageToChat(fallbackResponse);
                    });
                }
            }
        });
    }

    private String generateGeneralHealthResponse(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("diet") || lowerMessage.contains("nutrition")) {
            return "🥗 **Nutrition Guidance**\n\n" +
                   "A balanced diet is crucial for optimal health:\n" +
                   "• Include plenty of fruits and vegetables (5-9 servings daily)\n" +
                   "• Choose whole grains over refined grains\n" +
                   "• Include lean proteins (fish, poultry, legumes)\n" +
                   "• Limit processed foods and added sugars\n" +
                   "• Stay hydrated with 8-10 glasses of water daily\n\n" +
                   "Consider consulting a registered dietitian for personalized nutrition advice.";
        }
        
        if (lowerMessage.contains("exercise") || lowerMessage.contains("fitness")) {
            return "🏃‍♀️ **Exercise Recommendations**\n\n" +
                   "Regular physical activity provides numerous health benefits:\n" +
                   "• Aim for 150 minutes of moderate aerobic activity weekly\n" +
                   "• Include strength training exercises 2+ days per week\n" +
                   "• Start slowly and gradually increase intensity\n" +
                   "• Choose activities you enjoy to maintain consistency\n" +
                   "• Consider walking, swimming, cycling, or dancing\n\n" +
                   "Consult your healthcare provider before starting a new exercise program.";
        }
        
        if (lowerMessage.contains("sleep") || lowerMessage.contains("insomnia")) {
            return "😴 **Sleep Health Tips**\n\n" +
                   "Quality sleep is essential for overall health:\n" +
                   "• Aim for 7-9 hours of sleep nightly\n" +
                   "• Maintain a consistent sleep schedule\n" +
                   "• Create a relaxing bedtime routine\n" +
                   "• Keep your bedroom cool, dark, and quiet\n" +
                   "• Limit screen time before bed\n" +
                   "• Avoid caffeine and large meals before bedtime\n\n" +
                   "If sleep problems persist, consult your healthcare provider.";
        }
        
        // Default response
        return "🤖 **Health Information**\n\n" +
               "I'd be happy to help with your health question! For the most accurate and personalized guidance, " +
               "I recommend:\n\n" +
               "• Being more specific about your health concern\n" +
               "• Sharing any relevant symptoms or medical history\n" +
               "• Mentioning any specific health parameters you're curious about\n\n" +
               "You can also ask me about:\n" +
               "• Interpreting medical test results\n" +
               "• Understanding symptoms\n" +
               "• General health and wellness tips\n" +
               "• When to seek medical attention\n\n" +
               "What specific aspect of your health would you like to discuss?";
    }

    private void addFollowUpSuggestions() {
        ChatMessage followUp = new ChatMessage();
        followUp.setType(ChatMessage.MessageType.AI_RECOMMENDATION);
        followUp.setFromUser(false);
        followUp.setContent(
            "📋 **Recommended Next Steps:**\n\n" +
            "• Schedule an appointment with your healthcare provider\n" +
            "• Keep a symptom diary to track changes\n" +
            "• Monitor your vital signs if possible\n" +
            "• Prepare questions for your doctor visit\n" +
            "• Bring any relevant medical records or test results\n\n" +
            "Would you like help preparing questions for your healthcare provider?"
        );
        
        addMessageToChat(followUp);
    }

    private void addEmergencyGuidance() {
        ChatMessage emergency = new ChatMessage();
        emergency.setType(ChatMessage.MessageType.SYSTEM_INFO);
        emergency.setFromUser(false);
        emergency.setSeverity(ChatMessage.Severity.CRITICAL);
        emergency.setContent(
            "🚨 **IMPORTANT: Seek Immediate Medical Attention**\n\n" +
            "Based on your symptoms, you should consider:\n" +
            "• Calling emergency services (911) if symptoms are severe\n" +
            "• Visiting the nearest emergency room\n" +
            "• Contacting your healthcare provider immediately\n\n" +
            "**Emergency Warning Signs:**\n" +
            "• Difficulty breathing or shortness of breath\n" +
            "• Chest pain or pressure\n" +
            "• Severe abdominal pain\n" +
            "• Sudden confusion or difficulty speaking\n" +
            "• Loss of consciousness\n\n" +
            "Don't delay seeking professional medical care."
        );
        
        addMessageToChat(emergency);
    }

    private void showTypingIndicator() {
        ChatMessage typingMessage = new ChatMessage();
        typingMessage.setType(ChatMessage.MessageType.SYSTEM_INFO);
        typingMessage.setFromUser(false);
        typingMessage.setContent("🤖 Analyzing your message...");
        
        addMessageToChat(typingMessage);
    }

    private void hideTypingIndicator() {
        // Remove the last message if it's a typing indicator
        if (!chatMessages.isEmpty()) {
            ChatMessage lastMessage = chatMessages.get(chatMessages.size() - 1);
            if (lastMessage.getType() == ChatMessage.MessageType.SYSTEM_INFO && 
                lastMessage.getContent().contains("Analyzing")) {
                chatMessages.remove(chatMessages.size() - 1);
                chatAdapter.notifyItemRemoved(chatMessages.size());
            }
        }
    }

    private void addMessageToChat(ChatMessage message) {
        chatMessages.add(message);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        binding.rvChatMessages.scrollToPosition(chatMessages.size() - 1);
        
        // Hide empty state
        showEmptyState(false);
    }

    private void showErrorMessage(String error) {
        ChatMessage errorMessage = new ChatMessage();
        errorMessage.setType(ChatMessage.MessageType.SYSTEM_INFO);
        errorMessage.setFromUser(false);
        errorMessage.setContent("❌ " + error);
        
        addMessageToChat(errorMessage);
    }
    
    private void showEmptyState(boolean show) {
        binding.layoutEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.rvChatMessages.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (disposables != null) {
            disposables.clear();
        }
        binding = null;
    }
}