package com.waira.waira_v2.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ExplorarPayloadDTO {
    private List<CategoriaTreeDTO> categorias = new ArrayList<>();
    private List<ServicioExplorarDTO> servicios = new ArrayList<>();
}
