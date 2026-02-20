package com.robotech.robotech_backend.model.entity;


import com.robotech.robotech_backend.model.enums.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.security.SecureRandom;
import java.util.List;

@Entity
@Table(name = "clubes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Club {

    @Id
    @Column(length = 8, nullable = false, unique = true)
    private String idClub;

    @Column(nullable = false, unique = true)
    private String codigoClub;

    @Column(nullable = false)
    private String nombre;

    private String correoContacto;
    private String telefonoContacto;
    private String direccionFiscal;
    @Column(name = "imagen_url")
    private String imagenUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoClub estado;

    @OneToOne
    @JoinColumn(name = "id_usuario_propietario", nullable = false, unique = true)
    private Usuario usuario;

    @JsonIgnore
    @OneToMany(mappedBy = "clubActual", cascade = CascadeType.ALL)
    private List<Competidor> competidores;


    // ------------------------------------------------------------
    //        GENERADOR DE ID ALFANUMÉRICO (8 CARACTERES)
    // ------------------------------------------------------------
    private static final String ALPHA_NUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String generarId() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHA_NUM.charAt(RANDOM.nextInt(ALPHA_NUM.length())));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------
    //   SE EJECUTA AUTOMÁTICAMENTE ANTES DE INSERTAR EN LA BD
    // ------------------------------------------------------------
    @PrePersist
    public void prePersist() {
        if (this.idClub == null) {
            this.idClub = generarId();
        }
    }
}



