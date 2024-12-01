package com.example.footballapplication.matches.statistics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footballapplication.R;

import java.util.ArrayList;

public class MatchStatisticsAdapter extends RecyclerView.Adapter<MatchStatisticsAdapter.MatchStatisticsViewHolder> {

    ArrayList<MatchStatistics> statistics;
    Context mainContext;

    public MatchStatisticsAdapter(ArrayList<MatchStatistics> statistics, Context mainContext) {
        this.statistics = statistics;
        this.mainContext = mainContext;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDate(ArrayList<MatchStatistics> newStats) {
        this.statistics.clear();
        this.statistics.addAll(newStats);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MatchStatisticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mainContext).inflate(R.layout.statistics_recycler_row, parent, false);
        return new MatchStatisticsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchStatisticsViewHolder holder, int position) {
        MatchStatistics matchStatistics = statistics.get(position);
        holder.title.setText(matchStatistics.getStatisticTitle());

        int homeStatistic = matchStatistics.getHomeStatistic();
        int awayStatistic = matchStatistics.getAwayStatistic();

        String homeStatisticString = homeStatistic + "";
        String awayStatisticString = awayStatistic + "";

        if (matchStatistics.isPercentage()) {
            homeStatisticString += "%";
            awayStatisticString += "%";
        }

        holder.home.setText(homeStatisticString);
        holder.away.setText(awayStatisticString);

        int fullStatistic = homeStatistic + awayStatistic;
        if (fullStatistic == 0) {
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(Color.DKGRAY));
            holder.progressBar.setProgress(100);
        } else {
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(matchStatistics.getHomeColor()));
            holder.progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(matchStatistics.getAwayColor()));
            float value = ((float) homeStatistic / fullStatistic) * 100;
            int valueInt = Math.round(value);
            holder.progressBar.setProgress(valueInt);
        }
    }

    @Override
    public int getItemCount() {
        return statistics.size();
    }

    public static class MatchStatisticsViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView home;
        private final TextView away;
        private final ProgressBar progressBar;

        public MatchStatisticsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.match_statistics_row_title);
            home = itemView.findViewById(R.id.match_statistics_row_home);
            away = itemView.findViewById(R.id.match_statistics_row_away);
            progressBar = itemView.findViewById(R.id.match_statistics_row_progress_bar);
        }
    }
}
