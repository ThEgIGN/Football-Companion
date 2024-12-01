package com.example.footballapplication.matches.statistics;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footballapplication.BuildConfig;
import com.example.footballapplication.map.MapActivity;
import com.example.footballapplication.orientation.ScreenSizeHelper;
import com.example.footballapplication.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@SuppressWarnings("SpellCheckingInspection")
public class MatchStatisticsActivity extends AppCompatActivity {

    ArrayList<MatchStatistics> statistics;

    RecyclerView recyclerView;
    MatchStatisticsAdapter adapter;
    LinearProgressIndicator linearProgressIndicator;

    private int matchStatId, homeColor, awayColor;

    @Override
    public void onDestroy(){
        super.onDestroy();
        recyclerView.setAdapter(null);
        adapter = null;
        recyclerView = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenSizeHelper.lockOrientationForSmallPhones(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_match_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton button = findViewById(R.id.match_statistics_back_button);
        button.setOnClickListener(view -> finish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        Intent intent = getIntent();
        String matchStatTitle = intent.getStringExtra("title");
        matchStatId = intent.getIntExtra("id", 0);
        homeColor = intent.getIntExtra("homeColor", 0);
        awayColor = intent.getIntExtra("awayColor", 0);

        recyclerView = findViewById(R.id.match_statistics_recycler_view);
        linearProgressIndicator = findViewById(R.id.match_statistics_progress_infinite_bar);
        TextView matchStatisticsTitle = findViewById(R.id.match_statistics_teams);
        matchStatisticsTitle.setText(matchStatTitle);

        Button buttonFindStadium = findViewById(R.id.match_statistics_find_stadium_button);
        buttonFindStadium.setOnClickListener(view -> getResponse(false));

        setupRecyclerView();
        if (savedInstanceState != null) {
            ArrayList<MatchStatistics> savedStats = savedInstanceState.getParcelableArrayList("statistics");
            runOnUiThread(() -> {
                adapter.updateDate(savedStats);
                changeInProgress(false);
            });
        } else {
            getResponse(true);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("statistics", statistics);
        super.onSaveInstanceState(outState);
    }

    private void getResponse(boolean stats) {
        changeInProgress(true);
        String url = "https://footapi7.p.rapidapi.com/api/match/" + matchStatId;

        if (stats) {
            url += "/statistics";
        }

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-key", BuildConfig.RAPID_API_KEY)
                .addHeader("x-rapidapi-host", "footapi7.p.rapidapi.com")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("MatchStatistics", "Failed grabbing data with API");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        try {
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                String responseString = responseBody.string();
                                if (TextUtils.isEmpty(responseString)) {
                                    changeInProgress(false);
                                    Toast.makeText(MatchStatisticsActivity.this, getString(R.string.statistics_unavailable_for_current_match), Toast.LENGTH_SHORT).show();
                                } else {
                                    if (stats) {
                                        decodeJSONStats(responseString);
                                    } else {
                                        decodeJSONStadium(responseString);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.i("MatchStatistics", "Executor failed while reading response. " + e);
                        }
                    });
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void decodeJSONStats(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray array = root.getJSONArray("statistics");

            JSONObject object = array.getJSONObject(0);
            JSONArray groups = object.getJSONArray("groups");

            boolean totalShotsDone = false;
            for (int i = 0; i < groups.length(); i++) {
                JSONObject stat = groups.getJSONObject(i);
                String statName = stat.getString("groupName");

                if (!statName.equals("Match overview") && !statName.equals("Shots") && !statName.equals("Attack")) {
                    continue;
                }

                JSONArray statisticItems = stat.getJSONArray("statisticsItems");
                for (int j = 0; j < statisticItems.length(); j++) {
                    JSONObject mainStat = statisticItems.getJSONObject(j);
                    String mainStatName = mainStat.getString("name");
                    switch (mainStatName) {
                        case "Ball possession":
                            statistics.add(createMatchStatObject(mainStat, getString(R.string.ball_possession), true, 2));
                            break;
                        case "Total shots":
                            if (!totalShotsDone) {
                                statistics.add(createMatchStatObject(mainStat, getString(R.string.total_shots), false, 0));
                                totalShotsDone = true;
                            }
                            break;
                        case "Corner kicks":
                            statistics.add(createMatchStatObject(mainStat, getString(R.string.corner_kicks), false, 8));
                            break;
                        case "Fouls":
                            statistics.add(createMatchStatObject(mainStat, getString(R.string.fouls), false, 4));
                            break;
                        case "Passes":
                            statistics.add(createMatchStatObject(mainStat, getString(R.string.passes), false, 3));
                            break;
                        case "Yellow cards":
                            statistics.add(createMatchStatObject(mainStat, getString(R.string.yellow_cards), false, 5));
                            break;
                        case "Red cards":
                            statistics.add(createMatchStatObject(mainStat, getString(R.string.red_cards), false, 6));
                            break;
                        case "Offsides":
                            statistics.add(createMatchStatObject(mainStat, getString(R.string.offsides), false, 7));
                            break;
                        case "Shots on target":
                            statistics.add(createMatchStatObject(mainStat, getString(R.string.shots_on_target), false, 1));
                            break;
                    }
                }
            }

            statistics.sort(Comparator.comparingInt(MatchStatistics::getOrder));

            runOnUiThread(() -> {
                changeInProgress(false);
                adapter.notifyDataSetChanged();
            });
        } catch (Exception e) {
            runOnUiThread(() -> changeInProgress(false));
            Log.i("MatchStatistics", "Error while decoding JSON stats. " + e);
        }
    }

    private void decodeJSONStadium(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONObject event = (JSONObject) root.get("event");
            JSONObject venue = (JSONObject) event.get("venue");
            JSONObject stadium = (JSONObject) venue.get("stadium");
            String stadiumName = stadium.getString("name");
            JSONObject coords = (JSONObject) venue.get("venueCoordinates");
            double latitude = coords.getDouble("latitude");
            double longitude = coords.getDouble("longitude");

            changeInProgress(false);
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("stadiumName", stadiumName);
            startActivity(intent);
        } catch (Exception e) {
            runOnUiThread(() -> changeInProgress(false));
            Log.i("MatchStatistics", "Error while decoding JSON location. " + e);
        }
    }

    private MatchStatistics createMatchStatObject(JSONObject mainStat, String title, boolean percentage, int order) throws JSONException {
        int homeValue = mainStat.getInt("homeValue");
        int awayValue = mainStat.getInt("awayValue");
        return new MatchStatistics(title, homeValue, awayValue, percentage, homeColor, awayColor, order);
    }

    private void setupRecyclerView() {
        statistics = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MatchStatisticsAdapter(statistics, this);
        recyclerView.setAdapter(adapter);
    }

    private void changeInProgress(boolean show) {
        if (show) {
            linearProgressIndicator.setVisibility(View.VISIBLE);
        } else {
            linearProgressIndicator.setVisibility(View.INVISIBLE);
        }
    }
}