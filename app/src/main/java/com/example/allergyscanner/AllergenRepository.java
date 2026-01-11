package com.example.allergyscanner;

import static androidx.camera.core.impl.utils.Threads.runOnMain;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AllergenRepository {
    private final AllergenDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface LoadCallback {
        void onLoaded(List<Allergen> list);
    }

    public interface SaveCallback {
        void onSaved();
        void onError(String message);
    }

    public AllergenRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        dao = db.allergenDao();
    }

    public void getForUser(long userId, LoadCallback cb) {
        executor.execute(() -> {
            List<Allergen> list = dao.getForUser(userId);
            mainHandler.post(() -> cb.onLoaded(list));
        });
    }

    public void insertMany(final List<Allergen> items, Runnable onDone) {
        executor.execute(() -> {
            for (Allergen a : items) {
                dao.insert(a);
            }
            if (onDone != null) mainHandler.post(onDone);
        });
    }

    public void delete(Allergen allergen, Runnable callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            dao.deleteById(allergen.id);

            if (callback != null) {
                new Handler(Looper.getMainLooper()).post(callback);
            }
        });
    }

    public void updateMany(final List<Allergen> items, SaveCallback cb) {
        executor.execute(() -> {
            try {
                for (Allergen a : items) {
                    dao.update(a);
                }
                mainHandler.post(cb::onSaved);
            } catch (Exception e) {
                String msg = e.getMessage() == null ? "Błąd zapisu" : e.getMessage();
                mainHandler.post(() -> cb.onError(msg));
            }
        });
    }
}
