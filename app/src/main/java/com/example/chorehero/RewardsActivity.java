package com.example.chorehero;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RewardsActivity extends AppCompatActivity implements RewardAdapter.OnRewardClickListener {

    private RecyclerView rvRewards;
    private RewardAdapter adapter;
    private List<Reward> rewardList = new ArrayList<>();
    private AppDatabase db;
    private TextView tvTotalPointsRewards;
    private FloatingActionButton fabAddReward;
    private String familyCode = "HERO1234";
    private String userRole = "parent";
    private ExecutorService executorService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        prefs = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE);
        familyCode = prefs.getString("family_code", "HERO1234");
        userRole = prefs.getString("user_role", "parent");

        rvRewards = findViewById(R.id.rvRewards);
        tvTotalPointsRewards = findViewById(R.id.tvTotalPointsRewards);
        fabAddReward = findViewById(R.id.fabAddReward);

        if (rvRewards != null) {
            rvRewards.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RewardAdapter(rewardList, this);
            rvRewards.setAdapter(adapter);
        }

        if (fabAddReward != null) {
            if ("parent".equalsIgnoreCase(userRole)) {
                fabAddReward.setVisibility(View.VISIBLE);
                fabAddReward.setOnClickListener(v -> showAddRewardDialog());
            } else {
                fabAddReward.setVisibility(View.GONE);
            }
        }

        checkAndInsertDefaultRewards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRewards();
    }

    private void checkAndInsertDefaultRewards() {
        executorService.execute(() -> {
            List<Reward> existing = db.rewardDao().getRewardsForFamily(familyCode);
            if (existing == null || existing.isEmpty()) {
                db.rewardDao().insertReward(new Reward("30 min igrica", 20, familyCode));
                db.rewardDao().insertReward(new Reward("Omiljeni slatkiš", 35, familyCode));
                db.rewardDao().insertReward(new Reward("Izbor filma za večer", 50, familyCode));
                db.rewardDao().insertReward(new Reward("Shopping", 150, familyCode));
            }
            loadRewards();
        });
    }

    private void loadRewards() {
        executorService.execute(() -> {
            List<Reward> fromDb = db.rewardDao().getRewardsForFamily(familyCode);
            List<Task> tasks = db.taskDao().getTasksForFamily(familyCode);

            int earnedPoints = 0;
            if (tasks != null) {
                for (Task t : tasks) {
                    if (t.isCompleted) {
                        earnedPoints += t.points;
                    }
                }
            }

            // zarada minus potrošeno
            int spentPoints = prefs.getInt("spent_points_" + familyCode, 0);
            int availablePoints = earnedPoints - spentPoints;
            if (availablePoints < 0) availablePoints = 0;

            int finalAvailablePoints = availablePoints;
            new Handler(Looper.getMainLooper()).post(() -> {
                rewardList.clear();
                if (fromDb != null) {
                    rewardList.addAll(fromDb);
                }
                if (adapter != null) {
                    adapter.setRewards(rewardList);
                }
                if (tvTotalPointsRewards != null) {
                    tvTotalPointsRewards.setText(finalAvailablePoints + " PTS");
                }
            });
        });
    }

    private void showAddRewardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dodaj novu nagradu");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_reward, null);
        EditText etTitle = view.findViewById(R.id.etRewardTitle);
        EditText etCost = view.findViewById(R.id.etRewardCost);
        builder.setView(view);

        builder.setPositiveButton("Dodaj", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String costStr = etCost.getText().toString().trim();

            if (!title.isEmpty() && !costStr.isEmpty()) {
                int cost = Integer.parseInt(costStr);
                executorService.execute(() -> {
                    Reward newReward = new Reward(title, cost, familyCode);
                    db.rewardDao().insertReward(newReward);
                    loadRewards();
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(this, "Nagrada uspješno dodana!", Toast.LENGTH_SHORT).show()
                    );
                });
            } else {
                Toast.makeText(this, "Molimo unesite naziv i cijenu nagrade.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Otkaži", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onClaimClick(Reward reward) {
        executorService.execute(() -> {
            List<Task> tasks = db.taskDao().getTasksForFamily(familyCode);
            int earnedPoints = 0;
            if (tasks != null) {
                for (Task t : tasks) {
                    if (t.isCompleted) {
                        earnedPoints += t.points;
                    }
                }
            }

            int spentPoints = prefs.getInt("spent_points_" + familyCode, 0);
            int availablePoints = earnedPoints - spentPoints;

            int finalAvailablePoints = availablePoints;
            int finalEarnedPoints = earnedPoints;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalAvailablePoints >= reward.pointsCost) {
                    // Provjera da li je već preuzeta
                    Set<String> claimedRewards = prefs.getStringSet("claimed_rewards", new HashSet<>());
                    if (claimedRewards.contains(reward.title)) {
                        Toast.makeText(this, "Ova nagrada je već preuzeta!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 1. Dodaj trošak u potrošene bodove
                    int newSpentPoints = spentPoints + reward.pointsCost;
                    prefs.edit().putInt("spent_points_" + familyCode, newSpentPoints).apply();

                    // 2. Spremi nagradu u preuzete
                    Set<String> newClaimed = new HashSet<>(claimedRewards);
                    newClaimed.add(reward.title);
                    prefs.edit().putStringSet("claimed_rewards", newClaimed).apply();

                    // 3. Ažuriraj prikaz bodova na vrhu i adaptera
                    int remainingPoints = finalEarnedPoints - newSpentPoints;
                    if (tvTotalPointsRewards != null) {
                        tvTotalPointsRewards.setText(remainingPoints + " PTS");
                    }
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    // 4. Prikaži avatar čestitku
                    prikaziAvatarNagrade(reward.title);

                } else {
                    Toast.makeText(this, "Nemate dovoljno bodova za ovu nagradu! (Imate " + finalAvailablePoints + " PTS)", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void prikaziAvatarNagrade(String nazivNagrade) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_bravo);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView ivAvatar = dialog.findViewById(R.id.ivBravoAvatar);
        TextView tvPoruka = dialog.findViewById(R.id.tvBravoPoruka);
        TextView tvBodovi = dialog.findViewById(R.id.tvBravoBodovi);

        String ime = prefs.getString("user_name", "Heroj");
        String avatar = prefs.getString("user_avatar", "boy");

        tvPoruka.setText("Bravo " + ime + "!\nUspješno osvojena nagrada!");
        tvBodovi.setText(nazivNagrade);

        boolean isFemale = false;
        if (avatar != null) {
            String lower = avatar.toLowerCase();
            if (lower.contains("girl") || lower.contains("zensko") || lower.contains("žensko") || lower.contains("female") || lower.contains("curica")) {
                isFemale = true;
            }
        }

        if (isFemale) {
            ivAvatar.setImageResource(R.drawable.hero_girl);
        } else {
            ivAvatar.setImageResource(R.drawable.hero_boy);
        }

        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 3500);
    }
}