
package com.freyr.app;

// 1. IMPORTACIONES
import android.app.Application;

import com.freyr.app.data.local.FreyrDatabase;
import com.freyr.app.data.repositorio.FreyrRepositorio;


// 2. CLASE PRINCIPAL DE LA APLICACIÓN
public class FreyrApp extends Application {

    // 3. ATRIBUTOS
    // Repositorio compartido para acceder a las operaciones de datos.
    private FreyrRepositorio repositorio;


    // 4. INICIALIZACIÓN DE LA APLICACIÓN
    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializa la base de datos de Freyr.
        FreyrDatabase.getDatabase(this);

        // Crea el repositorio utilizando el contexto de la aplicación.
        repositorio = new FreyrRepositorio(this);
    }


    // 5. MÉTODO DE ACCESO AL REPOSITORIO
    // Permite que otras clases obtengan el repositorio compartido.
    public FreyrRepositorio getRepositorio() {
        return repositorio;
    }
}
