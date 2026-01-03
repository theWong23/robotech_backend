package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "encuentros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Encuentro {

    @Id
    @Column(length = 8)
    private String idEncuentro;

    @ManyToOne
    @JoinColumn(name = "id_categoria_torneo", nullable = false)
    private CategoriaTorneo categoriaTorneo;

    @ManyToOne
    @JoinColumn(name = "id_juez", nullable = false)
    private Juez juez;

    @ManyToOne
    @JoinColumn(name = "id_coliseo", nullable = false)
    private Coliseo coliseo;

    private Integer ronda; // 1, 2, semifinal, etc

    private String estado; // PENDIENTE, EN_JUEGO, FINALIZADO

    @PrePersist
    public void prePersist() {
        if (idEncuentro == null) {
            idEncuentro = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        estado = "PENDIENTE";
    }
}
