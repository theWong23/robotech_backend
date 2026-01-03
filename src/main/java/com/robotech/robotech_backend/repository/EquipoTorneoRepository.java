package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.EquipoTorneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipoTorneoRepository
        extends JpaRepository<EquipoTorneo, String> {

    // Equipos inscritos por categoría
    long countByCategoriaTorneoIdCategoriaTorneo(String idCategoria);

    // Ver si un robot ya está inscrito en este torneo
    boolean existsByRobotsIdRobotAndCategoriaTorneoTorneoIdTorneo(
            String idRobot,
            String idTorneo
    );

    // Listar equipos de un club en un torneo
    List<EquipoTorneo> findByClubIdClubAndCategoriaTorneoTorneoIdTorneo(
            String idClub,
            String idTorneo
    );

    // 🔹 Vista CLUB
    List<EquipoTorneo> findByClubUsuarioIdUsuario(String idUsuarioClub);

    // 🔹 Vista COMPETIDOR
    List<EquipoTorneo> findByRobotsCompetidorUsuarioIdUsuario(String idUsuario);

    List<EquipoTorneo> findByCategoriaTorneoIdCategoriaTorneo(String idCategoriaTorneo);

}
