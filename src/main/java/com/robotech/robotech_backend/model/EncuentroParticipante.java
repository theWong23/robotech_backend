package com.robotech.robotech_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "encuentro_participantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncuentroParticipante {

    @Id
    private String id;

    @ManyToOne
    private Encuentro encuentro;

    private String tipo; // ROBOT o EQUIPO
    private String idReferencia; // idRobot o idEquipo
}
