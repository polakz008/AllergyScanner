package com.example.allergyscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

public class MainActivity extends AppCompatActivity {

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
        allergensButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAllergensActivity();
            }
        });
        Button scanButton = findViewById(R.id.btnScan);
        scanButton.setOnClickListener(v -> openScanningActivity());
    }
    private void openAllergensActivity() {
        Intent intent = new Intent(this, AllergensActivity.class);
        startActivity(intent);
    }

    private void openScanningActivity() {
        Intent intent = new Intent(this, ScanningActivity.class);
        startActivity(intent);
    }
    //test jira
}
