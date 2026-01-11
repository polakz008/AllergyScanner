package com.example.allergyscanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LogIn extends AppCompatActivity {

    private UserRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new UserRepository(getApplication());

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnToRegister = findViewById(R.id.btnRegister);

        btnToRegister.setOnClickListener(v -> {
            Intent i = new Intent(LogIn.this, Register.class);
            startActivity(i);
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pwd = etPassword.getText().toString();

            if (email.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(LogIn.this, "Wypełnij email i hasło", Toast.LENGTH_SHORT).show();
                return;
            }

            repository.login(email, pwd, new UserRepository.LoginCallback() {
                @Override
                public void onSuccess(User user) {
                    Toast.makeText(LogIn.this, "Zalogowano jako: " + user.name, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(LogIn.this, MainActivity.class);
                    i.putExtra("user_id", user.id);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(LogIn.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}