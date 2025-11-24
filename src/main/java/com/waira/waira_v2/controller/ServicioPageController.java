package com.waira.waira_v2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.waira.waira_v2.service.ServicioService;

@Controller
public class ServicioPageController {

    private static final Logger logger = LoggerFactory.getLogger(ServicioPageController.class);

    @Autowired
    private ServicioService servicioService;

    @GetMapping("/servicios/{id}")
    public String verDetalleServicio(@PathVariable("id") Integer id, Model model) {
        return servicioService.obtenerDetalleServicio(id)
                .map(detalle -> {
                    model.addAttribute("detalle", detalle);
                    return "service-detail";
                })
                .orElseGet(() -> {
                    logger.warn("Servicio {} no disponible para detalle", id);
                    return "redirect:/explorar";
                });
    }
}
