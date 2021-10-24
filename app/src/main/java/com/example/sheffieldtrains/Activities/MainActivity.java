package com.example.sheffieldtrains;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    List times = new ArrayList<TrainTime>();
    TrainTimesRecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    ConstraintLayout loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("print", "test");

        loading = findViewById(R.id.loading);
        recyclerView = findViewById(R.id.train_times_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchTrainTimes();

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTrainTimes();
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchTrainTimes();
    }

    private void fetchTrainTimes() {
        if (isNetworkConnected()) {
            requestAPI();
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    private void requestAPI() {
        loading.setVisibility(View.VISIBLE);

        String appID = getString(R.string.app_id);
        String appKey = getString(R.string.app_key);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://transportapi.com/v3/uk/train/station/SHF/live.json?app_id=" + appID + "&app_key=" + appKey)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.d("error", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                Log.d("success", "onResponse: " + response.code());

                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    try {
                        JSONObject body = new JSONObject(responseBody.string());

                        //Getting departures JSONObject from body (which is the original object back from the response) and then got the "times" JSONArray from the departures JSONObject
                        JSONObject departures = body.getJSONObject("departures");
                        JSONArray trainTimes = departures.getJSONArray("all");

                        times.clear();//for when method is run again.
                        for (int i = 0; i < trainTimes.length(); i++) {
                            JSONObject trainTime = (JSONObject) trainTimes.get(i);
                            String time = trainTime.getString("expected_departure_time");
                            String platform = trainTime.getString("platform");
                            String name = trainTime.getString("destination_name");

                            TrainTime departure = new TrainTime(time, platform, name);
                            times.add(departure);
                        }
                        // running from main thread
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                adapter = new TrainTimesRecyclerViewAdapter(getApplicationContext(), times);
                                recyclerView.setAdapter(adapter);
                                loading.setVisibility(View.GONE);

                                if (times.isEmpty()) {
                                    Toast.makeText(getApplicationContext(), "No trains available at this time", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), response.message(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}