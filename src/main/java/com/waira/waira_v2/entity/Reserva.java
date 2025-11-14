package com.waira.waira_v2.entity;

import java.util.Date;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReserva;


    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;


    @ManyToOne
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;


    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;


    private Date fechaCreacion;
    private Date fechaActualizacion;
    private Date fechaExpedicion;


    private Integer cantidadPersonas;


    private Double precioUnitario;


    private String referenciaPago;


    @Column(columnDefinition = "text")
    private String notasCliente;
}
