package com.waira.waira_v2.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "compras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCompra;

    @ManyToOne
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    @ManyToOne
    @JoinColumn(name = "id_metodo", nullable = false)
    private MetodoPago metodoPago;

    private String referenciaPago;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCompra = new Date();

    private String observaciones;
}

