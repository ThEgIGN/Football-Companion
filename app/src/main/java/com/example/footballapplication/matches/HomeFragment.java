package com.example.footballapplication.matches;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.footballapplication.BuildConfig;
import com.example.footballapplication.R;
import com.example.footballapplication.profile.Profile;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@SuppressWarnings("SpellCheckingInspection")
public class HomeFragment extends Fragment {

    ArrayList<Match> matchesArrayList;
    private ArrayList<Match> allMatches;
    private ArrayList<Match> allMatchesFiltered;
    private ArrayList<Match> allMatchesSearchTerms;

    RecyclerView recyclerView;
    MatchRecyclerAdapter adapter;
    LinearProgressIndicator linearProgressIndicator;
    SearchView searchView;

    private boolean updateRecyclerView;
    private String currentInterests;
    private long beggingOfDay, endOfDay;
    private boolean homeFragmentHidden;
    private boolean homeFragmentFirstTimeCreated;

    private int buttonLiveId;
    private int buttonEndedId;
    private int buttonUpcomingId;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        recyclerView.setAdapter(null);
        adapter = null;
        recyclerView = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<Button> allButtons = new ArrayList<>();
        updateRecyclerView = false;
        ImageButton buttonCalender = view.findViewById(R.id.home_calendar);
        homeFragmentHidden = false;
        homeFragmentFirstTimeCreated = true;

        buttonCalender.setOnClickListener(view1 -> pickDate());

        buttonLiveId = R.id.home_button_live;
        buttonEndedId = R.id.home_button_ended;
        buttonUpcomingId = R.id.home_button_upcoming;
        Button buttonLive = view.findViewById(buttonLiveId);
        allButtons.add(buttonLive);
        Button buttonEnded = view.findViewById(buttonEndedId);
        allButtons.add(buttonEnded);
        Button buttonUpcoming = view.findViewById(buttonUpcomingId);
        allButtons.add(buttonUpcoming);
        Button buttonAll = view.findViewById(R.id.home_button_all);
        allButtons.add(buttonAll);

        for (Button b: allButtons) {
            b.setOnClickListener(view1 -> updateMatchesBasedOnButton(b));
        }

        recyclerView = view.findViewById(R.id.home_recycler_view);
        linearProgressIndicator = view.findViewById(R.id.home_progress_infinite_bar);
        searchView = view.findViewById(R.id.home_search_view);
        setupSearchView();
        setupRecyclerView();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE).getReference().child("Users");
            reference.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Profile profile = snapshot.getValue(Profile.class);
                    if (profile != null) {
                        currentInterests = profile.getSearchStrings().toLowerCase();
                        updateRecyclerView = true;
                        // This is called once when HomeFragment is first created and never again
                        if (!homeFragmentHidden && homeFragmentFirstTimeCreated) {
                            updateRecyclerView = false;
                            homeFragmentFirstTimeCreated = false;
                            Date today = new Date();
                            setPeriodsOfDate(today);
                            grabMatchesForSpecificDateAndUpdateView(today);
                        } else {
                            updateVisibleMatchesBasedOnTerms();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.i("HomeFragmentDatabase", error.toString());
                }
            });
        }
    }

    private void setupRecyclerView() {
        matchesArrayList = new ArrayList<>();
        allMatches = new ArrayList<>();
        allMatchesFiltered = new ArrayList<>();
        allMatchesSearchTerms = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MatchRecyclerAdapter(matchesArrayList, getContext(), requireActivity());
        recyclerView.setAdapter(adapter);
    }

    private void changeInProgress(boolean show) {
        if (show) {
            linearProgressIndicator.setVisibility(View.VISIBLE);
        } else {
            linearProgressIndicator.setVisibility(View.INVISIBLE);
        }
    }

    private void pickDate() {
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.date_picker))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Date date = new Date(selection);
            setPeriodsOfDate(date);
            grabMatchesForSpecificDateAndUpdateView(date);
        });
        materialDatePicker.show(requireActivity().getSupportFragmentManager(), "DATE_PICKER");
    }

    private void setPeriodsOfDate(Date date) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        beggingOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        endOfDay = calendar.getTimeInMillis();
    }

    private void grabMatchesForSpecificDateAndUpdateView(Date date) {
        allMatches.clear();
        allMatchesFiltered.clear();
        getTopMatches(date);
    }

    private void updateVisibleMatchesBasedOnTerms() {
        if (TextUtils.isEmpty(currentInterests)) {
            allMatchesSearchTerms.clear();
            allMatchesSearchTerms.addAll(allMatches);
        } else {
            int countMatching = StringUtils.countMatches(currentInterests, ",");
            if (countMatching == 0) {
                addMatchesBasedOnSearchTerms(new String[]{currentInterests});
            } else {
                String[] inerestsSplit = currentInterests.split(", ");
                addMatchesBasedOnSearchTerms(inerestsSplit);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        homeFragmentHidden = hidden;
        // requireActivity() fails if it's called while fragment isn't on screen, aka hidden
        // This ques update that is checked every time fragment is visible
        if (!hidden && updateRecyclerView) {
            updateRecyclerView = false;
            if (allMatchesSearchTerms.isEmpty()) {
                Toast.makeText(requireContext(), R.string.matches_unavailable_for_specified_terms, Toast.LENGTH_SHORT).show();
            }
            requireActivity().runOnUiThread(() -> adapter.updateDate(allMatchesSearchTerms));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void decodeJSON(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray array = root.getJSONArray("events");

            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                long unixTime = object.getLong("startTimestamp");
                long unixTimeInMillis = unixTime * 1000;

                // User should only see matches that are scheduled for today in their time zone
                if (unixTimeInMillis >= beggingOfDay && unixTimeInMillis <= endOfDay) {
                    String tournamentName = ((JSONObject)object.get("season")).getString("name");

                    MatchType type;
                    JSONObject statusObject = (JSONObject)object.get("status");
                    String matchType = statusObject.getString("type");
                    String status = statusObject.getString("description");
                    switch (matchType) {
                        case "finished":
                            type = MatchType.FINISHED;
                            break;
                        case "inprogress":
                            type = MatchType.IN_PROGRESS;
                            break;
                        default:
                            type = MatchType.NOT_STARTED;
                    }

                    String homeTeam = ((JSONObject)object.get("homeTeam")).getString("name");
                    String awayTeam = ((JSONObject)object.get("awayTeam")).getString("name");

                    int homeScore = 0, awayScore = 0;
                    if (type != MatchType.NOT_STARTED) {
                        homeScore = ((JSONObject)object.get("homeScore")).getInt("current");
                        awayScore = ((JSONObject)object.get("awayScore")).getInt("current");
                    }

                    int id = object.getInt("id");

                    Date date = new Date(unixTimeInMillis);
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    String dateTime = df.format(date);

                    Match match = new Match(tournamentName, homeTeam, awayTeam, dateTime,type, id, homeScore, awayScore, unixTime, status);
                    allMatches.add(match);
                }
            }

            allMatches.sort(Comparator.comparingLong(Match::getUnixTime));
            allMatchesFiltered.addAll(allMatches);

            updateVisibleMatchesBasedOnTerms();

            requireActivity().runOnUiThread(() -> {
                adapter.updateDate(allMatchesSearchTerms);
                changeInProgress(false);
            });

            if (allMatchesSearchTerms.isEmpty()) {
                Toast.makeText(requireContext(), R.string.matches_unavailable_for_specified_terms, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            requireActivity().runOnUiThread(() -> changeInProgress(false));
            Log.i("HomeFragment", "Error while decoding JSON");
        }
    }

    @Nullable
    private String getStringFromRawRes(InputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        try {
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.i("HomeFragment", "IOException while reading from stream");
            return null;
        } finally {
            try {
                inputStream.close();
                byteArrayOutputStream.close();
            } catch (IOException e) {
                Log.i("HomeFragment", "IOException while closing the stream");
            }
        }

        String resultString;
        try {
            resultString = byteArrayOutputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.i("HomeFragment", "String from stream contains unsupported encoding");
            return null;
        }

        return resultString;
    }

    private void getTopMatches(Date date) {
        changeInProgress(true);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String todayDate = simpleDateFormat.format(date);

        String url = "https://footapi7.p.rapidapi.com/api/matches/top/" + todayDate;

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
                Log.i("HomeFragment", "Failed grabbing data with API");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();

                    executorService.execute(() -> {
                        try {
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                InputStream responseStream = responseBody.byteStream();
                                String jsonString = getStringFromRawRes(responseStream);
                                responseStream.close();
                                if (TextUtils.isEmpty(jsonString)) {
                                    changeInProgress(false);
                                    Toast.makeText(requireContext(), getString(R.string.matches_unavailable_for_selected_date), Toast.LENGTH_SHORT).show();
                                } else {
                                    decodeJSON(jsonString);
                                }
                            }
                        } catch (Exception e) {
                            Log.i("HomeFragment", "Executor failed while reading response");
                        }
                    });
                }
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                updateMatchesBasedOnSearch(s.toLowerCase());
                return true;
            }
        });
    }

    private void addMatchesBasedOnSearchTerms(String[] currentTerms) {
        allMatchesSearchTerms.clear();
        allMatchesFiltered.clear();
        for (Match m: allMatches) {
            for (String s: currentTerms) {
                if (m.getTournamentName().toLowerCase().contains(s) || m.getHomeTeam().toLowerCase().contains(s)
                        || m.getAwayTeam().toLowerCase().contains(s)) {
                    allMatchesSearchTerms.add(m);
                }
            }
        }
        allMatchesFiltered.addAll(allMatchesSearchTerms);
    }

    private void updateMatchesBasedOnSearch(String s) {
        ArrayList<Match> filteredMatches = new ArrayList<>();
        for (Match m: allMatchesFiltered) {
            if (m.getTournamentName().toLowerCase().contains(s) || m.getHomeTeam().toLowerCase().contains(s)
                || m.getAwayTeam().toLowerCase().contains(s)) {
                filteredMatches.add(m);
            }
        }
        requireActivity().runOnUiThread(() -> adapter.updateDate(filteredMatches));
    }

    private void updateMatchesBasedOnButton(Button b) {
        MatchType type = getMatchTypeBasedOnButtonId(b.getId());
        
        if (type == null) {
            allMatchesFiltered.clear();
            allMatchesFiltered.addAll(allMatchesSearchTerms);
            requireActivity().runOnUiThread(() -> adapter.updateDate(allMatchesSearchTerms));
        } else {
            ArrayList<Match> filteredMatches = new ArrayList<>();
            for (Match m: allMatchesSearchTerms) {
                if (m.getType() == type) {
                    filteredMatches.add(m);
                }
            }
            allMatchesFiltered.clear();
            allMatchesFiltered.addAll(filteredMatches);
            requireActivity().runOnUiThread(() -> adapter.updateDate(filteredMatches));
        }
    }

    private MatchType getMatchTypeBasedOnButtonId(int buttonId) {
        MatchType type = null;

        // This was originally in switch statement, but since app supports languages, switch based on
        // button text couldn't be used anymore. Switch based on button id also doesn't work, since id
        // isn't constant field. So this is the compromise
        if (buttonId == buttonLiveId) {
            type = MatchType.IN_PROGRESS;
        } else if (buttonId == buttonEndedId) {
            type = MatchType.FINISHED;
        } else if (buttonId == buttonUpcomingId) {
            type = MatchType.NOT_STARTED;
        }

        return type;
    }
}