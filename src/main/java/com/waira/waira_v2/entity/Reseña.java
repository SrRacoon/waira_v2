package com.waira.waira_v2.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resenas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reseña {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idResena;


    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;


    @ManyToOne
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;


    private Integer calificacion;


    @Column(columnDefinition = "text")
    private String comentario;


    private Date fechaCreacion;
}
