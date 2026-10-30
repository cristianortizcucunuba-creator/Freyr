
package com.freyr.app.Clases.grupo;

// 1. IMPORTACIONES
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.freyr.app.FreyrApp;
import com.freyr.app.R;
import com.freyr.app.Clases.listas.listaDeInvitaciones;
import com.freyr.app.data.datos.Grupofamiliar;
import com.freyr.app.data.datos.Invitacion;
import com.freyr.app.data.datos.Usuario;
import com.freyr.app.data.repositorio.FreyrRepositorio;


// 2. CLASE PRINCIPAL
public class InvitarGrupoFamiliar extends Fragment {

    // 3. DECLARACIÓN DE COMPONENTES
    private RecyclerView rvInvitaciones;
    private TextView tvSinInvitaciones;

    // Adaptador encargado de mostrar las invitaciones
    private listaDeInvitaciones adaptadorInvitaciones;

    // Repositorio para acceder a los datos
    private FreyrRepositorio repositorio;

    // Arreglo que almacena todas las invitaciones
    private Invitacion[] todasLasInvitaciones = new Invitacion[0];


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
                R.layout.fragment_invitations,
                contenedor,
                false
        );

        // Obtener el repositorio de la aplicación
        repositorio = ((FreyrApp) requireActivity()
                .getApplication()).getRepositorio();

        inicializarVistas(vista);
        configurarLista();
        observarDatos();

        return vista;
    }


    // 5. INICIALIZACIÓN DE COMPONENTES
    private void inicializarVistas(View vista) {
        rvInvitaciones = vista.findViewById(R.id.rv_invitaciones);
        tvSinInvitaciones = vista.findViewById(R.id.tv_sin_invitaciones);
    }


    // 6. CONFIGURACIÓN DEL RECYCLERVIEW
    private void configurarLista() {

        // Crear el adaptador y definir las acciones de respuesta
        adaptadorInvitaciones = new listaDeInvitaciones(
                requireContext(),
                new listaDeInvitaciones.AccionesRespuestaInvitacion() {

                    // Aceptar una invitación
                    @Override
                    public void Aceptar(Invitacion invitacion) {

                        repositorio.responderInvitacion(
                                invitacion.getId(),
                                true,
                                new FreyrRepositorio.Callback<Void>() {

                                    // Se ejecuta cuando la invitación se acepta correctamente
                                    @Override
                                    public void alTenerExito(Void resultado) {

                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() ->
                                                    Toast.makeText(
                                                            getActivity(),
                                                            "¡Invitación aceptada! Te has unido al grupo familiar.",
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                        }
                                    }

                                    // Se ejecuta cuando ocurre un error
                                    @Override
                                    public void alOcurrirError(String error) {

                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() ->
                                                    Toast.makeText(
                                                            getActivity(),
                                                            "No fue posible aceptar la invitación: " + error,
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                        }
                                    }
                                }
                        );
                    }


                    // Rechazar una invitación
                    @Override
                    public void Rechazar(Invitacion invitacion) {

                        repositorio.responderInvitacion(
                                invitacion.getId(),
                                false,
                                new FreyrRepositorio.Callback<Void>() {

                                    // Se ejecuta cuando la invitación se rechaza correctamente
                                    @Override
                                    public void alTenerExito(Void result) {

                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() ->
                                                    Toast.makeText(
                                                            getActivity(),
                                                            "Invitación rechazada.",
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                        }
                                    }

                                    // Se ejecuta cuando ocurre un error
                                    @Override
                                    public void alOcurrirError(String error) {

                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() ->
                                                    Toast.makeText(
                                                            getActivity(),
                                                            "No fue posible rechazar la invitación: " + error,
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                        }
                                    }
                                }
                        );
                    }
                }
        );

        // Configurar la distribución vertical de la lista
        rvInvitaciones.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        // Asignar el adaptador al RecyclerView
        rvInvitaciones.setAdapter(adaptadorInvitaciones);
    }


    // 7. OBSERVACIÓN DE DATOS
    private void observarDatos() {

        // Observar todos los grupos familiares
        repositorio.observarTodosLosGrupos().observe(
                getViewLifecycleOwner(),
                groups -> {

                    if (groups != null) {

                        // Convertir la lista de grupos en un arreglo
                        adaptadorInvitaciones.setGrupos(
                                groups.toArray(new Grupofamiliar[0])
                        );
                    }
                }
        );


        // Observar todos los usuarios registrados
        repositorio.observarTodosLosUsuarios().observe(
                getViewLifecycleOwner(),
                users -> {

                    if (users != null) {

                        // Convertir la lista de usuarios en un arreglo
                        adaptadorInvitaciones.setUsuarios(
                                users.toArray(new Usuario[0])
                        );
                    }
                }
        );


        // Observar todas las invitaciones registradas
        repositorio.observarTodasLasInvitaciones().observe(
                getViewLifecycleOwner(),
                invitaciones -> {

                    if (invitaciones != null) {

                        // Convertir las invitaciones en un arreglo
                        todasLasInvitaciones = invitaciones.toArray(
                                new Invitacion[0]
                        );

                        // Crear un arreglo temporal para las invitaciones pendientes
                        Invitacion[] invitacionesPendientes =
                                new Invitacion[todasLasInvitaciones.length];

                        int cantidad = 0;

                        // Obtener el identificador del usuario actual
                        String idUsuarioActual =
                                repositorio.obtenerIdUsuarioActual();

                        // Filtrar las invitaciones dirigidas al usuario actual
                        // cuyo estado sea pendiente
                        for (Invitacion invitacion : todasLasInvitaciones) {

                            if (idUsuarioActual != null
                                    && idUsuarioActual.equals(
                                    invitacion.getIdUsuarioInvitado()
                            )
                                    && "pendiente".equalsIgnoreCase(
                                    invitacion.getEstado()
                            )) {

                                invitacionesPendientes[cantidad] = invitacion;
                                cantidad++;
                            }
                        }

                        // Ajustar el tamaño del arreglo a las invitaciones encontradas
                        Invitacion[] resultado = new Invitacion[cantidad];

                        System.arraycopy(
                                invitacionesPendientes,
                                0,
                                resultado,
                                0,
                                cantidad
                        );

                        // Actualizar las invitaciones mostradas en el adaptador
                        adaptadorInvitaciones.setInvitaciones(resultado);

                        // Mostrar u ocultar el mensaje cuando no hay invitaciones
                        tvSinInvitaciones.setVisibility(
                                cantidad == 0 ? View.VISIBLE : View.GONE
                        );
                    }
                }
        );
    }
}
