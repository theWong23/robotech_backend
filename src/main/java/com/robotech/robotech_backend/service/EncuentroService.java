package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearEncuentrosDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EncuentroService {

    private final CategoriaTorneoRepository categoriaRepo;
    private final InscripcionTorneoRepository inscripcionRepo;
    private final EquipoTorneoRepository equipoRepo;
    private final EncuentroRepository encuentroRepo;
    private final EncuentroParticipanteRepository participanteRepo;
    private final JuezRepository juezRepo;
    private final ColiseoRepository coliseoRepo;

    // ------------------------------------------------
    // GENERAR ENCUENTROS
    // ------------------------------------------------
    public List<Encuentro> generarEncuentros(CrearEncuentrosDTO dto) {

        CategoriaTorneo categoria = categoriaRepo.findById(dto.getIdCategoriaTorneo())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 🔒 Validación fuerte
        if (new Date().before(categoria.getTorneo().getFechaCierreInscripcion())) {
            throw new RuntimeException("Las inscripciones aún no han cerrado");
        }

        Juez juez = juezRepo.findById(dto.getIdJuez())
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

        Coliseo coliseo = coliseoRepo.findById(dto.getIdColiseo())
                .orElseThrow(() -> new RuntimeException("Coliseo no encontrado"));

        List<String> participantes = obtenerParticipantes(categoria);

        if (participantes.size() < 2) {
            throw new RuntimeException("No hay suficientes participantes");
        }

        if ("ELIMINACION_DIRECTA".equals(dto.getTipoEncuentro())) {
            return generarEliminacionDirecta(categoria, juez, coliseo, participantes);
        } else {
            return generarTodosContraTodos(categoria, juez, coliseo, participantes);
        }
    }


    private List<String> obtenerParticipantes(CategoriaTorneo categoria) {

        if (categoria.getModalidad() == ModalidadCategoria.INDIVIDUAL) {
            return inscripcionRepo
                    .findByCategoriaTorneoIdCategoriaTorneo(
                            categoria.getIdCategoriaTorneo()
                    )
                    .stream()
                    .map(i -> i.getRobot().getIdRobot())
                    .toList();
        } else {
            return equipoRepo
                    .findByCategoriaTorneoIdCategoriaTorneo(
                            categoria.getIdCategoriaTorneo()
                    )
                    .stream()
                    .map(e -> e.getIdEquipo())
                    .toList();
        }
    }


    private List<Encuentro> generarEliminacionDirecta(
            CategoriaTorneo categoria,
            Juez juez,
            Coliseo coliseo,
            List<String> participantes
    ) {
        List<Encuentro> encuentros = new ArrayList<>();

        for (int i = 0; i < participantes.size(); i += 2) {

            Encuentro e = Encuentro.builder()
                    .categoriaTorneo(categoria)
                    .juez(juez)
                    .coliseo(coliseo)
                    .estado("PENDIENTE")
                    .build();

            e = encuentroRepo.save(e);

            encuentros.add(e);

            // aquí luego metes EncuentroParticipante
        }

        return encuentros;
    }


    private List<Encuentro> generarTodosContraTodos(
            CategoriaTorneo categoria,
            Juez juez,
            Coliseo coliseo,
            List<String> participantes
    ) {
        List<Encuentro> encuentros = new ArrayList<>();

        for (int i = 0; i < participantes.size(); i++) {
            for (int j = i + 1; j < participantes.size(); j++) {

                Encuentro e = Encuentro.builder()
                        .categoriaTorneo(categoria)
                        .juez(juez)
                        .coliseo(coliseo)
                        .estado("PENDIENTE")
                        .build();

                e = encuentroRepo.save(e);
                encuentros.add(e);
            }
        }

        return encuentros;
    }


    private void crearParticipante(
            Encuentro encuentro,
            String idRef,
            CategoriaTorneo categoria
    ) {
        participanteRepo.save(
                EncuentroParticipante.builder()
                        .id(UUID.randomUUID().toString())
                        .encuentro(encuentro)
                        .tipo(
                                categoria.getModalidad() == ModalidadCategoria.INDIVIDUAL
                                        ? "ROBOT"
                                        : "EQUIPO"
                        )
                        .idReferencia(idRef)
                        .build()
        );
    }

}

