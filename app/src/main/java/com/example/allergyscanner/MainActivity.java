package com.example.allergyscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private LinearLayout allergensContainer;
    private AppDatabase db;
    private long userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button allergensButton = findViewById(R.id.btnAllergens);
        allergensButton.setOnClickListener(v -> openAllergensActivity());

        Button scanButton = findViewById(R.id.btnScan);
        scanButton.setOnClickListener(v -> openScanningActivity());

        allergensContainer = findViewById(R.id.allergensContainer);
        db = AppDatabase.getInstance(this);


        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            userId = 1; // tymczasowo – jeden użytkownik
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadSelectedAllergens();
    }

    private void openAllergensActivity() {
        Intent intent = new Intent(this, AllergensActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    private void openScanningActivity() {
        Intent intent = new Intent(this, ScanningActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    public void openHistoryActivity(View v) {
        Intent intent = new Intent(this, History.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    private void loadSelectedAllergens() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Allergen> allergens =
                    db.allergenDao().getSelectedAllergens(userId);

            runOnUiThread(() -> showAllergens(allergens));
        });
    }

    private void showAllergens(List<Allergen> allergens) {
        allergensContainer.removeAllViews();

        if (allergens == null || allergens.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("Brak wybranych alergenów");
            tv.setTextSize(18);
            allergensContainer.addView(tv);
            return;
        }

        for (Allergen allergen : allergens) {
            TextView tv = new TextView(this);
            tv.setText("• " + allergen.name);
            tv.setTextSize(20);
            tv.setTextColor(getColor(R.color.dark_teal));
            tv.setPadding(0, 8, 0, 8);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);

            allergensContainer.addView(tv);
        }
    }
}
