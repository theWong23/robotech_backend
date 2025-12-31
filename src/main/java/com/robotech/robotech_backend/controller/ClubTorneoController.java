package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.service.CategoriaTorneoService;
import com.robotech.robotech_backend.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/club/torneos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ClubTorneoController {

    private final TorneoService torneoService;
    private final CategoriaTorneoService categoriaTorneoService;

    // 🔥 TORNEOS DISPONIBLES
    @GetMapping("/disponibles")
    public ResponseEntity<?> listarDisponibles() {
        return ResponseEntity.ok(torneoService.listarDisponibles());
    }
    // 🔥 CATEGORÍAS DE UN TORNEO
    @GetMapping("/{idTorneo}/categorias")
    public ResponseEntity<?> categoriasPorTorneo(@PathVariable String idTorneo) {
        return ResponseEntity.ok(
                categoriaTorneoService.listarPorTorneo(idTorneo)
        );
    }
}
