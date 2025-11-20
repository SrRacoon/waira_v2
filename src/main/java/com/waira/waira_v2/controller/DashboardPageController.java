package com.waira.waira_v2.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.waira.waira_v2.entity.Rol;
import com.waira.waira_v2.entity.SolicitudProveedor;
import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.repository.SolicitudProveedorRepository;
import com.waira.waira_v2.repository.UsuarioRepository;
import com.waira.waira_v2.service.ServicioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardPageController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardPageController.class);

    @Autowired
    private SolicitudProveedorRepository solicitudRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ServicioService servicioService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        try {
            Usuario usuarioSession = (Usuario) session.getAttribute("usuarioLogueado");
            if (usuarioSession == null) {
                logger.warn("Usuario no logueado intenta acceder a /dashboard");
                return "redirect:/";
            }

            logger.info("Usuario {} accede a /dashboard", usuarioSession.getEmail());

            List<Rol> roles = usuarioSession.getRoles();
            boolean tieneAdmin = false;
            boolean tieneProveedor = false;

            if (roles != null && !roles.isEmpty()) {
                for (Rol r : roles) {
                    if (r == null || r.getNombreRol() == null) continue;
                    String n = r.getNombreRol().trim().toUpperCase();
                    logger.info("Verificando rol: {}", n);

                    if (n.equals("ADMINISTRADOR")) {
                        tieneAdmin = true;
                    } 
                    
                    if (n.equals("PROVEEDOR")) {
                        tieneProveedor = true;
                        System.out.println("Usuario tiene rol PROVEEDOR");
                    }
                }
            } else {
                logger.warn("Usuario {} no tiene roles asignados", usuarioSession.getEmail());
            }

            if (tieneAdmin) {
                logger.info("Returnando admin template para usuario {}", usuarioSession.getEmail());
                List<SolicitudProveedor> solicitudes = solicitudRepo.findAll();
                List<Usuario> usuarios = usuarioRepo.findAll();
                model.addAttribute("solicitudes", solicitudes);
                model.addAttribute("usuarios", usuarios);
                return "admin";
            }
            if (tieneProveedor) {
                logger.info("Returnando provider-dashboard template para usuario {}", usuarioSession.getEmail());
                model.addAttribute("servicios", servicioService.listarServiciosProveedor(usuarioSession));
                return "provider-dashboard";
            }

            logger.info("Returnando user-dashboard template para usuario {}", usuarioSession.getEmail());
            return "user-dashboard";
        } catch (Exception e) {
            logger.error("Error en /dashboard", e);
            return "redirect:/";
        }
    }

}
