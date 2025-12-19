package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TorneoRepository extends JpaRepository<Torneo, String> {


    List<Torneo> findByEstado(String estado);

    List<Torneo> findByEstadoIn(List<String> estados);

    List<Torneo> findByEstadoAndTipo(String estado, String tipo);

    List<Torneo> findByEstadoInAndTipo(List<String> estados, String tipo);

    List<Torneo> findByCreadoPor(String creadoPor);

}
