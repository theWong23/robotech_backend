package com.robotech.robotech_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.security.SecureRandom;
import java.util.*;

@Entity
@Table(name = "competidores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competidor {

    @Id
    @Column(length = 8, nullable = false, unique = true)
    private String idCompetidor;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;  // <--- Perfil de usuario

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false, unique = true)
    private String dni;

    @Column(nullable = false)
    private String estadoValidacion;
    // PENDIENTE, APROBADO, RECHAZADO

    @Column(name = "foto_url")
    private String fotoUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_club")
    private Club club; // club de origen / histórico


    @OneToMany(mappedBy = "competidor")
    @JsonIgnore
    private List<Robot> robots;

    private static final String ALPHA_NUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String generarIdAlfanumerico() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHA_NUM.charAt(RANDOM.nextInt(ALPHA_NUM.length())));
        }
        return sb.toString();
    }

    // ---------- SE EJECUTA ANTES DE INSERTAR EN LA BD ----------
    @PrePersist
    public void prePersist() {
        if (this.idCompetidor == null) {
            this.idCompetidor = generarIdAlfanumerico();
        }
    }
}
