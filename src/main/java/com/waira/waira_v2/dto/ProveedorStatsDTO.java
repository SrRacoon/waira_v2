package com.waira.waira_v2.dto;

import lombok.Data;

@Data
public class ProveedorStatsDTO {
    private int totalServicios;
    private int serviciosActivos;
    private int serviciosSinImagen;
    private long totalVistas;
    private double precioPromedio;
    private String precioPromedioTexto = "$0";
}
