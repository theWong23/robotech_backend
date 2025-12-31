package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearTorneoDTO;
import com.robotech.robotech_backend.model.CategoriaTorneo;
import com.robotech.robotech_backend.model.Torneo;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.TorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TorneoService {

    private final TorneoRepository torneoRepo;
    private final CategoriaTorneoRepository categoriaRepo;

    // --------------------------------------------------
    // CREAR TORNEO
    // --------------------------------------------------
    public Torneo crearTorneo(CrearTorneoDTO dto, Authentication auth) {

        Torneo t = Torneo.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .fechaAperturaInscripcion(dto.getFechaAperturaInscripcion())
                .fechaCierreInscripcion(dto.getFechaCierreInscripcion())
                .build();

        // Reglas de negocio aquí
        t.setEstado("BORRADOR");

        if (auth != null && auth.getPrincipal() instanceof Usuario usuario) {
            t.setCreadoPor(usuario.getIdUsuario());
        } else {
            throw new RuntimeException("No autenticado");
        }

        return torneoRepo.save(t);
    }


    // --------------------------------------------------
    // LISTAR TODOS
    // --------------------------------------------------
    public List<Torneo> listar() {
        return torneoRepo.findAll();
    }

    // TORNEOS DISPONIBLES PARA CLUBES
    public List<Torneo> listarDisponibles() {
        return torneoRepo.findByEstado("INSCRIPCIONES_ABIERTAS");
    }

    // --------------------------------------------------
    // OBTENER POR ID
    // --------------------------------------------------
    public Torneo obtener(String id) {
        return torneoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
    }



    // --------------------------------------------------
    // EDITAR TORNEO
    // --------------------------------------------------
    public Torneo editar(String id, Torneo datos) {
        Torneo t = obtener(id);

        t.setNombre(datos.getNombre());
        t.setDescripcion(datos.getDescripcion());
        t.setFechaInicio(datos.getFechaInicio());
        t.setFechaFin(datos.getFechaFin());
        t.setFechaAperturaInscripcion(datos.getFechaAperturaInscripcion());
        t.setFechaCierreInscripcion(datos.getFechaCierreInscripcion());

        return torneoRepo.save(t);
    }

    // --------------------------------------------------
    // ABRIR INSCRIPCIONES (AUTOMÁTICO)
    // --------------------------------------------------
    public Torneo abrirInscripciones(String id) {
        Torneo t = obtener(id);

        Date hoy = new Date();

        if (hoy.before(t.getFechaAperturaInscripcion())) {
            throw new RuntimeException("Aún no es la fecha de apertura");
        }

        if (hoy.after(t.getFechaCierreInscripcion())) {
            throw new RuntimeException("La fecha de inscripción ya expiró");
        }

        t.setEstado("INSCRIPCIONES_ABIERTAS");
        return torneoRepo.save(t);
    }

    // --------------------------------------------------
    // CERRAR INSCRIPCIONES
    // --------------------------------------------------
    public Torneo cerrarInscripciones(String id) {
        Torneo t = obtener(id);
        t.setEstado("EN_PROGRESO");
        return torneoRepo.save(t);
    }

    // --------------------------------------------------
    // CAMBIAR ESTADO MANUALMENTE (ADMIN)
    // --------------------------------------------------
    public Torneo cambiarEstado(String id, String nuevoEstado) {

        Torneo t = obtener(id);

        if ("FINALIZADO".equals(t.getEstado())) {
            throw new RuntimeException("No se puede modificar un torneo finalizado");
        }

        List<String> permitidos = List.of(
                "BORRADOR",
                "INSCRIPCIONES_ABIERTAS",
                "EN_PROGRESO",
                "FINALIZADO"
        );

        if (!permitidos.contains(nuevoEstado)) {
            throw new RuntimeException("Estado inválido");
        }

        t.setEstado(nuevoEstado);
        return torneoRepo.save(t);
    }

    // --------------------------------------------------
    // ELIMINAR TORNEO
    // --------------------------------------------------
    public void eliminar(String id) {
        torneoRepo.deleteById(id);
    }

    // --------------------------------------------------
    // LISTAR TORNEOS PÚBLICOS
    // --------------------------------------------------
    public List<Torneo> listarPublicos() {
        return torneoRepo.findByEstadoIn(
                List.of("INSCRIPCIONES_ABIERTAS", "EN_PROGRESO", "FINALIZADO")
        );
    }

    // --------------------------------------------------
    // LISTAR CATEGORÍAS DE UN TORNEO
    // --------------------------------------------------
    public List<CategoriaTorneo> listarCategorias(String idTorneo) {
        Torneo torneo = obtener(idTorneo);
        return categoriaRepo.findByTorneo(torneo);
    }

    // --------------------------------------------------
    // LISTAR TORNEOS POR ADMINISTRADOR
    // --------------------------------------------------
    public List<Torneo> listarPorAdministrador(Authentication auth) {

        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            return torneoRepo.findAll();
        }

        if ("ADMIN".equals(usuario.getRol())) {
            return torneoRepo.findAll();
        }

        return torneoRepo.findByCreadoPor(usuario.getIdUsuario());
    }


}
