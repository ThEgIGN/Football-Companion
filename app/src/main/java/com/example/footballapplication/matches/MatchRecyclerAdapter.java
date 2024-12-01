package com.example.footballapplication.matches;

import static com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK;
import static com.google.android.material.timepicker.TimeFormat.CLOCK_24H;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footballapplication.MainActivity;
import com.example.footballapplication.R;
import com.example.footballapplication.matches.statistics.MatchStatisticsActivity;
import com.example.footballapplication.notifications.AlarmReceiver;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

@SuppressWarnings("SpellCheckingInspection")
public class MatchRecyclerAdapter extends RecyclerView.Adapter<MatchRecyclerAdapter.MatchViewHolder> {

    ArrayList<Match> matches;
    Context mainContext;
    FragmentActivity mainActivity;

    public MatchRecyclerAdapter(ArrayList<Match> matches, Context mainContext, FragmentActivity mainActivity) {
        this.mainContext = mainContext;
        this.matches = matches;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mainContext).inflate(R.layout.home_recycler_row, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matches.get(position);

        holder.leagueTitle.setText(match.getTournamentName());
        int homeScore = match.getHomeScore();
        int awayScore = match.getAwayScore();
        String score = homeScore + " - " + awayScore;
        holder.score.setText(score);

        String homeTeamName = match.getHomeTeam();
        String awayTeamName = match.getAwayTeam();
        String matchStatus = match.getStatus();

        holder.homeTeamLogo.setText(homeTeamName.substring(0, 1).toUpperCase());
        holder.awayTeamLogo.setText(awayTeamName.substring(0, 1).toUpperCase());

        // Color victory team in winner color, and loser team in loser color
        // If it's a tie, color them both in draw color
        int winnerColor = mainContext.getColor(R.color.winner);
        int loserColor = mainContext.getColor(R.color.loser);
        int drawColor = mainContext.getColor(R.color.draw);

        if (homeScore > awayScore) {
            holder.homeTeamLogo.setBackgroundColor(winnerColor);
            holder.awayTeamLogo.setBackgroundColor(loserColor);
        } else if (homeScore < awayScore) {
            holder.homeTeamLogo.setBackgroundColor(loserColor);
            holder.awayTeamLogo.setBackgroundColor(winnerColor);
        } else {
            holder.homeTeamLogo.setBackgroundColor(drawColor);
            holder.awayTeamLogo.setBackgroundColor(drawColor);
        }

        holder.homeTeamName.setText(homeTeamName);
        holder.awayTeamName.setText(awayTeamName);
        holder.time.setText(match.getDateTime());
        holder.status.setText(matchStatus);

        holder.itemView.setOnClickListener(view -> {
            MatchType type = match.getType();
            // Only finished matches have statistics
            if (type == MatchType.FINISHED && matchStatus.charAt(0) == 'E') {
                openStatisticsActivity(match.getId(), homeScore, awayScore, winnerColor, loserColor, homeTeamName, awayTeamName);
            } else if (type == MatchType.NOT_STARTED) {
                // Show timer if match hasn't started and it wasn't canceled
                if (matchStatus.charAt(0) == 'N') {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (testNotificationPermission()) {
                            openTimePicker(match.getUnixTime() * 1000, homeTeamName, awayTeamName);
                        }
                    } else {
                        openTimePicker(match.getUnixTime() * 1000, homeTeamName, awayTeamName);
                    }
                } else {
                    Toast.makeText(mainContext, mainContext.getString(R.string.match_has_been_canceled_or_postponed), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mainContext, mainContext.getString(R.string.statistics_are_available_for_finished_matches), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private boolean testNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, MainActivity.POST_NOTIFICATIONS_PERMISSION_CODE);
            return false;
        }
    }

    private void createNotification(long timeInMillis, String homeTeam, String awayTeam, long difference) {
        AlarmManager alarmManager = (AlarmManager) mainActivity.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mainContext, AlarmReceiver.class);
        intent.putExtra("title", homeTeam + " vs " + awayTeam);
        String message;
        if (difference <= 0) {
            message = mainContext.getString(R.string.match_has_already_started);
        } else {
            message = mainContext.getString(R.string.match_is_starting_in) + (difference + 1) + mainContext.getString(R.string.match_is_starting_in_part2);
        }
        intent.putExtra("message", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mainContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        Toast.makeText(mainContext, mainContext.getString(R.string.notification_set_successfully), Toast.LENGTH_SHORT).show();
    }

    private void openTimePicker(long unixTimeMili, String homeTeam, String awayTeam) {
        Calendar matchCalendar = Calendar.getInstance();
        matchCalendar.setTimeInMillis(unixTimeMili);
        // By default, timer is set to 30 minutes before match start time
        matchCalendar.add(Calendar.MINUTE, -30);

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(CLOCK_24H)
                .setHour(matchCalendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(matchCalendar.get(Calendar.MINUTE))
                .setInputMode(INPUT_MODE_CLOCK)
                .setTitleText(mainContext.getString(R.string.select_the_time_you_would_like_to_receive_a_notification))
                .build();

        timePicker.addOnPositiveButtonClickListener(view1 -> {
            Calendar todayCalendar = Calendar.getInstance(Locale.getDefault());
            Calendar notificationCalendar = Calendar.getInstance(Locale.getDefault());

            notificationCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            notificationCalendar.set(Calendar.MINUTE, timePicker.getMinute());
            notificationCalendar.set(Calendar.SECOND, 0);

            long todayTime = todayCalendar.getTimeInMillis();
            long notificationTime = notificationCalendar.getTimeInMillis();

            if (todayTime < notificationTime) {
                matchCalendar.add(Calendar.MINUTE, 30);
                long matchTime = matchCalendar.getTimeInMillis();
                long difference = (matchTime - notificationTime) / 60000;
                createNotification(notificationTime, homeTeam, awayTeam, difference);
            } else {
                Toast.makeText(mainContext, mainContext.getString(R.string.notification_can_t_be_set_in_past), Toast.LENGTH_SHORT).show();
            }
        });
        timePicker.show(mainActivity.getSupportFragmentManager(), "TIME_PICKER");
    }

    private void openStatisticsActivity(int id, int homeScore, int awayScore, int winnerColor,
                                        int loserColor, String homeTeam, String awayTeam) {
        Intent intent = new Intent(mainContext, MatchStatisticsActivity.class);
        intent.putExtra("id", id);
        if (homeScore >= awayScore) {
            intent.putExtra("homeColor", winnerColor);
            intent.putExtra("awayColor", loserColor);
        } else {
            intent.putExtra("homeColor", loserColor);
            intent.putExtra("awayColor", winnerColor);
        }
        intent.putExtra("title", homeTeam + " vs " + awayTeam);
        mainContext.startActivity(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDate(ArrayList<Match> newMatches) {
        this.matches.clear();
        this.matches.addAll(newMatches);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {

        private final TextView leagueTitle, homeTeamLogo, awayTeamLogo, score, homeTeamName, awayTeamName, time, status;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            leagueTitle = itemView.findViewById(R.id.match_league_title);
            homeTeamLogo = itemView.findViewById(R.id.match_home_team_logo);
            awayTeamLogo = itemView.findViewById(R.id.match_away_team_logo);
            score = itemView.findViewById(R.id.match_score);
            homeTeamName = itemView.findViewById(R.id.match_home_team_name);
            awayTeamName = itemView.findViewById(R.id.match_away_team_name);
            time = itemView.findViewById(R.id.match_time);
            status = itemView.findViewById(R.id.match_status);
        }
    }
}
