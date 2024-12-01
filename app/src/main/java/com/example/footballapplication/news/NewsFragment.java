package com.example.footballapplication.news;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.footballapplication.BuildConfig;
import com.example.footballapplication.R;
import com.example.footballapplication.orientation.ScreenSizeHelper;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
public class NewsFragment extends Fragment {

    ArrayList<News> newsArrayList;
    private ArrayList<News> allNews;

    RecyclerView recyclerView;
    NewsRecyclerAdapter adapter;
    LinearProgressIndicator linearProgressIndicator;
    SearchView searchView;

    public NewsFragment() {
        // Required empty public constructor
    }

    public static NewsFragment newInstance() {
        return new NewsFragment();
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
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    // When dealing with fragments, always use findViewById inside this function
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.news_recycler_view);
        linearProgressIndicator = view.findViewById(R.id.news_progress_infinite_bar);
        searchView = view.findViewById(R.id.news_search_view);
        setupSearchView();
        setupRecyclerView();

        if (!ScreenSizeHelper.smallPhone(requireActivity())) {
            getNews();
        }
    }

    private void setupRecyclerView() {
        newsArrayList = new ArrayList<>();
        allNews = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NewsRecyclerAdapter(newsArrayList, getContext());
        recyclerView.setAdapter(adapter);
    }

    private void changeInProgress(boolean show) {
        if (show) {
            linearProgressIndicator.setVisibility(View.VISIBLE);
        } else {
            linearProgressIndicator.setVisibility(View.INVISIBLE);
        }
    }

    public void getNews() {
        changeInProgress(true);

        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = simpleDateFormat.format(today);

        String url = "https://football-news11.p.rapidapi.com/api/news-by-date?date=" + todayDate + "&lang=en&page=1";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-key", BuildConfig.RAPID_API_KEY)
                .addHeader("x-rapidapi-host", "football-news11.p.rapidapi.com")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("NewsFragment", "Failed grabbing data with API");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();

                    executorService.execute(() -> {
                        try {
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                String responseString = responseBody.string();
                                if (!TextUtils.isEmpty(responseString)) {
                                    parseJSONAndCreateNews(responseString);
                                } else {
                                    Toast.makeText(requireContext(), getString(R.string.news_are_currently_unavailable), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (IOException e) {
                            Log.i("NewsFragment", "Executor failed while reading response");
                        }
                    });
                }
            }
        });
    }

    private void parseJSONAndCreateNews(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray array = root.getJSONArray("result");

            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);

                String title = object.getString("title");
                String image = object.getString("image");
                String url = object.getString("original_url");
                String time;

                String originalDate = object.getString("published_at");
                String[] dateSplit = originalDate.split(" ");
                if (dateSplit.length == 2) {
                    time = dateSplit[1];
                } else {
                    time = originalDate;
                }

                News news = new News(title, image, url, time);
                newsArrayList.add(news);
            }

            allNews.addAll(newsArrayList);

            requireActivity().runOnUiThread(new Runnable() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void run() {
                    changeInProgress(false);
                    adapter.notifyDataSetChanged();
                }
            });

            if (allNews.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.news_are_currently_unavailable), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            requireActivity().runOnUiThread(() -> changeInProgress(false));
            Log.i("NewsFragment", "Error while decoding JSON");
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                updateNewsBasedOnSearch(s);
                return true;
            }
        });
    }

    private void updateNewsBasedOnSearch(String s) {
        ArrayList<News> filteredNews = new ArrayList<>();
        for (News n : allNews) {
            if (n.getTitle().toLowerCase().contains(s.toLowerCase())) {
                filteredNews.add(n);
            }
        }
        requireActivity().runOnUiThread(() -> adapter.updateData(filteredNews));
    }
}