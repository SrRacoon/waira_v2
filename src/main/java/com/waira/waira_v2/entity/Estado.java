package com.waira.waira_v2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Estado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEstado;

    @Column(nullable = false, unique = true, length = 20)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEntidad tipoEntidad;
}


