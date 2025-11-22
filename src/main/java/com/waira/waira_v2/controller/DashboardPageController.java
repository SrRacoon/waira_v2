package com.waira.waira_v2.controller;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.waira.waira_v2.entity.Rol;
import com.waira.waira_v2.entity.Servicio;
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
    public String dashboard(
            @RequestParam(value = "section", required = false) String section,
            @RequestParam(value = "q", required = false) String query,
            HttpSession session,
            Model model) {
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
                    if (r == null || r.getNombreRol() == null) {
                        continue;
                    }
                    String n = r.getNombreRol().trim().toUpperCase(Locale.ROOT);
                    logger.info("Verificando rol: {}", n);

                    if ("ADMINISTRADOR".equals(n)) {
                        tieneAdmin = true;
                    }

                    if ("PROVEEDOR".equals(n)) {
                        tieneProveedor = true;
                        logger.debug("Usuario {} tiene rol PROVEEDOR", usuarioSession.getEmail());
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
                List<Servicio> serviciosProveedor = servicioService.listarServiciosProveedor(usuarioSession);

                String normalizedSection = StringUtils.hasText(section) ? section : "inicio";
                String criterio = query != null ? query.trim() : "";
                boolean hayFiltroActivo = !criterio.isBlank();
                List<Servicio> serviciosFiltrados = filtrarServicios(serviciosProveedor, criterio);

                model.addAttribute("currentSection", normalizedSection);
                model.addAttribute("servicios", serviciosFiltrados);
                model.addAttribute("tieneServicios", serviciosProveedor != null && !serviciosProveedor.isEmpty());
                model.addAttribute("hayFiltroActivo", hayFiltroActivo);
                model.addAttribute("buscarServiciosQuery", criterio);
                model.addAttribute("statsProveedor", servicioService.calcularStatsProveedor(serviciosProveedor));
                return "provider-dashboard";
            }

            logger.info("Returnando user-dashboard template para usuario {}", usuarioSession.getEmail());
            return "user-dashboard";
        } catch (Exception e) {
            logger.error("Error en /dashboard", e);
            return "redirect:/";
        }
    }

    private List<Servicio> filtrarServicios(List<Servicio> servicios, String criterio) {
        if (servicios == null || servicios.isEmpty() || !StringUtils.hasText(criterio)) {
            return servicios;
        }
        final String needle = normalizar(criterio).toLowerCase(Locale.ROOT);
        return servicios.stream()
                .filter(servicio -> contiene(servicio.getNombreServicio(), needle) || contiene(servicio.getDescripcion(), needle))
                .collect(Collectors.toList());
    }

    private boolean contiene(String valor, String needle) {
        if (valor == null) {
            return false;
        }
        String normalized = normalizar(valor).toLowerCase(Locale.ROOT);
        return normalized.contains(needle);
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String normalized = Normalizer.normalize(valor, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        try {
            session.invalidate();
            logger.info("Sesión finalizada correctamente");
        } catch (IllegalStateException e) {
            logger.warn("Sesión ya invalidada al intentar cerrar sesión", e);
        }
        return "redirect:/";
    }
}
