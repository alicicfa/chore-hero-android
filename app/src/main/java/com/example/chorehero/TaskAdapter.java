package com.example.chorehero;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onCheckClick(Task task);
        void onEditClick(Task task); // Dodata metoda za edit
        void onDeleteClick(Task task);
    }

    private List<Task> taskList;
    private final OnTaskClickListener listener;

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public void setTasks(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTitle.setText(task.title);
        holder.tvTime.setText(task.time);
        holder.tvPoints.setText("+" + task.points + "b");

        if (task.isCompleted) {
            holder.btnCheck.setImageResource(R.drawable.ic_checkbox_on);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(Color.parseColor("#94A3B8"));

            holder.cardTask.setAlpha(0.7f);
            holder.cardTask.setCardBackgroundColor(Color.parseColor("#F1F5F9"));
            holder.cardTask.setStrokeColor(Color.parseColor("#CBD5E1"));

            holder.tvPoints.setTextColor(Color.parseColor("#64748B"));
            holder.tvPoints.setBackgroundColor(Color.parseColor("#E2E8F0"));
        } else {
            holder.btnCheck.setImageResource(R.drawable.ic_checkbox_off);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setTextColor(Color.parseColor("#0F172A"));

            holder.cardTask.setAlpha(1.0f);
            holder.cardTask.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.cardTask.setStrokeColor(Color.parseColor("#2EC4B6"));

            holder.tvPoints.setTextColor(Color.parseColor("#0D9488"));
            holder.tvPoints.setBackgroundColor(Color.parseColor("#E6FFFA"));
        }

        // Listeneri
        holder.btnCheck.setOnClickListener(v -> listener.onCheckClick(task));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(task)); // Edit klik
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(task));
        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardTask;
        TextView tvTitle, tvPoints, tvTime;
        ImageView btnCheck, btnEdit, btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTask = itemView.findViewById(R.id.cardTask);
            btnCheck = itemView.findViewById(R.id.btnCheckTask);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvPoints = itemView.findViewById(R.id.tvTaskPoints);
            tvTime = itemView.findViewById(R.id.tvTaskTime);
            btnEdit = itemView.findViewById(R.id.btnEditTask);
            btnDelete = itemView.findViewById(R.id.btnDeleteTask);
        }
    }
}