package kpn.projects.notetoself.schedule;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kpn.projects.notetoself.AppDatabase;

public class ClassScheduleRepository { private static volatile ClassScheduleRepository instance;

    private final ClassDao dao;
    private final ExecutorService executor;

    private ClassScheduleRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.dao = db.classDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static ClassScheduleRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (ClassScheduleRepository.class) {
                if (instance == null) {
                    instance = new ClassScheduleRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public LiveData<List<Class>> getAll() {
        return dao.getAll();
    }

    public LiveData<Class> getByIdLive(long id) {
        return dao.getByIdLive(id);
    }

    public void addClass(Class entry, Runnable onComplete) {
        executor.execute(() -> {
            dao.insert(entry);
            if (onComplete != null) onComplete.run();
        });
    }

    public void updateClass(Class entry, Runnable onComplete) {
        executor.execute(() -> {
            dao.update(entry);
            if (onComplete != null) onComplete.run();
        });
    }

    public void deleteClass(Class entry) {
        executor.execute(() -> dao.delete(entry));
    }
}
