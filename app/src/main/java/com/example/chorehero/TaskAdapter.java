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

    private List<Task> taskList;
    private OnTaskClickListener listener;
    private boolean isParentView;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onCheckClick(Task task);
        void onEditClick(Task task);
        void onDeleteClick(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener, boolean isParentView) {
        this.taskList = taskList;
        this.listener = listener;
        this.isParentView = isParentView;
    }

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
        this.isParentView = false;
    }

    public void setTasks(List<Task> taskList) {
        this.taskList = taskList;
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
        if (holder.tvTitle != null) holder.tvTitle.setText(task.title);
        if (holder.tvTime != null) holder.tvTime.setText(task.time);
        if (holder.tvPoints != null) holder.tvPoints.setText("+" + task.points + "b");

        // Prikaz statusa zadatka i vizuelni efekti za završene/nezavršene zadatke
        if (holder.ivCheck != null) {
            holder.ivCheck.setVisibility(View.VISIBLE);

            if (task.isCompleted) {
                holder.ivCheck.setImageResource(R.drawable.ic_checkbox_on);
                holder.ivCheck.setAlpha(1.0f); // Puna vidljivost kad je završeno

                // Pastelno zelena pozadina i rub za završen zadatak
                if (holder.cardViewContainer != null) {
                    holder.cardViewContainer.setCardBackgroundColor(Color.parseColor("#F0FDF4"));
                    holder.cardViewContainer.setStrokeColor(Color.parseColor("#86EFAC"));
                }
                if (holder.tvTitle != null) {
                    holder.tvTitle.setTextColor(Color.parseColor("#94A3B8")); // Prigušeniji tekst
                    holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
            } else {
                holder.ivCheck.setImageResource(R.drawable.ic_checkbox_off);
                if (isParentView) {
                    holder.ivCheck.setAlpha(0.4f); // Kod roditelja nezavršeni krug je malo prigušen
                } else {
                    holder.ivCheck.setAlpha(1.0f);
                }

                // Standardna bijela pozadina i mint rub za nezavršene zadatke
                if (holder.cardViewContainer != null) {
                    holder.cardViewContainer.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                    holder.cardViewContainer.setStrokeColor(Color.parseColor("#2EC4B6"));
                }
                if (holder.tvTitle != null) {
                    holder.tvTitle.setTextColor(Color.parseColor("#0F172A"));
                    holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
            }

            // Ako je roditeljski ekran, potpuno gasimo klik na kvačicu
            if (isParentView) {
                holder.ivCheck.setClickable(false);
                holder.ivCheck.setEnabled(false);
                holder.ivCheck.setOnClickListener(null);
            } else {
                holder.ivCheck.setClickable(true);
                holder.ivCheck.setEnabled(true);
                holder.ivCheck.setOnClickListener(v -> {
                    if (listener != null) listener.onCheckClick(task);
                });
            }
        }

        // KONTROLA VIDLJIVOSTI: Uređivanje i brisanje vidljivi samo kod roditelja
        if (isParentView) {
            if (holder.btnEdit != null) {
                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnEdit.setOnClickListener(v -> {
                    if (listener != null) listener.onEditClick(task);
                });
            }
            if (holder.btnDelete != null) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(task);
                });
            }
        } else {
            // Kod djeteta potpuno sakrivamo olovku i kantu za smeće
            if (holder.btnEdit != null) {
                holder.btnEdit.setVisibility(View.GONE);
                holder.btnEdit.setOnClickListener(null);
            }
            if (holder.btnDelete != null) {
                holder.btnDelete.setVisibility(View.GONE);
                holder.btnDelete.setOnClickListener(null);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTaskClick(task);
        });
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvPoints;
        ImageView ivCheck;
        View btnEdit, btnDelete;
        MaterialCardView cardViewContainer;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            ivCheck = itemView.findViewById(R.id.ivCheck);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            cardViewContainer = itemView.findViewById(R.id.cardViewContainer);
        }
    }
}