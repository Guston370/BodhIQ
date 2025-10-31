package com.mit.bodhiq.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mit.bodhiq.R;
import com.mit.bodhiq.models.WellnessGoal;

import java.util.List;

/**
 * Adapter for wellness goals
 */
public class WellnessGoalsAdapter extends RecyclerView.Adapter<WellnessGoalsAdapter.ViewHolder> {
    
    private List<WellnessGoal> goals;
    private OnGoalActionListener listener;
    
    public interface OnGoalActionListener {
        void onEditGoal(WellnessGoal goal);
        void onDeleteGoal(WellnessGoal goal);
    }
    
    public WellnessGoalsAdapter(List<WellnessGoal> goals, OnGoalActionListener listener) {
        this.goals = goals;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_wellness_goal, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WellnessGoal goal = goals.get(position);
        
        holder.textGoalTitle.setText(goal.getTitle());
        holder.textGoalDescription.setText(goal.getDescription());
        
        // Set progress
        double progressPercentage = goal.getProgressPercentage();
        holder.progressGoal.setProgress((int) progressPercentage);
        holder.textGoalProgress.setText(String.format("%.0f%%", progressPercentage));
        holder.textGoalCurrent.setText(goal.getProgressText());
        
        // Set icon based on goal type
        int iconRes = getIconForGoalType(goal.getType());
        holder.iconGoalType.setImageResource(iconRes);
        
        // Set up action buttons
        holder.btnEditGoal.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditGoal(goal);
            }
        });
        
        holder.btnDeleteGoal.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteGoal(goal);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return goals.size();
    }
    
    public void updateGoals(List<WellnessGoal> newGoals) {
        this.goals = newGoals;
        notifyDataSetChanged();
    }
    
    private int getIconForGoalType(String type) {
        switch (type.toLowerCase()) {
            case "steps":
                return R.drawable.ic_directions_walk;
            case "weight":
                return R.drawable.ic_fitness_center;
            case "exercise":
                return R.drawable.ic_fitness_center;
            case "water":
                return R.drawable.ic_local_drink;
            case "sleep":
                return R.drawable.ic_bedtime;
            default:
                return R.drawable.ic_target;
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconGoalType;
        TextView textGoalTitle;
        TextView textGoalDescription;
        TextView textGoalProgress;
        ProgressBar progressGoal;
        TextView textGoalCurrent;
        TextView btnEditGoal;
        TextView btnDeleteGoal;
        
        ViewHolder(View itemView) {
            super(itemView);
            iconGoalType = itemView.findViewById(R.id.icon_goal_type);
            textGoalTitle = itemView.findViewById(R.id.text_goal_title);
            textGoalDescription = itemView.findViewById(R.id.text_goal_description);
            textGoalProgress = itemView.findViewById(R.id.text_goal_progress);
            progressGoal = itemView.findViewById(R.id.progress_goal);
            textGoalCurrent = itemView.findViewById(R.id.text_goal_current);
            btnEditGoal = itemView.findViewById(R.id.btn_edit_goal);
            btnDeleteGoal = itemView.findViewById(R.id.btn_delete_goal);
        }
    }
}