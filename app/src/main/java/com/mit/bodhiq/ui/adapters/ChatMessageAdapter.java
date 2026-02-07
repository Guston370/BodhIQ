package com.mit.bodhiq.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.mit.bodhiq.R;
import com.mit.bodhiq.data.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying chat messages with different layouts for user and AI
 * messages
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;
    private static final int VIEW_TYPE_MEDICAL_CARD = 3;
    private static final int VIEW_TYPE_SYSTEM = 4;
    private static final int VIEW_TYPE_TYPING = 5;
    private static final int VIEW_TYPE_ERROR = 6;

    private final List<ChatMessage> messages;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatMessageAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);

        if (message.isFromUser()) {
            return VIEW_TYPE_USER;
        } else {
            switch (message.getType()) {
                case AI_MEDICAL_CARD:
                    return VIEW_TYPE_MEDICAL_CARD;
                case SYSTEM_INFO:
                    return VIEW_TYPE_SYSTEM;
                case AI_TYPING:
                    return VIEW_TYPE_TYPING;
                case AI_ERROR:
                    return VIEW_TYPE_ERROR;
                default:
                    return VIEW_TYPE_AI;
            }
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        switch (viewType) {
            case VIEW_TYPE_USER:
                view = inflater.inflate(R.layout.item_chat_message_user, parent, false);
                break;
            case VIEW_TYPE_MEDICAL_CARD:
                view = inflater.inflate(R.layout.item_chat_message_medical, parent, false);
                break;
            case VIEW_TYPE_SYSTEM:
                view = inflater.inflate(R.layout.item_chat_message_system, parent, false);
                break;
            case VIEW_TYPE_TYPING:
                view = inflater.inflate(R.layout.item_chat_typing_indicator, parent, false);
                return new TypingViewHolder(view);
            case VIEW_TYPE_ERROR:
                view = inflater.inflate(R.layout.item_chat_error, parent, false);
                return new ErrorViewHolder(view);
            default:
                view = inflater.inflate(R.layout.item_chat_message_ai, parent, false);
                break;
        }

        return new MessageViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        protected final TextView messageText;
        protected final TextView timeText;
        protected final MaterialCardView messageCard;
        protected final int viewType;

        public MessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            messageText = itemView.findViewById(R.id.tv_message);
            timeText = itemView.findViewById(R.id.tv_time);
            messageCard = itemView.findViewById(R.id.card_message);
        }

        public void bind(ChatMessage message) {
            if (messageText != null) {
                messageText.setText(message.getContent());
            }

            if (timeText != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                timeText.setText(timeFormat.format(new Date(message.getTimestamp())));
            }

            // Set card styling based on message type and severity
            if (messageCard != null) {
                switch (viewType) {
                    case VIEW_TYPE_USER:
                        messageCard.setCardBackgroundColor(
                                itemView.getContext().getColor(R.color.primary_color));
                        if (messageText != null) {
                            messageText.setTextColor(
                                    itemView.getContext().getColor(R.color.white));
                        }
                        break;

                    case VIEW_TYPE_MEDICAL_CARD:
                        setSeverityCardColor(message.getSeverity());
                        break;

                    case VIEW_TYPE_SYSTEM:
                        messageCard.setCardBackgroundColor(
                                itemView.getContext().getColor(R.color.warning_container));
                        if (messageText != null) {
                            messageText.setTextColor(
                                    itemView.getContext().getColor(R.color.warning));
                        }
                        break;

                    default:
                        messageCard.setCardBackgroundColor(
                                itemView.getContext().getColor(R.color.surface_variant));
                        if (messageText != null) {
                            messageText.setTextColor(
                                    itemView.getContext().getColor(R.color.text_primary));
                        }
                        break;
                }
            }
        }

        private void setSeverityCardColor(ChatMessage.Severity severity) {
            if (messageCard == null || severity == null)
                return;

            int backgroundColor, textColor;

            switch (severity) {
                case CRITICAL:
                    backgroundColor = R.color.error_container;
                    textColor = R.color.error;
                    break;
                case HIGH:
                    backgroundColor = R.color.warning_container;
                    textColor = R.color.warning;
                    break;
                case MEDIUM:
                    backgroundColor = R.color.info_background;
                    textColor = R.color.info_text;
                    break;
                default:
                    backgroundColor = R.color.success_container;
                    textColor = R.color.success;
                    break;
            }

            messageCard.setCardBackgroundColor(itemView.getContext().getColor(backgroundColor));
            if (messageText != null) {
                messageText.setTextColor(itemView.getContext().getColor(textColor));
            }
        }
    }

    static class TypingViewHolder extends MessageViewHolder {
        public TypingViewHolder(@NonNull View itemView) {
            super(itemView, VIEW_TYPE_TYPING);
        }

        @Override
        public void bind(ChatMessage message) {
            // No binding needed for typing indicator
        }
    }

    static class ErrorViewHolder extends MessageViewHolder {
        private final TextView errorMessage;

        public ErrorViewHolder(@NonNull View itemView) {
            super(itemView, VIEW_TYPE_ERROR);
            errorMessage = itemView.findViewById(R.id.tv_error_message);
        }

        @Override
        public void bind(ChatMessage message) {
            if (errorMessage != null) {
                errorMessage.setText(message.getContent());
            }
            // Time binding is handled by super if ID matches, or we can override
        }
    }
}
