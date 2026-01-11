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


public class Register extends AppCompatActivity {

    private UserRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new UserRepository(getApplication());

        EditText etEmail = findViewById(R.id.etEmailReg);
        EditText etPassword = findViewById(R.id.etPasswordReg);
        EditText etName = findViewById(R.id.etNickReg);
        Button btnRegister = findViewById(R.id.btnRegisterFinal);

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pwd = etPassword.getText().toString();
            String name = etName.getText().toString().trim();


            if (email.isEmpty() || pwd.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }
            repository.register(email, pwd, name, new UserRepository.RegisterCallback() {
                @Override
                public void onSuccess(User user) {
                    Toast.makeText(Register.this, "Zarejestrowano: " + user.email, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(Register.this, LogIn.class);
                    startActivity(i);
                    finish();
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}