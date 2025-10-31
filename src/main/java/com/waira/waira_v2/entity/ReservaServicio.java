package com.waira.waira_v2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reserva_servicio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ReservaServicioId.class)
public class ReservaServicio {
    @Id
    @ManyToOne
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_servicio")
    private Servicio servicio;

    private Integer cantidad = 1;
    private Double precioUnitario;
}

