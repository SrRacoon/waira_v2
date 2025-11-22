package com.waira.waira_v2.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.waira.waira_v2.dto.CrearServicioDTO;
import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.repository.CategoriaRepository;
import com.waira.waira_v2.service.ServicioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/proveedor/servicios")
public class ProveedorServicioController {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorServicioController.class);

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private CategoriaRepository categoriaRepository;

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
        model.addAttribute("categoriasDisponibles", categoriaRepository.findAllWithSubcategorias());
        return "provider-servicio-form";
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String crearServicio(@ModelAttribute CrearServicioDTO crearServicioDTO,
                                @RequestParam(name = "imagenes", required = false) MultipartFile[] imagenes,
                                HttpSession session,
                                Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/";
        }
        if (!tieneRolProveedor(usuario)) {
            return "redirect:/dashboard";
        }
        List<MultipartFile> imagenesList = imagenes != null ? Arrays.asList(imagenes) : Collections.emptyList();
        logger.info("Proveedor {} intenta crear servicio con {} imágenes adjuntas", usuario.getEmail(), imagenesList.size());

        try {
            servicioService.crearServicio(usuario, crearServicioDTO, imagenesList);
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Error al crear servicio", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categoriasDisponibles", categoriaRepository.findAllWithSubcategorias());
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
