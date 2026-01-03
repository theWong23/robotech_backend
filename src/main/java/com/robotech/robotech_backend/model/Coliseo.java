package com.robotech.robotech_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Coliseo {
    @Id
    private String idColiseo;
    private String nombre;
    private String ubicacion;
}
