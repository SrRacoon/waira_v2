package com.waira.waira_v2.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReserva;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaActualizacion = new Date();

    @Column(nullable = false)
    private Integer cantidadPersonas;

    @Column(nullable = false)
    private Double precioUnitario;

    private String referenciaPago;

    private String notasCliente;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    private List<ReservaServicio> servicios;
}

