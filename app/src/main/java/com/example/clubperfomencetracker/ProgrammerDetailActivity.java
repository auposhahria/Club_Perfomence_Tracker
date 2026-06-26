package com.example.clubperfomencetracker;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProgrammerDetailActivity extends AppCompatActivity {
    private TextView tvName, tvCfId, tvMaxRating;
    private OkHttpClient client;
    private LoadingDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programmer_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String handle = getIntent().getStringExtra("CF_HANDLE");

        tvName = findViewById(R.id.tvDetailName);
        tvCfId = findViewById(R.id.tvDetailCfId);
        tvMaxRating = findViewById(R.id.tvDetailMaxRating);

        client = new OkHttpClient();
        progressDialog = new LoadingDialog(this);
        progressDialog.setMessage("Fetching user details...");

        if (handle != null) {
            tvCfId.setText(handle);
            fetchUserDetails(handle);
        }
    }

    private void fetchUserDetails(String handle) {
        progressDialog.show();
        String url = "https://codeforces.com/api/user.info?handles=" + handle;
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(ProgrammerDetailActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(ProgrammerDetailActivity.this, "Invalid Response", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                try {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    
                    if (!"OK".equals(jsonObject.getString("status"))) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(ProgrammerDetailActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                    
                    JSONArray result = jsonObject.getJSONArray("result");
                    if (result.length() > 0) {
                        JSONObject user = result.getJSONObject(0);
                        String firstName = user.optString("firstName", "");
                        String lastName = user.optString("lastName", "");
                        String combinedName = (firstName + " " + lastName).trim();
                        final String nameToDisplay = combinedName.isEmpty() ? handle : combinedName;
                        
                        final int maxRating = user.optInt("maxRating", 0);
                        final int currentRating = user.optInt("rating", 0);
                        
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            tvName.setText(nameToDisplay);
                            tvMaxRating.setText("Max Rating: " + maxRating + " | Current: " + currentRating);
                        });
                    } else {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(ProgrammerDetailActivity.this, "No user data found", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(ProgrammerDetailActivity.this, "Parse Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
