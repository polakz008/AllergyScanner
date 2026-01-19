package com.example.allergyscanner;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class History extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private TextView tvEmptyMessage;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Pobierz userId z Intent (przekazane z MainActivity)
        userId = getIntent().getLongExtra("user_id", 1);

        recyclerView = findViewById(R.id.recyclerHistory);
        tvEmptyMessage = findViewById(R.id.tvEmptyHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this, null);
        recyclerView.setAdapter(adapter);

        loadHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Odśwież historię po powrocie z ResultActivity
        loadHistory();
    }

    private void loadHistory() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<ScanHistory> historyList = db.scanHistoryDao().getHistoryForUser(userId);

            runOnUiThread(() -> {
                if (historyList != null && !historyList.isEmpty()) {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmptyMessage.setVisibility(View.GONE);
                    adapter.updateData(historyList);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }
}