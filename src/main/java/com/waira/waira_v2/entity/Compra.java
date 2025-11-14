package com.waira.waira_v2.entity;

import java.util.Date;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "compras")
@Data
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


    private Date fechaCompra;
}
