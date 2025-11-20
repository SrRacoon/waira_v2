package com.waira.waira_v2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.waira.waira_v2.dto.CrearServicioDTO;
import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.service.ServicioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/proveedor/servicios")
public class ProveedorServicioController {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorServicioController.class);

    @Autowired
    private ServicioService servicioService;

    @GetMapping("/nuevo")
    public String nuevoServicio(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/";
        }
        if (!tieneRolProveedor(usuario)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("crearServicioDTO", new CrearServicioDTO());
        return "provider-servicio-form";
    }

    @PostMapping
    public String crearServicio(@ModelAttribute CrearServicioDTO crearServicioDTO, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/";
        }
        if (!tieneRolProveedor(usuario)) {
            return "redirect:/dashboard";
        }
        try {
            servicioService.crearServicio(usuario, crearServicioDTO);
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Error al crear servicio", e);
            model.addAttribute("error", e.getMessage());
            return "provider-servicio-form";
        }
    }

    private boolean tieneRolProveedor(Usuario usuario) {
        if (usuario.getRoles() == null) return false;
        return usuario.getRoles().stream().anyMatch(r -> {
            if (r == null || r.getNombreRol() == null) return false;
            String nombre = r.getNombreRol().trim().toUpperCase();
            return nombre.contains("PROVEEDOR") || nombre.contains("OPERADOR");
        });
    }
}
