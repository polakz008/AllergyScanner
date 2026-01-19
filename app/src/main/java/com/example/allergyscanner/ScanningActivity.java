package com.example.allergyscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScanningActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private OpenFoodFactsApi api;
    private long userId;
    private boolean isScanning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        userId = getIntent().getLongExtra("user_id", 1);
        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://world.openfoodfacts.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(OpenFoodFactsApi.class);

        barcodeScanner = BarcodeScanning.getClient(new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build());

        if (hasCameraPermission()) startCamera();
        else requestCameraPermission();
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    if (!isScanning || imageProxy.getImage() == null) {
                        imageProxy.close();
                        return;
                    }
                    InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
                    barcodeScanner.process(image).addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String value = barcode.getRawValue();
                            if (value != null && isScanning) {
                                isScanning = false;
                                runOnUiThread(() -> fetchProductData(value));
                            }
                        }
                    }).addOnCompleteListener(task -> imageProxy.close());
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
            } catch (Exception e) { Log.e("Scanner", "Error", e); }
        }, ContextCompat.getMainExecutor(this));
    }

    private void fetchProductData(String barcode) {
        api.getProduct(barcode).enqueue(new retrofit2.Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, retrofit2.Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().status == 1) {
                    ProductResponse.Product p = response.body().product;

                    // DEBUGOWANIE - sprawdź co zwraca API
                    Log.d("API_DEBUG", "Nazwa: " + p.productName);
                    Log.d("API_DEBUG", "Marka: " + p.brands);
                    Log.d("API_DEBUG", "Skład (ingredients_text): " + p.ingredientsText);
                    Log.d("API_DEBUG", "Skład PL: " + p.ingredientsTextPl);
                    Log.d("API_DEBUG", "Skład EN: " + p.ingredientsTextEn);
                    Log.d("API_DEBUG", "Opis PL: " + p.descriptionPl);
                    Log.d("API_DEBUG", "Opis: " + p.description);

                    checkAllergensAndProceed(p);
                } else {
                    Toast.makeText(ScanningActivity.this, "Nieznany produkt", Toast.LENGTH_SHORT).show();
                    isScanning = true;
                }
            }
            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Log.e("API_ERROR", "Błąd połączenia", t);
                Toast.makeText(ScanningActivity.this, "Błąd połączenia z bazą danych", Toast.LENGTH_SHORT).show();
                isScanning = true;
            }
        });
    }

    private void checkAllergensAndProceed(ProductResponse.Product product) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Allergen> selected = db.allergenDao().getSelectedAllergens(userId);
            ArrayList<String> found = new ArrayList<>();

            Log.d("ALLERGEN_DEBUG", "=== ROZPOCZĘCIE SPRAWDZANIA ===");
            Log.d("ALLERGEN_DEBUG", "User ID: " + userId);
            Log.d("ALLERGEN_DEBUG", "Wybrane alergeny użytkownika: " + (selected != null ? selected.size() : 0));

            if (selected != null) {
                for (Allergen a : selected) {
                    Log.d("ALLERGEN_DEBUG", "  - " + a.name);
                }
            }

            String apiTags = product.allergensTags != null ? product.allergensTags.toString().toLowerCase() : "";
            Log.d("ALLERGEN_DEBUG", "API allergens_tags: " + apiTags);
            Log.d("ALLERGEN_DEBUG", "ingredients_text: " + product.ingredientsText);

            // Mapa tłumaczeń i wariantów alergenów - KOMPLETNA LISTA
            Map<String, String> translate = new HashMap<>();
            // Mleko i produkty mleczne
            translate.put("mleko", "milk");
            translate.put("mleko (laktoza)", "milk");
            translate.put("laktoza", "milk");
            translate.put("śmietanka", "milk");
            translate.put("śmietana", "milk");
            translate.put("nabiał", "milk");

            // Gluten i zboża
            translate.put("gluten", "gluten");
            translate.put("pszenica", "gluten");
            translate.put("żyto", "gluten");
            translate.put("jęczmień", "gluten");
            translate.put("owies", "gluten");

            // Jaja
            translate.put("jaja", "eggs");
            translate.put("jajka", "eggs");
            translate.put("jajko", "eggs");

            // Orzechy
            translate.put("orzechy", "nuts");
            translate.put("orzech", "nuts");
            translate.put("orzeszki ziemne", "peanuts");
            translate.put("arachidy", "peanuts");

            // Soja
            translate.put("soja", "soybeans");

            // Ryby i owoce morza
            translate.put("ryby", "fish");
            translate.put("ryba", "fish");
            translate.put("skorupiaki", "crustaceans");
            translate.put("mięczaki", "molluscs");

            // Warzywa i przyprawy
            translate.put("seler", "celery");
            translate.put("gorczyca", "mustard");
            translate.put("sezam", "sesame");

            // Inne
            translate.put("siarczyny", "sulphites");
            translate.put("dwutlenek siarki", "sulphites");
            translate.put("łubin", "lupin");

            for (Allergen a : selected) {
                String plName = a.name.toLowerCase().trim();

                // Usuń tekst w nawiasach np. "Mleko (laktoza)" -> "mleko"
                String plNameClean = plName.replaceAll("\\s*\\(.*?\\)\\s*", "").trim();

                String enName = translate.getOrDefault(plNameClean, plNameClean);

                // Sprawdź też samą nazwę bez czyszczenia (na wypadek gdyby była w mapie)
                if (!translate.containsKey(plNameClean) && translate.containsKey(plName)) {
                    enName = translate.get(plName);
                }

                Log.d("ALLERGEN_CHECK", "Szukam: '" + plName + "' -> clean: '" + plNameClean + "' (en: '" + enName + "')");
                Log.d("ALLERGEN_CHECK", "W apiTags: " + apiTags);

                boolean foundInPl = apiTags.contains(plNameClean);
                boolean foundInEn = apiTags.contains(enName);

                Log.d("ALLERGEN_CHECK", "Znaleziono w PL: " + foundInPl + ", w EN: " + foundInEn);

                if (foundInPl || foundInEn) {
                    found.add(a.name);
                    Log.d("ALLERGEN_FOUND", "✓ WYKRYTO ALERGEN: " + a.name);
                }
            }

            Log.d("ALLERGEN_RESULT", "=== WYNIK ===");
            Log.d("ALLERGEN_RESULT", "Wykryte alergeny (" + found.size() + "): " + found.toString());
            Log.d("ALLERGEN_RESULT", "================");

            runOnUiThread(() -> {
                Intent intent = new Intent(this, ResultActivity.class);

                // Nazwa produktu
                intent.putExtra("PRODUCT_NAME", product.productName);

                // Opis - jeśli brak, weź markę
                String displayDescription = product.getFinalDescription();
                if ((displayDescription == null || displayDescription.isEmpty()) && product.brands != null) {
                    displayDescription = "Marka: " + product.brands;
                }
                intent.putExtra("DESCRIPTION", displayDescription);

                // Skład - wykorzystaj nową metodę getFinalIngredients()
                intent.putExtra("INGREDIENTS", product.getFinalIngredients());
                intent.putStringArrayListExtra("FOUND_ALLERGENS", found);
                intent.putExtra("user_id", userId);

                startActivity(intent);
            });
        }).start();
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isScanning = true;
    }
}-