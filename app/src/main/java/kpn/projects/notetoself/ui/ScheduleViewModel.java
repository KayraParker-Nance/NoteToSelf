package kpn.projects.notetoself.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import kpn.projects.notetoself.schedule.Class;
import kpn.projects.notetoself.schedule.ClassScheduleRepository;

public class ScheduleViewModel extends AndroidViewModel {
    private final ClassScheduleRepository repository;
    private final LiveData<List<Class>> allClasses;

    public ScheduleViewModel(@NonNull Application application) {
        super(application);
        repository = ClassScheduleRepository.getInstance(application);
        allClasses = repository.getAll();
    }

    public LiveData<List<Class>> getAllClasses() {
        return allClasses;
    }

    public LiveData<Class> getClass(long id) {
        return repository.getByIdLive(id);
    }

    public void addClass(Class entry, Runnable onComplete) {
        repository.addClass(entry, onComplete);
    }

    public void updateClass(Class entry, Runnable onComplete) {
        repository.updateClass(entry, onComplete);
    }

    public void deleteClass(Class entry) {
        repository.deleteClass(entry);
    }
}
