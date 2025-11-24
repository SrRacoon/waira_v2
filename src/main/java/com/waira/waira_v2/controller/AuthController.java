package com.waira.waira_v2.controller;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waira.waira_v2.dto.LoginDTO;
import com.waira.waira_v2.dto.RegistroDTO;
import com.waira.waira_v2.dto.SolicitudProveedorDTO;
import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.service.SolicitudProveedorService;
import com.waira.waira_v2.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SolicitudProveedorService solicitudProveedorService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody RegistroDTO dto, HttpSession session) {
        try {
            Usuario usuario = usuarioService.registrarCliente(dto);
            session.setAttribute("usuarioLogueado", usuario);
            String role = "UNKNOWN";
            if (usuario.getRoles() != null && !usuario.getRoles().isEmpty() && usuario.getRoles().get(0) != null) {
                role = usuario.getRoles().get(0).getNombreRol();
            }

            return ResponseEntity.ok(
                Map.of(
                    "success", true,
                    "role", role,
                    "redirect", "/explorar"
                )
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Registro inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error registrando usuario", e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Problemas al registrar, inténtelo más tarde"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        try {
            Usuario usuario = usuarioService.login(loginDTO.getEmail(), loginDTO.getContrasena());
            session.setAttribute("usuarioLogueado", usuario);

            String redirect = "/explorar"; // default para cliente
            java.util.List<String> rolesList = new java.util.ArrayList<>();
            boolean tieneAdmin = false;
            boolean tieneProveedor = false;
            String fallbackRole = null;

            if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
                for (var r : usuario.getRoles()) {
                    if (r == null || r.getNombreRol() == null) continue;
                    String raw = r.getNombreRol();
                    String upper = raw.trim().toUpperCase();
                    rolesList.add(raw);
                    if (fallbackRole == null) fallbackRole = upper;
                    logger.info("Rol detectado para {}: raw='{}' upper='{}'", loginDTO.getEmail(), raw, upper);
                    if (upper.contains("ADMINISTRADOR")) tieneAdmin = true;
                    if (esRolProveedor(upper)) tieneProveedor = true;
                }
            }

            String principalRole = fallbackRole != null ? fallbackRole : "CLIENTE";
            if (tieneAdmin) {
                principalRole = "ADMINISTRADOR";
                redirect = "/dashboard"; // controlador decide plantilla admin
            } else if (tieneProveedor) {
                principalRole = "PROVEEDOR";
                redirect = "/dashboard"; // unificado en una sola ruta
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "role", principalRole,
                "roles", rolesList,
                "redirect", redirect
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Login inválido para email {}: {}", loginDTO.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error en proceso de login para email {}", loginDTO.getEmail(), e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Problemas al iniciar sesión, inténtelo más tarde"));
        }
    }

    @PostMapping("/solicitud-proveedor")
    public ResponseEntity<?> solicitudProveedor(@RequestBody SolicitudProveedorDTO dto, HttpSession session) {
        try {
            logger.info("Datos recibidos: nit={}, razonSocial={}, tipoVia={}, numero={}, complemento={}, barrio={}, ciudad={}", 
                dto.getNit(), dto.getRazonSocial(), dto.getTipoVia(), dto.getNumero(), dto.getComplemento(), dto.getBarrio(), dto.getCiudad());
            
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
            if (usuario == null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Debes estar logueado para crear una solicitud"));
            }

            solicitudProveedorService.crearSolicitud(usuario, dto);

            return ResponseEntity.ok(
                Map.of(
                    "success", true,
                    "message", "Solicitud enviada. Plazo de respuesta: 15 días"
                )
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Solicitud de proveedor inválida: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creando solicitud de proveedor", e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Problemas al procesar tu solicitud, inténtelo más tarde"));
        }
    }

    @GetMapping("/verificar-solicitud")
    public ResponseEntity<?> verificarSolicitud(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return ResponseEntity.ok(Map.of("tieneSolicitud", false, "estado", null));
        }

        try {
            String estado = solicitudProveedorService.obtenerEstadoSolicitud(usuario);
            boolean tieneSolicitud = estado != null;
            return ResponseEntity.ok(Map.of("tieneSolicitud", tieneSolicitud, "estado", estado));
        } catch (Exception e) {
            logger.error("Error verificando solicitud de proveedor para usuario {}", usuario.getEmail(), e);
            return ResponseEntity.ok(Map.of("tieneSolicitud", false, "estado", null));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            session.invalidate();
        } catch (Exception ignored) {}
        return ResponseEntity.ok(Map.of(
            "success", true,
            "redirect", "/"
        ));
    }

    @GetMapping("/destino")
    public ResponseEntity<?> destino(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return ResponseEntity.ok(Map.of(
                "logged", false,
                "redirect", "/explorar"
            ));
        }

        String redirect = "/explorar"; // cliente por defecto
        boolean admin = false;
        boolean proveedor = false;
        if (usuario.getRoles() != null) {
            for (var r : usuario.getRoles()) {
                if (r == null || r.getNombreRol() == null) continue;
                String upper = r.getNombreRol().trim().toUpperCase();
                if (upper.contains("ADMINISTRADOR")) admin = true;
                if (esRolProveedor(upper)) proveedor = true;
            }
        }
        if (admin || proveedor) {
            redirect = "/dashboard";
        }
        return ResponseEntity.ok(Map.of(
            "logged", true,
            "redirect", redirect
        ));
    }

    @GetMapping("/estado")
    public ResponseEntity<?> estado(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return ResponseEntity.ok(Map.of(
                "autenticado", false,
                "esProveedor", false,
                "esAdmin", false,
                "urlCrearServicio", "/proveedor/servicios/nuevo"
            ));
        }

        boolean esAdmin = false;
        boolean esProveedor = false;
        if (usuario.getRoles() != null) {
            for (var rol : usuario.getRoles()) {
                if (rol == null || rol.getNombreRol() == null) continue;
                String upper = rol.getNombreRol().trim().toUpperCase();
                if (upper.contains("ADMINISTRADOR")) {
                    esAdmin = true;
                }
                if (esRolProveedor(upper)) {
                    esProveedor = true;
                }
            }
        }

        return ResponseEntity.ok(Map.of(
            "autenticado", true,
            "esProveedor", esProveedor,
            "esAdmin", esAdmin,
            "nombre", usuario.getNombres(),
            "urlCrearServicio", "/proveedor/servicios/nuevo"
        ));
    }

    private boolean esRolProveedor(String nombreNormalizado) {
        if (nombreNormalizado == null) return false;
        return nombreNormalizado.contains("PROVEEDOR") || nombreNormalizado.contains("OPERADOR");
    }
}

