package com.robotech.robotech_backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import lombok.*;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "torneos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Torneo {

    @Id
    @Column(length = 8, nullable = false)
    private String idTorneo;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    @Temporal(TemporalType.DATE)
    private Date fechaInicio;

    @Temporal(TemporalType.DATE)
    private Date fechaFin;

    @Temporal(TemporalType.DATE)
    private Date fechaAperturaInscripcion;

    @Temporal(TemporalType.DATE)
    private Date fechaCierreInscripcion;

    @Column(nullable = false)
    private String tipo;   // INDIVIDUAL o EQUIPOS

    @Column(nullable = false)
    private String estado; // BORRADOR, INSCRIPCIONES_ABIERTAS, EN_PROGRESO, FINALIZADO

    @Column
    private Integer maxParticipantes;

    @Column
    private Integer numeroEncuentros;

    @Column
    private String creadoPor;

    @OneToMany(mappedBy = "torneo")
    @JsonManagedReference
    private List<CategoriaTorneo> categorias;

    private static final String ALPHA_NUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @PrePersist
    public void prePersist() {
        if (idTorneo == null) {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(ALPHA_NUM.charAt(RANDOM.nextInt(ALPHA_NUM.length())));
            }
            idTorneo = sb.toString();
        }
    }
}
