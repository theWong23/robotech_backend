package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InscripcionTorneoService {

    private final CategoriaTorneoRepository catRepo;
    private final RobotRepository robotRepo;
    private final InscripcionTorneoRepository inscripcionRepository;
    private final CompetidorRepository competidorRepository;

    // ----------------------------------------------------------------------
    // INSCRIBIR UN ROBOT A UNA CATEGORÍA DE TORNEO
    // ----------------------------------------------------------------------
    public InscripcionTorneo inscribir(String idCategoriaTorneo, String idRobot, String idUsuario) {

        CategoriaTorneo categoria = catRepo.findById(idCategoriaTorneo)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Torneo torneo = categoria.getTorneo();

        if (!"INDIVIDUAL".equals(torneo.getTipo())) {
            throw new RuntimeException("Este torneo no es individual");
        }

        Date hoy = new Date();
        if (hoy.before(torneo.getFechaAperturaInscripcion()) ||
                hoy.after(torneo.getFechaCierreInscripcion())) {
            throw new RuntimeException("Las inscripciones están fuera de fecha");
        }

        Competidor competidor = competidorRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        Robot robot = robotRepo.findById(idRobot)
                .orElseThrow(() -> new RuntimeException("Robot no encontrado"));

        if (!robot.getCompetidor().equals(competidor)) {
            throw new RuntimeException("El robot no pertenece al competidor");
        }

        // No duplicar inscripción
        boolean yaInscrito = inscripcionRepository
                .existsByRobotIdRobotAndCategoriaTorneoTorneoIdTorneo(idRobot, torneo.getIdTorneo());

        if (yaInscrito) {
            throw new RuntimeException("El robot ya está inscrito en este torneo");
        }

        // Cupos
        long inscritos = inscripcionRepository
                .countByCategoriaTorneoIdCategoriaTorneo(idCategoriaTorneo);

        if (inscritos >= categoria.getMaxParticipantes()) {
            throw new RuntimeException("Cupos agotados en esta categoría");
        }

        // Crear inscripción
        InscripcionTorneo i = InscripcionTorneo.builder()
                .categoriaTorneo(categoria)
                .robot(robot)
                .estado("PENDIENTE")
                .build();

        return inscripcionRepository.save(i);
    }

    // ----------------------------------------------------------------------
    // APROBAR INSCRIPCIÓN
    // ----------------------------------------------------------------------
    public InscripcionTorneo aprobar(String idInscripcion) {
        InscripcionTorneo i = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        i.setEstado("APROBADO");
        return inscripcionRepository.save(i);
    }

    // ----------------------------------------------------------------------
    // RECHAZAR INSCRIPCIÓN
    // ----------------------------------------------------------------------
    public InscripcionTorneo rechazar(String idInscripcion) {
        InscripcionTorneo i = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        i.setEstado("RECHAZADO");
        return inscripcionRepository.save(i);
    }

    // ----------------------------------------------------------------------
    // LISTAR INSCRITOS DE UN TORNEO
    // ----------------------------------------------------------------------
    public List<?> listarInscritos(String torneoId) {

        return inscripcionRepository.findByCategoriaTorneoTorneoIdTorneo(torneoId)
                .stream()
                .map(ins -> {
                    return new Object() {
                        public final String idCompetidor = ins.getRobot().getCompetidor().getIdCompetidor();
                        public final String competidor = ins.getRobot().getCompetidor().getUsuario().getCorreo();
                        public final String robot = ins.getRobot().getNombre();
                        public final String categoria = ins.getCategoriaTorneo().getCategoria();
                        public final String estado = ins.getEstado();
                    };
                })
                .collect(Collectors.toList());
    }
}
