package com.example.allergyscanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class AllergensActivity extends AppCompatActivity {

    private LinearLayout listContainer;
    private List<String> labels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allergens);

        //Zamiast listy trzeba będzie zrobić bazę danych

        listContainer = findViewById(R.id.list_container);

        labels = new ArrayList<>();
        labels.add("Gluten");
        labels.add("Skorupiaki");
        labels.add("Jaja");
        labels.add("Ryby");
        labels.add("Orzeszki ziemne");
        labels.add("Soja");
        labels.add("Mleko (laktoza)");
        labels.add("Orzechy");
        labels.add("Seler");
        labels.add("Gorczyca");
        labels.add("Sezam");
        labels.add("Siarczyny");
        labels.add("Łubin");
        labels.add("Mięczaki");

        populateList();

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> selected = getSelectedAllergens();
                Toast.makeText(AllergensActivity.this, "Wybrane: " + android.text.TextUtils.join(", ", selected), Toast.LENGTH_LONG).show();
            }
        });

        Button btnAdd = findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AllergensActivity.this, "Tu możesz dodać nowe alergeny (funkcja do zaimplementowania)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateList() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < labels.size(); i++) {
            final View row = inflater.inflate(R.layout.item_allergen, listContainer, false);
            TextView tv = row.findViewById(R.id.tv_allergen);
            final ImageView iv = row.findViewById(R.id.iv_circle);
            final CheckBox cb = row.findViewById(R.id.cb_allergen);

            final String label = labels.get(i);
            tv.setText(label);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cb.setChecked(!cb.isChecked());
                    if (cb.isChecked()) {
                        iv.setImageDrawable(ContextCompat.getDrawable(AllergensActivity.this, R.drawable.circle_checked));
                    } else {
                        iv.setImageDrawable(ContextCompat.getDrawable(AllergensActivity.this, R.drawable.circle_unchecked));
                    }
                }
            });

            listContainer.addView(row);
        }
    }

    private List<String> getSelectedAllergens() {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < listContainer.getChildCount(); i++) {
            View row = listContainer.getChildAt(i);
            CheckBox cb = row.findViewById(R.id.cb_allergen);
            TextView tv = row.findViewById(R.id.tv_allergen);
            if (cb.isChecked()) {
                selected.add(tv.getText().toString());
            }
        }
        return selected;
    }
}
