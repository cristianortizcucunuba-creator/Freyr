package com.freyr.app;

import android.app.Application;
import com.freyr.app.data.local.FreyrDatabase;
import com.freyr.app.data.repositorio.FreyrRepositorio;

public class FreyrApp extends Application {
    private FreyrRepositorio repositorio;

    @Override
    public void onCreate() {
        super.onCreate();
        FreyrDatabase.getDatabase(this);
        repositorio = new FreyrRepositorio(this);
    }

    public FreyrRepositorio getRepositorio() {
        return repositorio;
    }
}
