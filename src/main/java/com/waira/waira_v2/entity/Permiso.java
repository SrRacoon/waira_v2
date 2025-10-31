package com.waira.waira_v2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permisos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPermiso;

    @ManyToOne
    @JoinColumn(name = "id_administrador", nullable = false)
    private Administrador administrador;

    @Column(nullable = false, unique = true, length = 50)
    private String nombrePermiso;
}

