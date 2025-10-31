package com.waira.waira_v2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor extends Usuario {

    @Column(nullable = false, unique = true, length = 100)
    private String razonSocial;

    @Column(nullable = false, unique = true)
    private Integer nit;

    @ManyToOne
    @JoinColumn(name = "id_direccion", nullable = false)
    private Direccion direccion;
}


