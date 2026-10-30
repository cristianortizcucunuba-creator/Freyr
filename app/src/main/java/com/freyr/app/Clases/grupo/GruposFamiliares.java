
package com.freyr.app.Clases.grupo;

// 1. IMPORTACIONES
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.Clases.listas.listaDeGruposFamiliares;
import com.freyr.app.data.datos.Deuda;
import com.freyr.app.data.datos.Grupofamiliar;
import com.freyr.app.data.datos.Pago;
import com.freyr.app.data.repositorio.FreyrRepositorio;


// 2. CLASE PRINCIPAL
public class GruposFamiliares extends Fragment {

    // 3. DECLARACIÓN DE COMPONENTES
    private Button btnCreargrupo;
    private RecyclerView rvGruposFamiliares;

    // Adaptador encargado de mostrar los grupos familiares
    private listaDeGruposFamiliares adaptadorGrupos;

    // Repositorio para acceder a los datos de la aplicación
    private FreyrRepositorio repositorio;


    // 4. CICLO DE VIDA DEL FRAGMENT
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflador,
            @Nullable ViewGroup contenedor,
            @Nullable Bundle estadoGuardado
    ) {

        // Cargar el diseño del fragment
        View vista = inflador.inflate(
                R.layout.fragment_family_groups,
                contenedor,
                false
        );

        // Obtener el repositorio de la aplicación
        repositorio = ((FreyrApp) requireActivity()
                .getApplication()).getRepositorio();

        inicializarVistas(vista);
        configurarAcciones();
        configurarLista();
        observarDatos();

        return vista;
    }


    // 5. INICIALIZACIÓN DE COMPONENTES
    private void inicializarVistas(View vista) {
        btnCreargrupo = vista.findViewById(R.id.btn_crear_grupo);
        rvGruposFamiliares = vista.findViewById(R.id.rv_fam_grupo);
    }


    // 6. CONFIGURACIÓN DE EVENTOS
    private void configurarAcciones() {

        // Abrir la actividad para crear un nuevo grupo familiar
        btnCreargrupo.setOnClickListener(v -> {

            Intent intencion = new Intent(
                    getActivity(),
                    CrearGrupoFamiliar.class
            );

            startActivity(intencion);
        });
    }


    // 7. CONFIGURACIÓN DEL RECYCLERVIEW
    private void configurarLista() {

        // Crear el adaptador y configurar la acción al seleccionar un grupo
        adaptadorGrupos = new listaDeGruposFamiliares(
                requireContext(),
                grupo -> {

                    // Abrir los detalles y configuración del grupo seleccionado
                    Intent intencion = new Intent(
                            getActivity(),
                            ConfigurarCuentaFamiliar.class
                    );

                    intencion.putExtra("group_id", grupo.getId());
                    startActivity(intencion);
                }
        );

        // Configurar el diseño de la lista
        rvGruposFamiliares.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        // Asignar el adaptador al RecyclerView
        rvGruposFamiliares.setAdapter(adaptadorGrupos);
    }


    // 8. OBSERVACIÓN DE DATOS
    private void observarDatos() {

        // Obtener el identificador del usuario actual
        String uid = repositorio.obtenerIdUsuarioActual();

        // Informar al adaptador qué usuario tiene la sesión activa
        adaptadorGrupos.setIdUsuarioActual(uid);


        // Observar todos los grupos familiares registrados
        repositorio.observarTodosLosGrupos().observe(
                getViewLifecycleOwner(),
                groups -> {

                    if (groups != null) {

                        // Crear un arreglo temporal para almacenar los grupos coincidentes
                        Grupofamiliar[] gruposCoincidentes =
                                new Grupofamiliar[groups.size()];

                        int cantidad = 0;

                        // Filtrar los grupos a los que pertenece el usuario actual
                        for (Grupofamiliar grupo : groups) {

                            if (grupo.getMiembrosIds() != null
                                    && grupo.getMiembrosIds().contains(uid)) {

                                gruposCoincidentes[cantidad] = grupo;
                                cantidad++;
                            }
                        }

                        // Ajustar el tamaño del arreglo a la cantidad de grupos encontrados
                        Grupofamiliar[] gruposDelUsuario =
                                new Grupofamiliar[cantidad];

                        System.arraycopy(
                                gruposCoincidentes,
                                0,
                                gruposDelUsuario,
                                0,
                                cantidad
                        );

                        // Actualizar los grupos mostrados en la lista
                        adaptadorGrupos.setGrupos(gruposDelUsuario);
                    }
                }
        );


        // Observar todas las deudas registradas
        repositorio.observarTodasLasDeudas().observe(
                getViewLifecycleOwner(),
                debts -> {

                    if (debts != null) {

                        // Convertir la lista de deudas en un arreglo
                        adaptadorGrupos.setDeudas(
                                debts.toArray(new Deuda[0])
                        );
                    }
                }
        );


        // Observar todos los pagos registrados
        repositorio.observarTodosLosPagos().observe(
                getViewLifecycleOwner(),
                payments -> {

                    if (payments != null) {

                        // Convertir la lista de pagos en un arreglo
                        adaptadorGrupos.setPagos(
                                payments.toArray(new Pago[0])
                        );
                    }
                }
        );
    }
}
