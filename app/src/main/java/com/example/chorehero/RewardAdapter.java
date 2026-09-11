package com.example.chorehero;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {

    private List<Reward> rewardList;
    private OnRewardClickListener listener;

    public interface OnRewardClickListener {
        void onClaimClick(Reward reward);
        void onDeleteClick(Reward reward);
    }

    public RewardAdapter(List<Reward> rewardList, OnRewardClickListener listener) {
        this.rewardList = rewardList;
        this.listener = listener;
    }

    public void setRewards(List<Reward> rewardList) {
        this.rewardList = rewardList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Reward reward = rewardList.get(position);
        if (holder.tvRewardTitle != null) holder.tvRewardTitle.setText(reward.title);
        if (holder.tvRewardCost != null) holder.tvRewardCost.setText(reward.pointsCost + " PTS");

        Context context = holder.itemView.getContext();
        SharedPreferences prefs = context.getSharedPreferences("ChoreHeroPrefs", Context.MODE_PRIVATE);
        String userRole = prefs.getString("user_role", "parent");

        // PROMJENA OVDJE: Čitamo direktno iz baze, a ne iz SharedPreferences seta
        boolean isClaimed = reward.isClaimed;

        if ("parent".equalsIgnoreCase(userRole)) {
            // RODITELJ: Prikazuje status ako je nagrada preuzeta i dugme za brisanje
            if (holder.btnClaimReward != null) {
                if (isClaimed) {
                    holder.btnClaimReward.setText("Preuzeta nagrada");
                    holder.btnClaimReward.setBackgroundColor(Color.parseColor("#B0BEC5")); // siva
                    holder.btnClaimReward.setEnabled(false);
                    holder.btnClaimReward.setVisibility(View.VISIBLE);
                } else {
                    holder.btnClaimReward.setVisibility(View.GONE);
                }
            }

            if (holder.btnDeleteReward != null) {
                holder.btnDeleteReward.setVisibility(View.VISIBLE);
                holder.btnDeleteReward.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(reward);
                });
            }

        } else {
            // DIJETE: Ima gumb "Preuzmi", nema dugme za brisanje
            if (holder.btnDeleteReward != null) {
                holder.btnDeleteReward.setVisibility(View.GONE);
            }

            if (holder.btnClaimReward != null) {
                holder.btnClaimReward.setVisibility(View.VISIBLE);
                if (isClaimed) {
                    holder.btnClaimReward.setText("Preuzeto");
                    holder.btnClaimReward.setBackgroundColor(Color.parseColor("#B0BEC5"));
                    holder.btnClaimReward.setEnabled(false);
                    holder.btnClaimReward.setOnClickListener(null);
                } else {
                    holder.btnClaimReward.setText("Preuzmi");
                    holder.btnClaimReward.setBackgroundColor(Color.parseColor("#26A69A"));
                    holder.btnClaimReward.setEnabled(true);
                    holder.btnClaimReward.setOnClickListener(v -> {
                        if (listener != null) listener.onClaimClick(reward);
                    });
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return rewardList != null ? rewardList.size() : 0;
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        TextView tvRewardTitle, tvRewardCost;
        Button btnClaimReward;
        ImageButton btnDeleteReward;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRewardTitle = itemView.findViewById(R.id.tvRewardTitle);
            tvRewardCost = itemView.findViewById(R.id.tvRewardCost);
            btnClaimReward = itemView.findViewById(R.id.btnClaimReward);
            btnDeleteReward = itemView.findViewById(R.id.btnDeleteReward);
        }
    }
}