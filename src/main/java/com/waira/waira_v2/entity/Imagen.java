package com.waira.waira_v2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "imagenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Imagen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idImagen;

    @ManyToOne
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @Column(nullable = false, unique = true, length = 255)
    private String url;
}

