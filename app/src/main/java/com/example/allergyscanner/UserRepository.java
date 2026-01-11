package com.example.allergyscanner;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface RegisterCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface LoginCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
    }

    public void register(final String email, final String plainPassword, final String name, final RegisterCallback cb) {
        executor.execute(() -> {
            try {
                User existing = userDao.getUserByEmail(email);
                if (existing != null) {
                    mainHandler.post(() -> cb.onError("Użytkownik o tym emailu już istnieje"));
                    return;
                }
                String hash = PasswordUtil.sha256(plainPassword);
                User user = new User(email, hash, name);
                long id = userDao.insert(user);
                user.id = id;
                mainHandler.post(() -> cb.onSuccess(user));
            } catch (Exception e) {
                mainHandler.post(() -> cb.onError("Błąd rejestracji: " + e.getMessage()));
            }
        });
    }


    public void login(final String email, final String plainPassword, final LoginCallback cb) {
        executor.execute(() -> {
            try {
                User user = userDao.getUserByEmail(email);
                if (user == null) {
                    mainHandler.post(() -> cb.onError("Nie znaleziono użytkownika"));
                    return;
                }
                String hash = PasswordUtil.sha256(plainPassword);
                if (user.passwordHash != null && user.passwordHash.equals(hash)) {
                    mainHandler.post(() -> cb.onSuccess(user));
                } else {
                    mainHandler.post(() -> cb.onError("Nieprawidłowe hasło"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> cb.onError("Błąd logowania: " + e.getMessage()));
            }
        });
    }
}