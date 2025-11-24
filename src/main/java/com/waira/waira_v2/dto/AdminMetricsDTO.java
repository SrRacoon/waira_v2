package com.waira.waira_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminMetricsDTO {
    private long totalUsuarios;
    private long totalOperadores;
    private long totalPaquetes;
    private long reservasActivas;
    private long comprasCompletadas;
}
