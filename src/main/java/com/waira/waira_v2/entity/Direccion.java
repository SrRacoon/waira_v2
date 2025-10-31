package com.waira.waira_v2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "direcciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDireccion;

    @Column(nullable = false, length = 50)
    private String tipoVia;

    @Column(nullable = false, length = 50)
    private String numero;

    private String complemento;

    @Column(nullable = false, length = 50)
    private String barrio;

    @Column(nullable = false, length = 30)
    private String ciudad;
}


