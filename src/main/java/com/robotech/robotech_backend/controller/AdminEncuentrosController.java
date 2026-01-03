package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CategoriaEncuentroAdminDTO;
import com.robotech.robotech_backend.dto.CrearEncuentrosDTO;
import com.robotech.robotech_backend.model.Encuentro;
import com.robotech.robotech_backend.service.AdminEncuentrosService;
import com.robotech.robotech_backend.service.EncuentroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/encuentros")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public class AdminEncuentrosController {

    private final AdminEncuentrosService adminEncuentrosService;
    private final EncuentroService encuentroService;

    // ----------------------------------------------------
    // 1️⃣ LISTAR CATEGORÍAS LISTAS PARA GENERAR ENCUENTROS
    // ----------------------------------------------------
    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaEncuentroAdminDTO>> listarCategorias() {
        return ResponseEntity.ok(
                adminEncuentrosService.listarCategoriasActivas()
        );
    }

    // ----------------------------------------------------
    // 2️⃣ CREAR ENCUENTROS (ELIMINACIÓN / TODOS VS TODOS)
    // ----------------------------------------------------
    @PostMapping("/generar")
    public ResponseEntity<List<Encuentro>> generarEncuentros(
            @RequestBody CrearEncuentrosDTO dto
    ) {
        List<Encuentro> encuentros = encuentroService.generarEncuentros(dto);
        return ResponseEntity.ok(encuentros);
    }
}
