package com.waira.waira_v2.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waira.waira_v2.dto.CategoriaTreeDTO;
import com.waira.waira_v2.dto.ExplorarPayloadDTO;
import com.waira.waira_v2.dto.ServicioExplorarDTO;
import com.waira.waira_v2.service.ServicioService;

@RestController
@RequestMapping("/api/explorar")
public class ExplorarRestController {

    @Autowired
    private ServicioService servicioService;

    @GetMapping
    public ResponseEntity<ExplorarPayloadDTO> obtenerExplorarCompleto() {
        return ResponseEntity.ok(servicioService.construirExplorarPayload());
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaTreeDTO>> listarCategorias() {
        return ResponseEntity.ok(servicioService.construirArbolCategorias());
    }

    @GetMapping("/servicios")
    public ResponseEntity<List<ServicioExplorarDTO>> listarServicios() {
        return ResponseEntity.ok(servicioService.construirCatalogoServicios());
    }
}
