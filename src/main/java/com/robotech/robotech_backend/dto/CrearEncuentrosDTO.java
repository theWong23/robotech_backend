package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class CrearEncuentrosDTO {

    private String idCategoriaTorneo;

    // ELIMINACION_DIRECTA | TODOS_CONTRA_TODOS
    private String tipoEncuentro;

    private String idJuez;
    private String idColiseo;
}
