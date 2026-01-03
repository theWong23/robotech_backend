package com.robotech.robotech_backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoriaEncuentroAdminDTO {

    private String idCategoriaTorneo;

    private String torneo;
    private String idTorneo;

    private String categoria;
    private String modalidad;

    private Integer maxParticipantes;
    private Integer inscritos;

    private boolean inscripcionesCerradas;
}
