package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CategoriaEncuentroAdminDTO;
import com.robotech.robotech_backend.model.CategoriaTorneo;
import com.robotech.robotech_backend.model.ModalidadCategoria;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEncuentrosService {

    private final CategoriaTorneoRepository categoriaRepo;
    private final InscripcionTorneoRepository inscripcionRepo;
    private final EquipoTorneoRepository equipoRepo;

    // ----------------------------------------------------
    // LISTAR CATEGORÍAS PARA GESTIÓN DE ENCUENTROS
    // ----------------------------------------------------
    public List<CategoriaEncuentroAdminDTO> listarCategoriasActivas() {

        Date hoy = new Date();

        return categoriaRepo.findAll().stream()
                .map(categoria -> {

                    boolean inscripcionesCerradas =
                            hoy.after(categoria.getTorneo().getFechaCierreInscripcion());

                    int inscritos;
                    int max;

                    if (categoria.getModalidad() == ModalidadCategoria.INDIVIDUAL) {
                        inscritos = (int) inscripcionRepo
                                .countByCategoriaTorneoIdCategoriaTorneo(
                                        categoria.getIdCategoriaTorneo()
                                );
                        max = categoria.getMaxParticipantes();
                    } else {
                        inscritos = (int) equipoRepo
                                .countByCategoriaTorneoIdCategoriaTorneo(
                                        categoria.getIdCategoriaTorneo()
                                );
                        max = categoria.getMaxEquipos();
                    }

                    return CategoriaEncuentroAdminDTO.builder()
                            .idCategoriaTorneo(categoria.getIdCategoriaTorneo())
                            .categoria(categoria.getCategoria())
                            .modalidad(categoria.getModalidad().name())
                            .inscritos(inscritos)
                            .maxParticipantes(max)
                            .torneo(categoria.getTorneo().getNombre())
                            .idTorneo(categoria.getTorneo().getIdTorneo())
                            .inscripcionesCerradas(inscripcionesCerradas)
                            .build();
                })
                .toList();
    }
}
