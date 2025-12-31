package com.robotech.robotech_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorias_torneo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaTorneo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String idCategoriaTorneo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_torneo", nullable = false)
    @JsonIgnore // 🔥 CLAVE: corta el bucle infinito
    private Torneo torneo;

    @Column(nullable = false)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModalidadCategoria modalidad;

    // Para INDIVIDUAL
    private Integer maxParticipantes;

    // Para EQUIPO
    private Integer maxEquipos;
    private Integer maxIntegrantesEquipo;

    private String descripcion;
}
