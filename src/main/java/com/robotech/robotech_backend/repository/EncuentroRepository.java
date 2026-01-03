package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.Encuentro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncuentroRepository extends JpaRepository<Encuentro, String> {

    List<Encuentro> findByCategoriaTorneoIdCategoriaTorneo(String idCategoriaTorneo);

    List<Encuentro> findByJuezIdJuez(String idJuez);
}

