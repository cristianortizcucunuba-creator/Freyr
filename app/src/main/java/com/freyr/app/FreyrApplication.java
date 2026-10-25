package com.freyr.app;

import android.app.Application;
import com.freyr.app.data.local.FreyrDatabase;
import com.freyr.app.data.repository.FreyrRepository;

public class FreyrApplication extends Application {
    private FreyrRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        FreyrDatabase.getDatabase(this);
        repository = new FreyrRepository(this);
    }

    public FreyrRepository getRepository() {
        return repository;
    }
}
