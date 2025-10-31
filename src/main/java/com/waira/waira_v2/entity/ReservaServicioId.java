package com.waira.waira_v2.entity;

import java.io.Serializable;
import java.util.Objects;

public class ReservaServicioId implements Serializable {
    private Integer reserva;
    private Integer servicio;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservaServicioId)) return false;
        ReservaServicioId that = (ReservaServicioId) o;
        return Objects.equals(reserva, that.reserva) && Objects.equals(servicio, that.servicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reserva, servicio);
    }
}

