package com.example.allergyscanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllergensActivity extends AppCompatActivity {

    private LinearLayout listContainer;
    private AllergenRepository repository;
    private long userId = -1;

    private final List<Allergen> allergensInMemory = new ArrayList<>();

    private static final List<String> DEFAULT_LABELS = Arrays.asList(
            "Gluten", "Skorupiaki", "Jaja", "Ryby", "Orzeszki ziemne", "Soja",
            "Mleko (laktoza)", "Orzechy", "Seler", "Gorczyca", "Sezam", "Siarczyny",
            "Łubin", "Mięczaki"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allergens);

        repository = new AllergenRepository(getApplication());
        listContainer = findViewById(R.id.list_container);

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            userId = 1;
        }

        AppDatabase db = AppDatabase.getInstance(this);

        new Thread(() -> {
            User existingUser = db.userDao().getUserById(userId);
            if (existingUser == null) {
                db.userDao().insert(new User("test@mail.com", "haslo", "Test User"));
            }
            runOnUiThread(this::loadOrSeedAllergens);
        }).start();

        loadOrSeedAllergens();

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveSelectedAllergens());

        Button btnAdd = findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(v -> {
            final EditText input = new EditText(AllergensActivity.this);
            input.setHint("Nazwa alergenu");
            new androidx.appcompat.app.AlertDialog.Builder(AllergensActivity.this)
                    .setTitle("Dodaj nowy alergen")
                    .setView(input)
                    .setPositiveButton("Zapisz", (dialog, which) -> {
                        String name = input.getText().toString().trim();
                        if (name.isEmpty()) {
                            Toast.makeText(AllergensActivity.this, "Wpisz nazwę alergenu", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Allergen newAllergen = new Allergen(name, userId, false);
                        repository.insertMany(java.util.Collections.singletonList(newAllergen), () -> {
                            loadOrSeedAllergens();
                            Toast.makeText(AllergensActivity.this, "Dodano nowy alergen", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("Anuluj", null)
                    .show();
        });
    }

    private void loadOrSeedAllergens() {
        repository.getForUser(userId, list -> {
            if (list == null || list.isEmpty()) {
                List<Allergen> seed = new ArrayList<>();
                for (String label : DEFAULT_LABELS) {
                    seed.add(new Allergen(label, userId, false));
                }
                repository.insertMany(seed, this::loadFromDbAndPopulate);
            } else {
                allergensInMemory.clear();
                allergensInMemory.addAll(list);
                populateFromAllergensList(allergensInMemory);
            }
        });
    }

    private void loadFromDbAndPopulate() {
        repository.getForUser(userId, list -> {
            allergensInMemory.clear();
            allergensInMemory.addAll(list);
            populateFromAllergensList(allergensInMemory);
        });
    }

    private void populateFromAllergensList(List<Allergen> list) {
        listContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Allergen allergen : list) {
            final View row = inflater.inflate(R.layout.item_allergen, listContainer, false);
            TextView tv = row.findViewById(R.id.tv_allergen);
            final ImageView iv = row.findViewById(R.id.iv_circle);
            final CheckBox cb = row.findViewById(R.id.cb_allergen);

            tv.setText(allergen.name);
            cb.setChecked(allergen.selected);
            if (allergen.selected) {
                iv.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.circle_checked));
            } else {
                iv.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.circle_unchecked));
            }

            row.setOnClickListener(v -> {
                boolean newState = !cb.isChecked();
                cb.setChecked(newState);
                allergen.selected = newState;
                iv.setImageDrawable(ContextCompat.getDrawable(this,
                        newState ? R.drawable.circle_checked : R.drawable.circle_unchecked));
            });

            ImageView ivDelete = row.findViewById(R.id.iv_delete);
            if (!DEFAULT_LABELS.contains(allergen.name)) {
                ivDelete.setVisibility(View.VISIBLE);
                ivDelete.setOnClickListener(v -> {
                    new androidx.appcompat.app.AlertDialog.Builder(AllergensActivity.this)
                            .setTitle("Usuń alergen?")
                            .setMessage("Czy na pewno chcesz usunąć alergen \"" + allergen.name + "\"?")
                            .setPositiveButton("Tak", (dialog, which) -> {
                                repository.delete(allergen, () -> {
                                    loadOrSeedAllergens();
                                    Toast.makeText(AllergensActivity.this, "Usunięto alergen", Toast.LENGTH_SHORT).show();
                                });
                            })
                            .setNegativeButton("Nie", null)
                            .show();
                });
            } else {
                ivDelete.setVisibility(View.GONE);
            }

            listContainer.addView(row);
        }
    }

    private void saveSelectedAllergens() {
        repository.updateMany(allergensInMemory, new AllergenRepository.SaveCallback() {
            @Override
            public void onSaved() {
                Toast.makeText(AllergensActivity.this, "Zapisano alergeny", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AllergensActivity.this, "Błąd zapisu: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
