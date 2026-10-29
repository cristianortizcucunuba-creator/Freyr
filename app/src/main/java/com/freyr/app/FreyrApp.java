package com.freyr.app;

import android.app.Application;
import com.freyr.app.data.local.FreyrDatabase;
import com.freyr.app.data.repositorio.FreyrRepositorio;

public class FreyrApp extends Application {
    private FreyrRepositorio repository;

    @Override
    public void onCreate() {
        super.onCreate();
        FreyrDatabase.getDatabase(this);
        repository = new FreyrRepositorio(this);
    }

    public FreyrRepositorio getRepository() {
        return repository;
    }
}
